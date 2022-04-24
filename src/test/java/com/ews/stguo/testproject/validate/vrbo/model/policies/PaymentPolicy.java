package com.ews.stguo.testproject.validate.vrbo.model.policies;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class PaymentPolicy {

    private String localCurrency;
    private List<String> propertyFees;
    private List<String> optionalExtras;

    public String getLocalCurrency() {
        return localCurrency;
    }

    public void setLocalCurrency(String localCurrency) {
        this.localCurrency = localCurrency;
    }

    public List<String> getPropertyFees() {
        return propertyFees;
    }

    public void setPropertyFees(List<String> propertyFees) {
        this.propertyFees = propertyFees;
    }

    public List<String> getOptionalExtras() {
        return optionalExtras;
    }

    public void setOptionalExtras(List<String> optionalExtras) {
        this.optionalExtras = optionalExtras;
    }
}
