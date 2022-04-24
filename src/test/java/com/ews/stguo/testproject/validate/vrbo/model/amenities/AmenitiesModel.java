package com.ews.stguo.testproject.validate.vrbo.model.amenities;

import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class AmenitiesModel {

    private PropertyId propertyId;
    private Map<String, List<String>> propertyAmenities;
    private Map<String, List<String>> roomAmenities;
    private Map<String, List<String>> cleanlinessAndSafety;

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(PropertyId propertyId) {
        this.propertyId = propertyId;
    }

    public Map<String, List<String>> getPropertyAmenities() {
        return propertyAmenities;
    }

    public void setPropertyAmenities(Map<String, List<String>> propertyAmenities) {
        this.propertyAmenities = propertyAmenities;
    }

    public Map<String, List<String>> getRoomAmenities() {
        return roomAmenities;
    }

    public void setRoomAmenities(Map<String, List<String>> roomAmenities) {
        this.roomAmenities = roomAmenities;
    }

    public Map<String, List<String>> getCleanlinessAndSafety() {
        return cleanlinessAndSafety;
    }

    public void setCleanlinessAndSafety(Map<String, List<String>> cleanlinessAndSafety) {
        this.cleanlinessAndSafety = cleanlinessAndSafety;
    }
}
