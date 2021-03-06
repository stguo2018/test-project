package com.ews.stguo.testproject.feed.common.guestreviews;

import cn.hutool.json.JSONUtil;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.validate.vrbo.model.guestreviews.GuestReview;
import com.ews.stguo.testproject.validate.vrbo.model.guestreviews.GuestReviewsModel;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestReviewsTest {

    private static final String FILE_TEMP = "expedia-lodging-guestreviews-%d-all.jsonl";

    @Test
    public void test01() throws Exception {
        String fileName = "expedia-lodging-guestreviews-%d-all.jsonl";
        String recordFile = "ReviewCount-de2.csv";
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try (BufferedWriter bw = RWFileUtils.getWriter(recordFile)) {
            bw.write("ExpediaId,ReviewCount");
            bw.newLine();
            bw.flush();
            for (int i = 1; i <= 3; i++) {
                final BufferedWriter finalBw = bw;
                final int i2 = i;
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        int count = 0;
                        try (BufferedReader br = RWFileUtils.getReader(String.format(fileName, i2))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                GuestReviewsModel model = JSONUtil.toBean(line, GuestReviewsModel.class);
                                if (model == null) {
                                    continue;
                                }
                                String expediaId = model.getPropertyId().getExpedia();
                                int reviewCount = CollectionUtils.isNotEmpty(model.getGuestReviews()) ?
                                        model.getGuestReviews().size() : 0;
                                synchronized (finalBw) {
                                    finalBw.write(expediaId + "," + reviewCount);
                                    finalBw.newLine();
                                }
                                if (++count % 100000 == 0) {
                                    System.out.println("bw-" + i2 + "---->" + count);
                                }
                            }
                            synchronized (finalBw) {
                                finalBw.flush();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executorService));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        }
        try (BufferedReader br = RWFileUtils.getReader(recordFile)) {
            String line;
            int count = 0;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                int reviewCount = Integer.parseInt(line.split(",")[1]);
                if (reviewCount > 0) {
                    System.out.println(line);
                    if (count++ == 10) {
                        break;
                    }
                }
            }
        }
        System.out.println("Done");
    }

    @Test
    public void test02() throws Exception {
        String fileName = "expedia-lodging-guestreviews-%d-all.jsonl";
        int countEcom = 0;
        int countHcom = 0;
        int countVrbo = 0;
        for (int i = 1; i <= 3; i++) {
            try (BufferedReader br = RWFileUtils.getReader(String.format(fileName, i))) {
                String line;
                while ((line = br.readLine()) != null) {
                    GuestReviewsModel model = JSONUtil.toBean(line, GuestReviewsModel.class);
                    if (model == null) {
                        continue;
                    }
                    if (CollectionUtils.isNotEmpty(model.getGuestReviews())) {
                        Map<String, List<GuestReview>> groups = model.getGuestReviews().stream()
                                .collect(Collectors.groupingBy(GuestReview::getReviewBrand));
                        if (groups.get("expedia") != null) {
                            countEcom += groups.get("expedia").size();
                        }
                        if (groups.get("hcom") != null) {
                            countHcom += groups.get("hcom").size();
                        }
                        if (groups.get("vrbo") != null) {
                            countVrbo += groups.get("vrbo").size();
                        }
                    }
                }
            }
        }
        System.out.println("Ecom:" + countEcom);
        System.out.println("Hcom:" + countHcom);
        System.out.println("Vrbo:" + countVrbo);
        System.out.println("Done");
    }

}
