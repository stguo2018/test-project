package com.ews.stguo.testproject.feed.controlfile;

import com.ews.stguo.testproject.utils.client.ClientCreator;
import com.ews.stguo.testproject.utils.client.ContentProviderClient;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.expedia.www.ews.models.propertyinfo.v2.response.ContentType;
import com.expedia.www.ews.models.propertyinfo.v2.response.InventorySourceType;
import com.expedia.www.ews.models.propertyinfo.v2.response.PropertyInfoResponseType;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ControlFileTest {

    @Test
    public void test01() throws Exception {
        List<String> lines = ControlFileRWUtils.loadHotelIdStrByPaths("control-file.csv");
        try (BufferedWriter bw = RWFileUtils.getWriter("3PI-contorl-file.csv")) {
            for (String line : lines) {
                String[] columns = line.split(",");
                if (columns.length >= 3 && columns[1].equals("1") &&
                        !columns[2].contains("24") && !columns[2].contains("83") &&
                        !columns[2].contains("103")) {
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.flush();
        }
    }

    @Test
    public void test02() throws Exception {
        // 3PI Hotel Ids.csv
        List<String> hotelIds = ControlFileRWUtils.loadHotelIdStrByPaths("3PI Hotel Ids.csv")
                .stream().map(l -> l.split(",")[0]).collect(Collectors.toList());
        String url = "https://localhost:4411/query/v2/propertyInfos";
        CloseableHttpClient httpClient = ClientCreator.createHttpClient();
        RateLimiter rateLimiter = RateLimiter.create(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // ews-content-provider
        List<List<String>> partition = Lists.partition(hotelIds, Math.min(hotelIds.size(), 200));
        int count = partition.size();
        AtomicInteger i = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try (BufferedWriter bw = RWFileUtils.getWriter("record.csv")) {
            bw.write("ExpediaId,HcomId,VrboId,ExpediaBookable,HcomBookable,VrboBookable,ProviderIds,Link");
            bw.newLine();
            bw.flush();
            for (List<String> ids : partition) {
                int c = count;
                int ii = i.incrementAndGet();
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        rateLimiter.acquire();
                        i.incrementAndGet();
                        PropertyInfoResponseType propertyInfoV2 = ContentProviderClient.getPropertyInfoV2(url, ids, httpClient);
                        recordMessage(bw, propertyInfoV2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.printf("PropertyInfo: (%d/%d)%n", ii, c);
                }, executorService));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recordMessage(BufferedWriter bw, PropertyInfoResponseType res) throws Exception {
        if (res == null || CollectionUtils.isEmpty(res.getContents())) {
            return;
        }
        for (ContentType content : res.getContents()) {
            String expediaId = content.getPropertyId().getExpedia();
            String hcomId = Optional.ofNullable(content.getPropertyId().getHcom()).orElse("");
            String vrboId = Optional.ofNullable(content.getPropertyId().getVrbo()).orElse("");
            Boolean expediaBookable = Optional.ofNullable(content.getBookable().getExpedia()).orElse(false);
            Boolean hcomBookable = Optional.ofNullable(content.getBookable().getHcom()).orElse(false);
            Boolean vrboBookable = Optional.ofNullable(content.getBookable().getVrbo()).orElse(false);
            List<String> providers = new ArrayList<>();
            Optional.ofNullable(content.getInventorySources()).ifPresent(is -> {
                for (InventorySourceType ist : is) {
                    providers.add(ist.getId());
                }
            });
            String providerIds = CollectionUtils.isNotEmpty(providers) ? String.join(";", providers) :
                    "";
            if (providerIds.contains("24") || providerIds.contains("83") || providerIds.contains("103")) {
                continue;
            }
            synchronized (bw) {
                bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s", expediaId, hcomId, vrboId,
                        String.valueOf(expediaBookable), String.valueOf(hcomBookable),
                        String.valueOf(vrboBookable), providerIds,
                        String.format("https://www.expedia.com/Hotel-Search?selected=%s", expediaId)));
                bw.newLine();
            }
        }
        synchronized (bw) {
            bw.flush();
        }
    }

    @Test
    public void compareHcomControlFileWithNewAndOld() throws Exception {
        List<String> oldIds = ControlFileRWUtils.loadHotelIdStrByPaths("hcom-feed-control-file (1).csv")
                .stream().map(s -> s.split(",")[0]).collect(Collectors.toList());
        System.out.println("Old: " + oldIds.size());
        List<String> newIds = ControlFileRWUtils.loadHotelIdStrByPaths("hcom-feed-control-file.csv")
                .stream().map(s -> s.split(",")[0]).collect(Collectors.toList());
        System.out.println("New: " + newIds.size());
        List<String> addedIds = ListUtils.removeAll(newIds, new HashSet<>(oldIds));
        List<String> removedIds = ListUtils.removeAll(oldIds, new HashSet<>(newIds));
        System.out.println("Added: " + addedIds.size());
        System.out.println("Removed: " + removedIds.size());
        try (BufferedWriter bw = RWFileUtils.getWriter("AddedIds.csv")) {
            for (String addedId : addedIds) {
                bw.write(addedId);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("RemovedIds.csv")) {
            for (String removedId : removedIds) {
                bw.write(removedId);
                bw.newLine();
            }
            bw.flush();
        }
        System.out.println("Done");
    }

}
