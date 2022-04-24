package com.ews.stguo.testproject.validate.vrbo.model.summary;

import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class SummaryModel {

    private PropertyId propertyId;
    private Bookable bookable;
    private PropertyType propertyType;
    private GuestRating guestRating;
    private ReferencePrice referencePrice;
    private String lastUpdated;
    private String propertyName;
    private String address1;
    private String address2;
    private String city;
    private String province;
    private String country;
    private String postalCode;
    private GeoLocation geoLocation;
    private String heroImage;
    private StarRating starRating;

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(PropertyId propertyId) {
        this.propertyId = propertyId;
    }

    public Bookable getBookable() {
        return bookable;
    }

    public void setBookable(Bookable bookable) {
        this.bookable = bookable;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public GuestRating getGuestRating() {
        return guestRating;
    }

    public void setGuestRating(GuestRating guestRating) {
        this.guestRating = guestRating;
    }

    public ReferencePrice getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(ReferencePrice referencePrice) {
        this.referencePrice = referencePrice;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
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

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getHeroImage() {
        return heroImage;
    }

    public void setHeroImage(String heroImage) {
        this.heroImage = heroImage;
    }

    public StarRating getStarRating() {
        return starRating;
    }

    public void setStarRating(StarRating starRating) {
        this.starRating = starRating;
    }
}
