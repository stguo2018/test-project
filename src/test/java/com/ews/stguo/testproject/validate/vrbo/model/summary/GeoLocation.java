package com.ews.stguo.testproject.validate.vrbo.model.summary;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GeoLocation {

    private String latitude;
    private String longitude;
    private boolean obfuscated;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isObfuscated() {
        return obfuscated;
    }

    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }
}
