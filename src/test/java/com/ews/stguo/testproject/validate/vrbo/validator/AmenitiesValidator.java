package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.validate.vrbo.model.amenities.AmenitiesModel;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CLEANLINESSANDSAFETY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYAMENITIES;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ROOMAMENITIES;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class AmenitiesValidator extends VrboValidator<AmenitiesModel> {

    public AmenitiesValidator(Set<Integer> hotelIds) {
        super(hotelIds);
    }

    @Override
    public String getFileType() {
        return "Amenities";
    }

    @Override
    public Class<AmenitiesModel> getClazz() {
        return AmenitiesModel.class;
    }

    @Override
    public void analyse(int index, AmenitiesModel model) {
        if (model.getPropertyId() != null) {
            if (StringUtils.isNotBlank(model.getPropertyId().getExpedia())) {
                hotelIds.remove(Integer.parseInt(model.getPropertyId().getExpedia()));
                updateCounter(EXPEDIAID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getHcom())) {
                updateCounter(HCOMID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getVrbo())) {
                updateCounter(VRBOID);
            }
        }

        if (MapUtils.isNotEmpty(model.getPropertyAmenities())) {
            updateCounter(PROPERTYAMENITIES);
        }
        if (MapUtils.isNotEmpty(model.getRoomAmenities())) {
            updateCounter(ROOMAMENITIES);
        }
        if (MapUtils.isNotEmpty(model.getCleanlinessAndSafety())) {
            updateCounter(CLEANLINESSANDSAFETY);
        }


    }
}
