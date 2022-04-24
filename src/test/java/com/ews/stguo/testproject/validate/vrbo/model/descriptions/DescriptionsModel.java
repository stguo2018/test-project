package com.ews.stguo.testproject.validate.vrbo.model.descriptions;

import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class DescriptionsModel {

    private PropertyId propertyId;
    private String areaDescription;
    private String propertyDescription;
    private String renovationsAndClosure;

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(PropertyId propertyId) {
        this.propertyId = propertyId;
    }

    public String getAreaDescription() {
        return areaDescription;
    }

    public void setAreaDescription(String areaDescription) {
        this.areaDescription = areaDescription;
    }

    public String getPropertyDescription() {
        return propertyDescription;
    }

    public void setPropertyDescription(String propertyDescription) {
        this.propertyDescription = propertyDescription;
    }

    public String getRenovationsAndClosure() {
        return renovationsAndClosure;
    }

    public void setRenovationsAndClosure(String renovationsAndClosure) {
        this.renovationsAndClosure = renovationsAndClosure;
    }
}

