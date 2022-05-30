package com.ews.stguo.testproject.feed.common.images;

import cn.hutool.json.JSONUtil;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.validate.vrbo.model.images.Images;
import com.ews.stguo.testproject.validate.vrbo.model.images.ImagesModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ImagesTest {

    public static final String GENERAL_FILE_TEMP = "expedia-lodging-%d-all.jsonl";

    @Test
    public void test01() throws Exception {
        try (BufferedWriter bw = RWFileUtils.getWriter("hotels.csv")) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            AtomicInteger atomicInteger = new AtomicInteger(0);
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                System.out.println(String.format("%s - %d", LocalDateTime.now().toString(), atomicInteger.get()));
            }, 5L, 5L, TimeUnit.SECONDS);
            for (int i = 1; i <= 3; i++) {
                int index = i;
                futures.add(CompletableFuture.runAsync(() -> {
                    try (BufferedReader br = RWFileUtils.getReader(String.format(GENERAL_FILE_TEMP, index))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            ImagesModel model = JSONUtil.toBean(line, ImagesModel.class);
                            if (model != null) {
                                int imageSize = 0;
                                if (MapUtils.isNotEmpty(model.getImages())) {
                                    for (List<Images> value : model.getImages().values()) {
                                        imageSize += value.size();
                                    }
                                }
                                synchronized (bw) {
                                    bw.write(String.format("%s,%d", model.getPropertyId().getExpedia(), imageSize));
                                    bw.newLine();
                                }
                            }
                            synchronized (atomicInteger) {
                                atomicInteger.incrementAndGet();
                            }
                        }
                        synchronized (bw) {
                            bw.flush();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executorService));
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
            }
        }
    }

    @Test
    public void test02() throws Exception {
        Set<String> ids = ControlFileRWUtils.loadHotelIdStrByPaths("hotels.csv").stream()
                .map(String::valueOf).collect(Collectors.toSet());
        Map<String, Integer> map = new HashMap<>();
        try (BufferedReader br = RWFileUtils.getReader("hotels.csv")) {
            String line;
            while ((line = br.readLine()) != null) {
              String[] columns = line.split(",");
              map.put(columns[0], Integer.parseInt(columns[1]));
            }
        }
        System.out.println("MapSize: " + map.size());
        try (BufferedWriter bw = RWFileUtils.getWriter("hotels2.csv")) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            AtomicInteger atomicInteger = new AtomicInteger(0);
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                System.out.println(String.format("%s - %d", LocalDateTime.now().toString(), atomicInteger.get()));
            }, 5L, 5L, TimeUnit.SECONDS);
            for (int i = 1; i <= 3; i++) {
                int index = i;
                futures.add(CompletableFuture.runAsync(() -> {
                    try (BufferedReader br = RWFileUtils.getReader(String.format(GENERAL_FILE_TEMP, index))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            ImagesModel model = JSONUtil.toBean(line, ImagesModel.class);
                            if (model != null) {
                                int imageSize = 0;
                                if (MapUtils.isNotEmpty(model.getImages())) {
                                    for (List<Images> value : model.getImages().values()) {
                                        imageSize += value.size();
                                    }
                                }
                                String ecomId = model.getPropertyId().getExpedia();
                                if (map.containsKey(ecomId) && !Objects.equals(imageSize, map.get(ecomId))) {
                                    synchronized (bw) {
                                        bw.write(String.format("%s,%d,%d", ecomId, map.get(ecomId), imageSize));
                                        bw.newLine();
                                    }
                                }
                            }
                            synchronized (atomicInteger) {
                                atomicInteger.incrementAndGet();
                            }
                        }
                        synchronized (bw) {
                            bw.flush();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executorService));
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
            }
        }
    }

}
