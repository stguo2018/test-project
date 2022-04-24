package com.ews.stguo.testproject.validate.vrbo.model.locations;

import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;
import com.ews.stguo.testproject.validate.vrbo.model.summary.GeoLocation;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LocationsModel {

    private PropertyId propertyId;
    private String propertyName;
    private String address1;
    private String address2;
    private String city;
    private String province;
    private String country;
    private String postalCode;
    private List<Phone> phone;
    private GeoLocation geoLocation;
    private LocationAttribute locationAttribute;

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(PropertyId propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
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

    public List<Phone> getPhone() {
        return phone;
    }

    public void setPhone(List<Phone> phone) {
        this.phone = phone;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public LocationAttribute getLocationAttribute() {
        return locationAttribute;
    }

    public void setLocationAttribute(LocationAttribute locationAttribute) {
        this.locationAttribute = locationAttribute;
    }
}
