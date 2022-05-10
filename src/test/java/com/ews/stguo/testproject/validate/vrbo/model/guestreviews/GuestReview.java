package com.ews.stguo.testproject.validate.vrbo.model.guestreviews;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestReview {

    private String reviewerName;
    private Integer reviewerScore;
    private String reviewBrand;
    private String reviewText;
    private String creationDate;
    private String checkinDate;
    private String chackoutDate;
    private String languageCode;

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

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(String checkinDate) {
        this.checkinDate = checkinDate;
    }

    public String getChackoutDate() {
        return chackoutDate;
    }

    public void setChackoutDate(String chackoutDate) {
        this.chackoutDate = chackoutDate;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
