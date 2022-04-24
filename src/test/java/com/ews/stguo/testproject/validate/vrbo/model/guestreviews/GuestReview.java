package com.ews.stguo.testproject.validate.vrbo.model.guestreviews;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestReview {

    private String reviewerName;
    private Integer reviewerScore;
    private String reviewBrand;
    private String reviewText;

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Integer getReviewerScore() {
        return reviewerScore;
    }

    public void setReviewerScore(Integer reviewerScore) {
        this.reviewerScore = reviewerScore;
    }

    public String getReviewBrand() {
        return reviewBrand;
    }

    public void setReviewBrand(String reviewBrand) {
        this.reviewBrand = reviewBrand;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
}
