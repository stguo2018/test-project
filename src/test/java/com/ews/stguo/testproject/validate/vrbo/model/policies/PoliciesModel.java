package com.ews.stguo.testproject.validate.vrbo.model.policies;

import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class PoliciesModel {

    private PropertyId propertyId;
    private String checkInStartTime;
    private String checkInEndTime;
    private String checkOutTime;
    private List<String> checkInPolicy;
    private List<String> checkOutPolicy;
    private String minimumAge;
    private List<String> petPolicy;
    private List<String> childrenAndExtraBedPolicy;
    private List<String> checkInInstructions;
    private List<String> specialInstructions;
    private List<String> knowBeforeYouGo;
    private PaymentPolicy paymentPolicy;
    private List<String> formsOfPayment;

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(PropertyId propertyId) {
        this.propertyId = propertyId;
    }

    public String getCheckInStartTime() {
        return checkInStartTime;
    }

    public void setCheckInStartTime(String checkInStartTime) {
        this.checkInStartTime = checkInStartTime;
    }

    public String getCheckInEndTime() {
        return checkInEndTime;
    }

    public void setCheckInEndTime(String checkInEndTime) {
        this.checkInEndTime = checkInEndTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public List<String> getCheckInPolicy() {
        return checkInPolicy;
    }

    public void setCheckInPolicy(List<String> checkInPolicy) {
        this.checkInPolicy = checkInPolicy;
    }

    public List<String> getCheckOutPolicy() {
        return checkOutPolicy;
    }

    public void setCheckOutPolicy(List<String> checkOutPolicy) {
        this.checkOutPolicy = checkOutPolicy;
    }

    public String getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(String minimumAge) {
        this.minimumAge = minimumAge;
    }

    public List<String> getPetPolicy() {
        return petPolicy;
    }

    public void setPetPolicy(List<String> petPolicy) {
        this.petPolicy = petPolicy;
    }

    public List<String> getChildrenAndExtraBedPolicy() {
        return childrenAndExtraBedPolicy;
    }

    public void setChildrenAndExtraBedPolicy(List<String> childrenAndExtraBedPolicy) {
        this.childrenAndExtraBedPolicy = childrenAndExtraBedPolicy;
    }

    public List<String> getCheckInInstructions() {
        return checkInInstructions;
    }

    public void setCheckInInstructions(List<String> checkInInstructions) {
        this.checkInInstructions = checkInInstructions;
    }

    public List<String> getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(List<String> specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public List<String> getKnowBeforeYouGo() {
        return knowBeforeYouGo;
    }

    public void setKnowBeforeYouGo(List<String> knowBeforeYouGo) {
        this.knowBeforeYouGo = knowBeforeYouGo;
    }

    public PaymentPolicy getPaymentPolicy() {
        return paymentPolicy;
    }

    public void setPaymentPolicy(PaymentPolicy paymentPolicy) {
        this.paymentPolicy = paymentPolicy;
    }

    public List<String> getFormsOfPayment() {
        return formsOfPayment;
    }

    public void setFormsOfPayment(List<String> formsOfPayment) {
        this.formsOfPayment = formsOfPayment;
    }
}
