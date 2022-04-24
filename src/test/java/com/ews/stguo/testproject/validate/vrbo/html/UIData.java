package com.ews.stguo.testproject.validate.vrbo.html;


import org.apache.commons.lang3.BooleanUtils;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class UIData {

    // Summary
    private String propertyType;
    private String vrboAvgRating;
    private String referencePriceValue;
    private String referencePriceCurrency;
    private String propertyName;
    private String city;
    private String province;
    private String country;
    private String postalCode;
    private String geoLatitude;
    private String geoLongitude;
    private boolean obfuscated;

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getVrboAvgRating() {
        return vrboAvgRating;
    }

    public void setVrboAvgRating(String vrboAvgRating) {
        this.vrboAvgRating = vrboAvgRating;
    }

    public String getReferencePriceValue() {
        return referencePriceValue;
    }

    public void setReferencePriceValue(String referencePriceValue) {
        this.referencePriceValue = referencePriceValue;
    }

    public String getReferencePriceCurrency() {
        return referencePriceCurrency;
    }

    public void setReferencePriceCurrency(String referencePriceCurrency) {
        this.referencePriceCurrency = referencePriceCurrency;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getGeoLatitude() {
        return geoLatitude;
    }

    public void setGeoLatitude(String geoLatitude) {
        this.geoLatitude = geoLatitude;
    }

    public String getGeoLongitude() {
        return geoLongitude;
    }

    public void setGeoLongitude(String geoLongitude) {
        this.geoLongitude = geoLongitude;
    }

    public boolean isObfuscated() {
        return obfuscated;
    }

    public void setObfuscated(Boolean obfuscated) {
        this.obfuscated = BooleanUtils.isNotFalse(obfuscated);
    }
}
