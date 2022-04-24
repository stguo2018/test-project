package com.ews.stguo.testproject.validate.vrbo.model.vacationrental;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Bedrooms {

    private int numberOfBedrooms;
    private List<String> bedTypes;

    public int getNumberOfBedrooms() {
        return numberOfBedrooms;
    }

    public void setNumberOfBedrooms(int numberOfBedrooms) {
        this.numberOfBedrooms = numberOfBedrooms;
    }

    public List<String> getBedTypes() {
        return bedTypes;
    }

    public void setBedTypes(List<String> bedTypes) {
        this.bedTypes = bedTypes;
    }
}
