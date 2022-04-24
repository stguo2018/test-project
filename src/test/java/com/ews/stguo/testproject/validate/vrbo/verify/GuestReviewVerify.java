package com.ews.stguo.testproject.validate.vrbo.verify;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestReviewVerify extends SDPVerify{

    @Test
    public void test01() throws Exception {
        verify("GuestReviews", "sddp-feed-control-file (2).csv");
    }

    @Test
    public void test02() throws Exception {
        for (int i = 1; i <= 3; i++) {
            try (BufferedReader br = RWFileUtils.getReader(String.format("expedia-lodging-%d-all.jsonl", i))) {
                String line;
                while ((line = br.readLine()) != null) {
                    JSONObject o = new JSONObject(line);
                    JSONObject propertyId = o.optJSONObject("propertyId");
                    String ecomId = propertyId.optString("expedia");
                    if (StringUtils.equalsIgnoreCase(ecomId, "22648720")) {
                        System.out.println(line);
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected void dataStorage(Connection conn, JSONObject data, String ecomId, int fileIndex) throws Exception {
        String queryTemp = "insert into %s(%s) values(%s)";
        StringBuilder columns = new StringBuilder();
        List<Object> params = new ArrayList<>();
        columns.append("ecom_id");
        params.add(ecomId);
        Optional.ofNullable(data.optJSONObject("guestRating")).ifPresent(gr -> {
            Optional.ofNullable(gr.optJSONObject("expedia")).ifPresent(o -> {
                Optional.ofNullable(o.optString("avgRating")).ifPresent(v -> {
                    columns.append(",ecom_avg_rating");
                    params.add(v);
                });
                Optional.ofNullable(o.optString("reviewCount")).ifPresent(v -> {
                    columns.append(",ecom_review_count");
                    params.add(v);
                });
            });
            Optional.ofNullable(gr.optJSONObject("hcom")).ifPresent(o -> {
                Optional.ofNullable(o.optString("avgRating")).ifPresent(v -> {
                    columns.append(",hcom_avg_rating");
                    params.add(v);
                });
                Optional.ofNullable(o.optString("reviewCount")).ifPresent(v -> {
                    columns.append(",hcom_review_count");
                    params.add(v);
                });
            });
            Optional.ofNullable(gr.optJSONObject("vrbo")).ifPresent(o -> {
                Optional.ofNullable(o.optString("avgRating")).ifPresent(v -> {
                    columns.append(",vrbo_avg_rating");
                    params.add(v);
                });
                Optional.ofNullable(o.optString("reviewCount")).ifPresent(v -> {
                    columns.append(",vrbo_review_count");
                    params.add(v);
                });
            });
        });
        columns.append(",file_index");
        params.add(String.valueOf(fileIndex));
        JSONArray guestReviews = data.optJSONArray("guestReviews");
        int reviewsNumber = Optional.ofNullable(guestReviews)
                .map(grs -> grs.length()).orElse(0);
        int ecomReviews = 0;
        int hcomReviews = 0;
        int vrboReviews = 0;
        if (reviewsNumber > 0) {
            for (int i = 0; i < guestReviews.length(); i++) {
                JSONObject guestReview = guestReviews.optJSONObject(i);
                String reviewBrand = guestReview.optString("reviewBrand");
                ecomReviews = StringUtils.equalsIgnoreCase("expedia", reviewBrand) ? ecomReviews + 1 : ecomReviews;
                hcomReviews = StringUtils.equalsIgnoreCase("hcom", reviewBrand) ? hcomReviews + 1 : hcomReviews;
                vrboReviews = StringUtils.equalsIgnoreCase("vrbo", reviewBrand) ? vrboReviews + 1 : vrboReviews;
            }
        }
        columns.append(",ecom_reviews_number");
        params.add(ecomReviews);
        columns.append(",hcom_reviews_number");
        params.add(hcomReviews);
        columns.append(",vrbo_reviews_number");
        params.add(vrboReviews);
        String values = params.stream().map(a -> "?").collect(Collectors.joining(","));
        String query = String.format(queryTemp, "guest_review_summary", columns, values);
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            for (int i = 0; i < params.size(); i++) {
                int index = i + 1;
                preparedStatement.setObject(index, params.get(i));
            }
            preparedStatement.executeUpdate();
        }
        Optional.ofNullable(data.optJSONArray("guestReviews")).ifPresent(grs -> {
            for (int i = 0; i < grs.length(); i++) {
                int reviewId = i + 1;
                Optional.ofNullable(grs.optJSONObject(i)).ifPresent(o -> {
                    StringBuilder columns2 = new StringBuilder();
                    List<Object> params2 = new ArrayList<>();
                    columns2.append("review_id");
                    params2.add(reviewId);
                    columns2.append(",ecom_id");
                    params2.add(ecomId);
                    Optional.ofNullable(o.optString("reviewName")).ifPresent(v -> {
                        columns2.append(",review_name");
                        params2.add(v);
                    });
                    Optional.ofNullable(o.optString("reviewScore")).ifPresent(v -> {
                        columns2.append(",review_score");
                        params2.add(v);
                    });
                    Optional.ofNullable(o.optString("reviewBrand")).ifPresent(v -> {
                        columns2.append(",review_brand");
                        params2.add(v);
                    });
                    Optional.ofNullable(o.optString("reviewText")).ifPresent(v -> {
                        columns2.append(",review_text");
                        params2.add(v);
                    });
                    String values2 = params2.stream().map(a -> "?").collect(Collectors.joining(","));
                    String query2 = String.format(queryTemp, "guest_review_text", columns2, values2);
//                    try (PreparedStatement preparedStatement = conn.prepareStatement(query2)) {
//                        for (int j = 0; j < params2.size(); j++) {
//                            int index = j + 1;
//                            preparedStatement.setObject(index, params2.get(j));
//                        }
//                        preparedStatement.executeUpdate();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
                });
            }
        });

    }
}
