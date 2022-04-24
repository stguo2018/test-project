package com.ews.stguo.testproject.test.feed;

import com.ews.stguo.testproject.utils.process.ConsoleProgressBarUtils;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Objects;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ValidateGuestReviewFeedFile {

    @Test
    public void test() throws Exception {
        String[] paths = new String[]{
                "guestreview-feed-1.jsonl",
                "guestreview-feed-2.jsonl",
                "guestreview-feed-3.jsonl",
                "guestreview-feed-4.jsonl",
                "guestreview-feed-5.jsonl",
                "guestreview-feed-6.jsonl"
        };
        int process = 0;
        int totalProcess = paths.length;
        for (String path : paths) {
            ConsoleProgressBarUtils.show((process / (float) totalProcess) * 100);
            try (BufferedReader reader = RWFileUtils.getReader(path);
                 BufferedWriter writer1 = RWFileUtils.getWriter("fullData.txt");
                 BufferedWriter writer2 = RWFileUtils.getWriter("data.txt")) {
                String json;
                while ((json = reader.readLine()) != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        Assert.assertNotNull(jsonObject);
                        JSONObject propertyId = jsonObject.getJSONObject("propertyId");
                        String vrboPropertyId = propertyId.getString("vrbo");
                        JSONArray guestReviews = jsonObject.getJSONArray("guestReviews");
                        if (StringUtils.isBlank(vrboPropertyId) || guestReviews == null) {
                            continue;
                        }
                        try {
                            boolean anyEcom = false;
                            boolean anyVrbo = false;
                            boolean anyHcom = false;
                            for (int i = 0; i < guestReviews.length(); i++) {
                                String reviewBrand = guestReviews.getJSONObject(i).getString("reviewBrand");
                                if (Objects.equals(reviewBrand, "expedia")) {
                                    anyEcom = true;
                                }
                                if (Objects.equals(reviewBrand, "vrbo")) {
                                    anyVrbo = true;
                                }
                                if (Objects.equals(reviewBrand, "hcom")) {
                                    anyHcom = true;
                                }
                                if (anyEcom && anyVrbo) {
                                    writer2.write(json);
                                    writer2.newLine();
                                    writer2.flush();
                                    if (anyHcom) {
                                        writer1.write(json);
                                        writer1.newLine();
                                        writer1.flush();
                                        break;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            System.out.println("next");
                        }
                    } catch (Exception e) {
                        System.out.println("error: " + json);
                        throw e;
                    }
                }
            }
            process++;
        }
        ConsoleProgressBarUtils.show((process / (float) totalProcess) * 100);
    }

}
