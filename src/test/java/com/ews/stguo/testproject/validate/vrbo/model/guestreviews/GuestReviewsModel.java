package com.ews.stguo.testproject.validate.vrbo.model.guestreviews;

import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;
import com.ews.stguo.testproject.validate.vrbo.model.summary.GuestRating;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestReviewsModel {

    private PropertyId propertyId;
    private GuestRating guestRating;
    private List<GuestReview> guestReviews;

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(PropertyId propertyId) {
        this.propertyId = propertyId;
    }

    public GuestRating getGuestRating() {
        return guestRating;
    }

    public void setGuestRating(GuestRating guestRating) {
        this.guestRating = guestRating;
    }

    public List<GuestReview> getGuestReviews() {
        return guestReviews;
    }

    public void setGuestReviews(List<GuestReview> guestReviews) {
        this.guestReviews = guestReviews;
    }
}
