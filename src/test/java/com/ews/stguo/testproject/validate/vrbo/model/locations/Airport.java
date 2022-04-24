package com.ews.stguo.testproject.validate.vrbo.model.locations;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Airport {

    private String id;
    private String code;
    private String name;
    private String distance;
    private String unit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
