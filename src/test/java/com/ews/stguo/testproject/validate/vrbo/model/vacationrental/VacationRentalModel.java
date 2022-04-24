package com.ews.stguo.testproject.validate.vrbo.model.vacationrental;

import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class VacationRentalModel {

    private PropertyId propertyId;
    private PropertySize propertySize;
    private Integer maxOccupancy;
    private Bathrooms bathrooms;
    private Bedrooms bedrooms;
    private HouseRules houseRules;
    private List<String> hostLanguages;
    private PropertyManager propertyManager;

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(PropertyId propertyId) {
        this.propertyId = propertyId;
    }

    public PropertySize getPropertySize() {
        return propertySize;
    }

    public void setPropertySize(PropertySize propertySize) {
        this.propertySize = propertySize;
    }

    public Integer getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(Integer maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public Bathrooms getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Bathrooms bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Bedrooms getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Bedrooms bedrooms) {
        this.bedrooms = bedrooms;
    }

    public HouseRules getHouseRules() {
        return houseRules;
    }

    public void setHouseRules(HouseRules houseRules) {
        this.houseRules = houseRules;
    }

    public List<String> getHostLanguages() {
        return hostLanguages;
    }

    public void setHostLanguages(List<String> hostLanguages) {
        this.hostLanguages = hostLanguages;
    }

    public PropertyManager getPropertyManager() {
        return propertyManager;
    }

    public void setPropertyManager(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }
}
