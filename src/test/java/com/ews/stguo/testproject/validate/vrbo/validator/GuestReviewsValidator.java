package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.validate.vrbo.model.guestreviews.GuestReviewsModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAAVGRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAREVIEWCOUNT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GUESTREVIEWS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMAVGRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMREVIEWCOUNT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOAVGRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOREVIEWCOUNT;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestReviewsValidator extends VrboValidator<GuestReviewsModel> {

    public GuestReviewsValidator(Set<Integer> hotelIds) {
        super(hotelIds);
    }

    @Override
    protected String getFileNameFormat() {
        return "ews-29840/" + getFileType() + "/expedia-lodging-guestreviews-%d-all.jsonl";
    }

    @Override
    public String getFileType() {
        return "GuestReviews";
    }

    @Override
    public Class<GuestReviewsModel> getClazz() {
        return GuestReviewsModel.class;
    }

    @Override
    public void analyse(int index, GuestReviewsModel model) {
        if (model.getPropertyId() != null) {
            if (StringUtils.isNotBlank(model.getPropertyId().getExpedia())) {
                hotelIds.remove(Integer.parseInt(model.getPropertyId().getExpedia()));
                updateCounter(EXPEDIAID);
            } else {
                System.out.println("a");
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getHcom())) {
                updateCounter(HCOMID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getVrbo())) {
                updateCounter(VRBOID);
            }
        }
        if (model.getGuestRating() != null) {
            if (model.getGuestRating().getExpedia() != null) {
                if (!Objects.equals("0", model.getGuestRating().getExpedia().getAvgRating())) {
                    updateCounter(EXPEDIAAVGRATING);
                }
                if (!Objects.equals("0", model.getGuestRating().getExpedia().getReviewCount())) {
                    updateCounter(EXPEDIAREVIEWCOUNT);
                }
            }
            if (model.getGuestRating().getHcom() != null) {
                if (!Objects.equals("0", model.getGuestRating().getHcom().getAvgRating())) {
                    updateCounter(HCOMAVGRATING);
                }
                if (!Objects.equals("0", model.getGuestRating().getHcom().getReviewCount())) {
                    updateCounter(HCOMREVIEWCOUNT);
                }
            }
            if (model.getGuestRating().getVrbo() != null) {
                if (!Objects.equals("0", model.getGuestRating().getVrbo().getAvgRating())) {
                    updateCounter(VRBOAVGRATING);
                }
                if (!Objects.equals("0", model.getGuestRating().getVrbo().getReviewCount())) {
                    updateCounter(VRBOREVIEWCOUNT);
                }
            }
            if (CollectionUtils.isNotEmpty(model.getGuestReviews())) {
                updateCounter(GUESTREVIEWS);
            }
        }
    }
}
