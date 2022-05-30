package com.ews.stguo.testproject.feed.common;

import com.ews.stguo.testproject.utils.client.ClientCreator;
import com.ews.stguo.testproject.utils.client.ContentProviderClient;
import com.ews.stguo.testproject.utils.client.PDQClient;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.expedia.www.ews.models.propertyinfo.v2.response.ContentType;
import com.expedia.www.ews.models.propertyinfo.v2.response.InventorySourceType;
import com.expedia.www.ews.models.propertyinfo.v2.response.PropertyInfoResponseType;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import java.io.BufferedWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class VrboPropertyIdTest {

    @Test
    public void test01() throws Exception {
        List<String> hotelIds = ControlFileRWUtils.loadHotelIdStrByPaths("sddp-feed-control-file (4).csv")
                .stream().map(line -> line.split(",")[0]).collect(Collectors.toList());
        String url = "https://localhost:4411/query/v2/propertyInfos";
        CloseableHttpClient httpClient = ClientCreator.createHttpClient();
        RateLimiter rateLimiter = RateLimiter.create(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // ews-content-provider
        List<String> missingVrboIds = new ArrayList<>();
        List<List<String>> partition = Lists.partition(hotelIds, Math.min(hotelIds.size(), 200));
        int count = partition.size();
        int i = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<String> ids : partition) {
            int c = count;
            int j = i++;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    rateLimiter.acquire();
                    PropertyInfoResponseType propertyInfoV2 = ContentProviderClient.getPropertyInfoV2(url, ids, httpClient);
                    missingVrboIds.addAll(extractMissingVrboIds(propertyInfoV2));
                    System.out.printf("PropertyInfo: (%d/%d)%n", j, c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        // PDQ
        Map<String, String> vrboIdMappingFromPDQ = new HashMap<>();
        partition = Lists.partition(missingVrboIds, Math.min(missingVrboIds.size(), 100));
        count = partition.size();
        i = 0;
        futures = new ArrayList<>();
        for (List<String> ids : partition) {
            int c = count;
            int j = i++;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    rateLimiter.acquire();
                    vrboIdMappingFromPDQ.putAll(PDQClient.getVrboIdMappingFromPDQ(ids, httpClient));
                    System.out.printf("PDQ: (%d/%d)%n", j, c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        System.out.println("MissingVrboIdsSize: " + missingVrboIds.size());
        System.out.println("VrboIdMappingFromPDQSize: " + vrboIdMappingFromPDQ.size());
        try (BufferedWriter bw = RWFileUtils.getWriter("MissingVrboIds.csv")) {
            bw.write("ExpediaId");
            bw.newLine();
            bw.flush();
            for (String id : missingVrboIds) {
                bw.write(id);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("VrboIdMappingFromPDQ.csv")) {
            bw.write("ExpediaId,VrboId");
            bw.newLine();
            bw.flush();
            for (Map.Entry<String, String> entry : vrboIdMappingFromPDQ.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
            bw.flush();
        }
        System.out.println("Done!");
    }

    private List<String> extractMissingVrboIds(PropertyInfoResponseType propertyInfoV2) {
        List<String> missingVrboIds = new ArrayList<>();
        if (propertyInfoV2 == null || CollectionUtils.isEmpty(propertyInfoV2.getContents())) {
            return missingVrboIds;
        }
        for (ContentType content : propertyInfoV2.getContents()) {
            boolean isVrbo = false;
            for (InventorySourceType inventorySource : content.getInventorySources()) {
                if (inventorySource.getId().equals("83") || inventorySource.getId().equals("103")) {
                    isVrbo = true;
                    break;
                }
            }
            if (isVrbo && StringUtils.isBlank(content.getPropertyId().getVrbo())) {
                missingVrboIds.add(content.getPropertyId().getExpedia());
            }
        }
        return missingVrboIds;
    }

    @Test
    public void extractDataForAutomationTestWithBookable() throws Exception {
        List<String> hotelIds = ControlFileRWUtils.loadHotelIdStrByPaths("sddp-feed-control-file.csv")
                .stream().map(line -> line.split(",")[0]).collect(Collectors.toList());
        Collections.shuffle(hotelIds);
        String url = "https://localhost:4411/query/v2/propertyInfos";
        CloseableHttpClient httpClient = ClientCreator.createHttpClient();
        List<List<String>> partition = Lists.partition(hotelIds, Math.min(hotelIds.size(), 200));
        int count = partition.size();
        class Record {
            int i = 0;
            int instantBookable = 0;
            int instantUnbookable = 0;
            int quoteAndHoldBookable = 0;
            int quoteAndHoldUnbookable = 0;
        }
        Record r = new Record();
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            System.out.printf("%s - PropertyInfo: (%d/%d)%n", LocalDateTime.now(), r.i, count);
            System.out.printf("%s - %d,%d,%d,%d%n", LocalDateTime.now(), r.instantBookable, r.instantUnbookable,
                    r.quoteAndHoldBookable, r.quoteAndHoldUnbookable);
        }, 5, 5, TimeUnit.SECONDS);
        try (BufferedWriter bw = RWFileUtils.getWriter("Contents.csv")) {
            bw.write("ExpediaId,VrboTriad,Bookable,InstantBook");
            bw.newLine();
            bw.flush();
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            RateLimiter rateLimiter = RateLimiter.create(10);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (List<String> ids : partition) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        synchronized (r) {
                            if (r.instantBookable >= 35000 && r.instantUnbookable >= 5000 &&
                                    r.quoteAndHoldBookable >= 8750 && r.quoteAndHoldUnbookable >= 1250) {
                                return;
                            }
                        }
                        rateLimiter.acquire();
                        PropertyInfoResponseType propertyInfoV2 = ContentProviderClient.getPropertyInfoV2(url, ids, httpClient);
                        synchronized (r) {
                            r.i++;
                        }
                        for (ContentType content : propertyInfoV2.getContents()) {
                            try {
                                String expedia = content.getPropertyId().getExpedia();
                                String vrbo = content.getPropertyId().getVrbo();
                                if (StringUtils.isBlank(vrbo)) {
                                    continue;
                                }
                                Boolean vrboBookable = content.getBookable().getVrbo();
                                Boolean instantBook = content.getInstantBook();
                                boolean ib = r.instantBookable < 35000 && BooleanUtils.isTrue(instantBook) &&
                                        BooleanUtils.isTrue(vrboBookable);
                                boolean inb = r.instantUnbookable < 5000 && BooleanUtils.isTrue(instantBook) &&
                                        BooleanUtils.isFalse(vrboBookable);
                                boolean qb = r.quoteAndHoldBookable < 8750 && BooleanUtils.isFalse(instantBook) &&
                                        BooleanUtils.isTrue(vrboBookable);
                                boolean qnb = r.quoteAndHoldUnbookable < 1250 && BooleanUtils.isFalse(instantBook) &&
                                        BooleanUtils.isFalse(vrboBookable);
                                if (ib || inb || qb || qnb) {
                                    synchronized (r) {
                                        if (ib) r.instantBookable++;
                                        if (inb) r.instantUnbookable++;
                                        if (qb) r.quoteAndHoldBookable++;
                                        if (qnb) r.quoteAndHoldUnbookable++;
                                    }
                                    synchronized (bw) {
                                        bw.write(String.format("%s,%s,%s,%s", expedia, vrbo,
                                                String.valueOf(vrboBookable), String.valueOf(instantBook)));
                                        bw.newLine();
                                        bw.flush();
                                    }
                                }
                                synchronized (r) {
                                    if (r.instantBookable >= 35000 && r.instantUnbookable >= 5000 &&
                                            r.quoteAndHoldBookable >= 8750 && r.quoteAndHoldUnbookable >= 1250) {
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executorService));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        }
    }

    @Test
    public void findMissedMappingFromVrboMapping() throws Exception {
        Set<String> vrboMappingIds = ControlFileRWUtils.loadHotelIdStrByPaths("ews-hotel-static-ecomid-vrbo-mapping.csv")
                .stream().map(line -> line.split(",")[0]).collect(Collectors.toSet());
        List<String> sdpIds = ControlFileRWUtils.loadHotelIdStrByPaths("sddp-feed-control-file (1).csv")
                .stream().map(line -> line.split(",")[0]).collect(Collectors.toList());
        System.out.println(vrboMappingIds.size());
        System.out.println(sdpIds.size());
        List<String> list = ListUtils.removeAll(sdpIds, vrboMappingIds);
        System.out.println(list.size());
        try (BufferedWriter br = RWFileUtils.getWriter("MissedHotelIds.csv")) {
            for (String s : list) {
                br.write(s);
                br.newLine();
            }
            br.flush();
        }
    }

    @Test
    public void checkedMissedMappingFromPDQ() throws Exception {
        List<String> hotelIds = ControlFileRWUtils.loadHotelIdStrByPaths("noVrboMapping.txt")
                .stream().map(line -> line.split(",")[0]).collect(Collectors.toList());
        Map<String, String> vrboIdMappingFromPDQ = new HashMap<>();
        List<List<String>> partition = Lists.partition(hotelIds, Math.min(hotelIds.size(), 100));
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int count = partition.size();
        int i = 0;
        RateLimiter rateLimiter = RateLimiter.create(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try (CloseableHttpClient httpClient = ClientCreator.createHttpClient()) {
            for (List<String> ids : partition) {
                int c = count;
                int j = i++;
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        rateLimiter.acquire();
                        vrboIdMappingFromPDQ.putAll(PDQClient.getVrboIdMappingFromPDQ(ids, httpClient));
                        System.out.printf("PDQ: (%d/%d)%n", j, c);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executorService));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
            System.out.println("VrboIdMappingFromPDQSize: " + vrboIdMappingFromPDQ.size());
            try (BufferedWriter bw = RWFileUtils.getWriter("VrboIdMappingFromPDQ.csv")) {
                bw.write("ExpediaId,VrboId");
                bw.newLine();
                bw.flush();
                for (Map.Entry<String, String> entry : vrboIdMappingFromPDQ.entrySet()) {
                    bw.write(entry.getKey() + "," + entry.getValue());
                    bw.newLine();
                }
                bw.flush();
            }
        }
        System.out.println("Done!");
    }

}
