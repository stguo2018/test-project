package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.validate.vrbo.model.summary.SummaryModel;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ADDRESS1;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ADDRESS2;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CITY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.COUNTRY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAAVGRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIABOOKABLE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONLATITUDE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONLONGITUDE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONOBFUSCATED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMAVGRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMBOOKABLE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.LASTUPDATED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.POSTALCODE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYTYPEID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYTYPENAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROVINCE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.REFERENCEPRICECURRENCY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.REFERENCEPRICEVALUE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.STARRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOAVGRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOBOOKABLE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class SummaryValidator extends VrboValidator<SummaryModel> {

    public SummaryValidator(Set<Integer> hotelIds) {
        super(hotelIds);
    }

    @Override
    public String getFileType() {
        return "Summary";
    }

    @Override
    public Class<SummaryModel> getClazz() {
        return SummaryModel.class;
    }

    @Override
    public void analyse(int index, SummaryModel model) {
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

        if (model.getBookable() != null) {
            updateCounter(EXPEDIABOOKABLE);
            updateCounter(HCOMBOOKABLE);
            updateCounter(VRBOBOOKABLE);
        }

        if (model.getPropertyType() != null) {
            if (model.getPropertyType().getId() != null) {
                updateCounter(PROPERTYTYPEID);
            }
            if (StringUtils.isNotBlank(model.getPropertyType().getName())) {
                updateCounter(PROPERTYTYPENAME);
            }
        }


        if (model.getGuestRating() != null) {
            if (StringUtils.isNotBlank(model.getGuestRating().getExpedia().getAvgRating()) && !Objects.equals("0", model.getGuestRating().getExpedia().getAvgRating())) {
                updateCounter(EXPEDIAAVGRATING);
            }
            if (StringUtils.isNotBlank(model.getGuestRating().getHcom().getAvgRating()) && !Objects.equals("0", model.getGuestRating().getHcom().getAvgRating())) {
                updateCounter(HCOMAVGRATING);
            }
            if (StringUtils.isNotBlank(model.getGuestRating().getVrbo().getAvgRating()) && !Objects.equals("0", model.getGuestRating().getVrbo().getAvgRating())) {
                updateCounter(VRBOAVGRATING);
            }
        }

        if (model.getReferencePrice() != null) {
            if (StringUtils.isNotBlank(model.getReferencePrice().getValue())) {
                updateCounter(REFERENCEPRICEVALUE);
            }
            if (StringUtils.isNotBlank(model.getReferencePrice().getCurrency())) {
                updateCounter(REFERENCEPRICECURRENCY);
            }
        }

        if (StringUtils.isNotBlank(model.getLastUpdated())) {
            updateCounter(LASTUPDATED);
        }
        if (StringUtils.isNotBlank(model.getPropertyName())) {
            updateCounter(PROPERTYNAME);
        }
        if (StringUtils.isNotBlank(model.getAddress1())) {
            updateCounter(ADDRESS1);
        }
        if (StringUtils.isNotBlank(model.getAddress2())) {
            updateCounter(ADDRESS2);
        }
        if (StringUtils.isNotBlank(model.getCity())) {
            updateCounter(CITY);
        }
        if (StringUtils.isNotBlank(model.getProvince())) {
            updateCounter(PROVINCE);
        }
        if (StringUtils.isNotBlank(model.getCountry())) {
            updateCounter(COUNTRY);
        }
        if (StringUtils.isNotBlank(model.getPostalCode())) {
            updateCounter(POSTALCODE);
        }

        if (model.getGeoLocation() != null) {
            if (StringUtils.isNotBlank(model.getGeoLocation().getLatitude())) {
                updateCounter(GEOLOCATIONLATITUDE);
            }
            if (StringUtils.isNotBlank(model.getGeoLocation().getLongitude())) {
                updateCounter(GEOLOCATIONLONGITUDE);
            }
            updateCounter(GEOLOCATIONOBFUSCATED);
        }

        if (model.getStarRating() != null && StringUtils.isNotBlank(model.getStarRating().getRating())) {
            updateCounter(STARRATING);
        }

    }

}
