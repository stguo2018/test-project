package com.ews.stguo.testproject.test.freemaker;

import com.ews.stguo.testproject.utils.file.RWFileUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestReviewDataGenerator {

    public static Map<String, Object> getGuestReviewDataMap() throws Exception {
        List<Listing> listings = new ArrayList<>();
        Listing listing1 = new Listing();
        listing1.setEcomHotelId("23423432");
        listing1.setHcomHotelId("34523433");
        listing1.setVrboPropertyId("1.2.3");
        listing1.setEcomGuestRating(new GuestRating(4.6545455f, 100));
        listing1.setHcomGuestRating(new GuestRating(4.657871f, 201));
        listing1.setVrboGuestRating(new GuestRating(3.123123f, 45454));
        List<GuestReview> guestReviews = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            guestReviews.add(generateRandomReview("Listing1", 4));
        }
        String reviewText;
        try (BufferedReader br = RWFileUtils.getReader("text.txt")) {
            reviewText = br.readLine();
        }
//        reviewText = StringEscapeUtils.escapeJson(reviewText);
        guestReviews.get(0).setReviewText(reviewText);
        listing1.setGuestReviews(guestReviews);
        listings.add(listing1);

        Listing listing2 = new Listing();
        listing2.setEcomHotelId("23423432");
        listing2.setHcomHotelId("34523433");
        listing2.setVrboPropertyId("1.2.3");
        listing2.setEcomGuestRating(new GuestRating(4.654545f, 100));
        listing2.setHcomGuestRating(new GuestRating(4.657871f, 201));
        listing2.setVrboGuestRating(new GuestRating(3.123123f, 45454));
        guestReviews = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            guestReviews.add(generateRandomReview("Listing2", 3));
        }
        guestReviews.get(0).setReviewText(null);
        listing2.setGuestReviews(guestReviews);
        listings.add(listing2);

        Listing listing3 = new Listing();
        listing3.setEcomHotelId("23423432");
        listing3.setHcomHotelId("34523433");
        listing3.setVrboPropertyId("1.2.3");
        listing3.setEcomGuestRating(new GuestRating(4.654545f, 100));
        listing3.setHcomGuestRating(new GuestRating(4.657871f, 201));
        listing3.setVrboGuestRating(new GuestRating(3.123123f, 45454));
        listings.add(listing3);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("listings", listings);
        dataMap.put("generateBody", true);
        return dataMap;
    }

    private static GuestReview generateRandomReview(String reviewerName, int reviewerScore) {
        GuestReview guestReview = new GuestReview();
        guestReview.setReviewerName(reviewerName);
        guestReview.setReviewerScore(reviewerScore);
        guestReview.setReviewText(UUID.randomUUID().toString());
        return guestReview;
    }

    public static class Listing {
        private String ecomHotelId;
        private String hcomHotelId;
        private String vrboPropertyId;
        private GuestRating ecomGuestRating;
        private GuestRating hcomGuestRating;
        private GuestRating vrboGuestRating;
        private List<GuestReview> guestReviews;

        public String getEcomHotelId() {
            return ecomHotelId;
        }

        public void setEcomHotelId(String ecomHotelId) {
            this.ecomHotelId = ecomHotelId;
        }

        public String getHcomHotelId() {
            return hcomHotelId;
        }

        public void setHcomHotelId(String hcomHotelId) {
            this.hcomHotelId = hcomHotelId;
        }

        public String getVrboPropertyId() {
            return vrboPropertyId;
        }

        public void setVrboPropertyId(String vrboPropertyId) {
            this.vrboPropertyId = vrboPropertyId;
        }

        public GuestRating getEcomGuestRating() {
            return ecomGuestRating;
        }

        public void setEcomGuestRating(GuestRating ecomGuestRating) {
            this.ecomGuestRating = ecomGuestRating;
        }

        public GuestRating getHcomGuestRating() {
            return hcomGuestRating;
        }

        public void setHcomGuestRating(GuestRating hcomGuestRating) {
            this.hcomGuestRating = hcomGuestRating;
        }

        public GuestRating getVrboGuestRating() {
            return vrboGuestRating;
        }

        public void setVrboGuestRating(GuestRating vrboGuestRating) {
            this.vrboGuestRating = vrboGuestRating;
        }

        public List<GuestReview> getGuestReviews() {
            return guestReviews;
        }

        public void setGuestReviews(List<GuestReview> guestReviews) {
            this.guestReviews = guestReviews;
        }
    }

    public static class GuestRating {
        private float avgRating;
        private int reviewCount;

        public GuestRating(float avgRating, int reviewCount) {
            this.avgRating = avgRating;
            this.reviewCount = reviewCount;
        }

        public float getAvgRating() {
            return avgRating;
        }

        public void setAvgRating(float avgRating) {
            this.avgRating = avgRating;
        }

        public int getReviewCount() {
            return reviewCount;
        }

        public void setReviewCount(int reviewCount) {
            this.reviewCount = reviewCount;
        }
    }

    public static class GuestReview {
        private String reviewerName;
        private int reviewerScore;
        private String reviewText;

        public String getReviewerName() {
            return reviewerName;
        }

        public void setReviewerName(String reviewerName) {
            this.reviewerName = reviewerName;
        }

        public int getReviewerScore() {
            return reviewerScore;
        }

        public void setReviewerScore(int reviewerScore) {
            this.reviewerScore = reviewerScore;
        }

        public String getReviewText() {
            return reviewText;
        }

        public void setReviewText(String reviewText) {
            this.reviewText = reviewText;
        }
    }

}
