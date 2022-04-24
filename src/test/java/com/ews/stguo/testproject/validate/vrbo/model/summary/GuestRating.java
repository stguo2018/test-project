package com.ews.stguo.testproject.validate.vrbo.model.summary;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestRating {

    private Rating expedia;
    private Rating hcom;
    private Rating vrbo;

    public Rating getExpedia() {
        return expedia;
    }

    public void setExpedia(Rating expedia) {
        this.expedia = expedia;
    }

    public Rating getHcom() {
        return hcom;
    }

    public void setHcom(Rating hcom) {
        this.hcom = hcom;
    }

    public Rating getVrbo() {
        return vrbo;
    }

    public void setVrbo(Rating vrbo) {
        this.vrbo = vrbo;
    }

    public class Rating {
        private String avgRating;
        private String reviewCount;;

        public String getAvgRating() {
            return avgRating;
        }

        public void setAvgRating(String avgRating) {
            this.avgRating = avgRating;
        }

        public String getReviewCount() {
            return reviewCount;
        }

        public void setReviewCount(String reviewCount) {
            this.reviewCount = reviewCount;
        }
    }

}
