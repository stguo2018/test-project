package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.validate.vrbo.model.policies.PoliciesModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHECKINENDTIME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHECKININSTRUCTIONS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHECKINPOLICY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHECKINSTARTTIME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHECKOUTPOLICY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHECKOUTTIME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHILDRENANDEXTRABEDPOLICY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.FORMSOFPAYMENT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.KNOWBEFOREYOUGO;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MINIMUMAGE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PAYMENTPOLICYLOCALCURRENCY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PAYMENTPOLICYOPTIONALEXTRAS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PAYMENTPOLICYPROPERTYFEES;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PETPOLICY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.SPECIALINSTRUCTIONS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class PoliciesValidator extends VrboValidator<PoliciesModel> {

    public PoliciesValidator(Set<Integer> hotelIds) {
        super(hotelIds);
    }

    @Override
    public String getFileType() {
        return "Policies";
    }

    @Override
    public Class<PoliciesModel> getClazz() {
        return PoliciesModel.class;
    }

    @Override
    public void analyse(int index, PoliciesModel model) {
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
        if (StringUtils.isNotBlank(model.getCheckInStartTime())) {
            updateCounter(CHECKINSTARTTIME);
        }
        if (StringUtils.isNotBlank(model.getCheckInEndTime())) {
            updateCounter(CHECKINENDTIME);
        }
        if (StringUtils.isNotBlank(model.getCheckOutTime())) {
            updateCounter(CHECKOUTTIME);
        }
        if (CollectionUtils.isNotEmpty(model.getCheckInPolicy())) {
            updateCounter(CHECKINPOLICY);
        }
        if (CollectionUtils.isNotEmpty(model.getCheckOutPolicy())) {
            updateCounter(CHECKOUTPOLICY);
        }
        if (StringUtils.isNotBlank(model.getMinimumAge())) {
            updateCounter(MINIMUMAGE);
        }
        if (CollectionUtils.isNotEmpty(model.getPetPolicy())) {
            updateCounter(PETPOLICY);
        }
        if (CollectionUtils.isNotEmpty(model.getChildrenAndExtraBedPolicy())) {
            updateCounter(CHILDRENANDEXTRABEDPOLICY);
        }
        if (CollectionUtils.isNotEmpty(model.getCheckInInstructions())) {
            updateCounter(CHECKININSTRUCTIONS);
        }
        if (CollectionUtils.isNotEmpty(model.getSpecialInstructions())) {
            updateCounter(SPECIALINSTRUCTIONS);
        }
        if (CollectionUtils.isNotEmpty(model.getKnowBeforeYouGo())) {
            updateCounter(KNOWBEFOREYOUGO);
        }
        if (model.getPaymentPolicy() != null) {
            if (StringUtils.isNotBlank(model.getPaymentPolicy().getLocalCurrency())) {
                updateCounter(PAYMENTPOLICYLOCALCURRENCY);
            }
            if (CollectionUtils.isNotEmpty(model.getPaymentPolicy().getPropertyFees())) {
                updateCounter(PAYMENTPOLICYPROPERTYFEES);
            }
            if (CollectionUtils.isNotEmpty(model.getPaymentPolicy().getOptionalExtras())) {
                updateCounter(PAYMENTPOLICYOPTIONALEXTRAS);
            }
        }
        if (CollectionUtils.isNotEmpty(model.getFormsOfPayment())) {
            updateCounter(FORMSOFPAYMENT);
        }
    }
}
