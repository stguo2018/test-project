package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.validate.vrbo.model.vacationrental.VacationRentalModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.BEDTYPES;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHILDRENPERMITTED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HOSTLANGUAGES;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MANAGERNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MANAGERPHOTOURL;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MAXOCCUPANCY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MEASUREMENT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.NUMBEROFBATHROOMS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.NUMBEROFBEDROOMS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.OWNERCHILDRENFREETEXT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.OWNERPARTYFREETEXT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.OWNERPETSFREETEXT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.OWNERSMOKINGFREETEXT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PARTIESOREVENTSPERMITTED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PETSPERMITTED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.SMOKINGPERMITTED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.UNITS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class VacationRentalValidator extends VrboValidator<VacationRentalModel> {

    public VacationRentalValidator(Set<Integer> hotelIds) {
        super(hotelIds);
    }

    @Override
    public String getFileType() {
        return "VacationRental";
    }

    @Override
    public Class<VacationRentalModel> getClazz() {
        return VacationRentalModel.class;
    }

    @Override
    public void analyse(int index, VacationRentalModel model) {

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

        if (model.getPropertySize() != null) {
            if (model.getPropertySize().getMeasurement() > 0) {
                updateCounter(MEASUREMENT);
            }
            if (StringUtils.isNotBlank(model.getPropertySize().getUnits())) {
                updateCounter(UNITS);
            }
        }

        if (model.getMaxOccupancy() != null) {
            updateCounter(MAXOCCUPANCY);
        }

        if (model.getBathrooms() != null && model.getBathrooms().getNumberOfBathrooms() > 0) {
            updateCounter(NUMBEROFBATHROOMS);
        }

        if (model.getBedrooms() != null) {
            if (model.getBedrooms().getNumberOfBedrooms() > 0) {
                updateCounter(NUMBEROFBEDROOMS);
            }
            if (CollectionUtils.isNotEmpty(model.getBedrooms().getBedTypes())) {
                updateCounter(BEDTYPES);
            }
        }

        if (model.getHouseRules() != null) {
            if (model.getHouseRules().getPartyOrEventRules() != null) {
                if (StringUtils.isNotBlank(model.getHouseRules().getPartyOrEventRules().getOwnerPartyFreeText())) {
                    updateCounter(PARTIESOREVENTSPERMITTED);
                    updateCounter(OWNERPARTYFREETEXT);
                }
            }
            if (model.getHouseRules().getSmokingRules() != null) {
                if (StringUtils.isNotBlank(model.getHouseRules().getSmokingRules().getOwnerSmokingFreeText())) {
                    updateCounter(SMOKINGPERMITTED);
                    updateCounter(OWNERSMOKINGFREETEXT);
                }
            }
            if (model.getHouseRules().getPetRules() != null) {
                if (StringUtils.isNotBlank(model.getHouseRules().getPetRules().getOwnerPetsFreeText())) {
                    updateCounter(PETSPERMITTED);
                    updateCounter(OWNERPETSFREETEXT);
                }
            }
            if (model.getHouseRules().getChildRules() != null) {
                if (StringUtils.isNotBlank(model.getHouseRules().getChildRules().getOwnerChildrenFreeText())) {
                    updateCounter(CHILDRENPERMITTED);
                    updateCounter(OWNERCHILDRENFREETEXT);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(model.getHostLanguages())) {
            updateCounter(HOSTLANGUAGES);
        }

        if (model.getPropertyManager() != null) {
            if (StringUtils.isNotBlank(model.getPropertyManager().getName())) {
                updateCounter(MANAGERNAME);
            }
            if (StringUtils.isNotBlank(model.getPropertyManager().getPhotoURL())) {
                updateCounter(MANAGERPHOTOURL);
            }
        }
    }

}
