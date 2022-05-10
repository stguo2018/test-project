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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

}
