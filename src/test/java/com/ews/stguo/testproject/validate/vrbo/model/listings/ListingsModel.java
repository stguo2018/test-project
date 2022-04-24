package com.ews.stguo.testproject.validate.vrbo.model.listings;

import com.ews.stguo.testproject.validate.vrbo.model.summary.Bookable;
import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;
import com.ews.stguo.testproject.validate.vrbo.model.summary.PropertyType;
import com.ews.stguo.testproject.validate.vrbo.model.summary.ReferencePrice;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ListingsModel {

    private PropertyId propertyId;
    private Bookable bookable;
    private PropertyType propertyType;
    private String inventorySource;
    private String starRating;
    private ReferencePrice referencePrice;
    private ChainAndBrand chainAndBrand;
    private VrboPropertyType vrboPropertyType;

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

    public String getInventorySource() {
        return inventorySource;
    }

    public void setInventorySource(String inventorySource) {
        this.inventorySource = inventorySource;
    }

    public String getStarRating() {
        return starRating;
    }

    public void setStarRating(String starRating) {
        this.starRating = starRating;
    }

    public ReferencePrice getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(ReferencePrice referencePrice) {
        this.referencePrice = referencePrice;
    }

    public ChainAndBrand getChainAndBrand() {
        return chainAndBrand;
    }

    public void setChainAndBrand(ChainAndBrand chainAndBrand) {
        this.chainAndBrand = chainAndBrand;
    }

    public VrboPropertyType getVrboPropertyType() {
        return vrboPropertyType;
    }

    public void setVrboPropertyType(VrboPropertyType vrboPropertyType) {
        this.vrboPropertyType = vrboPropertyType;
    }
}
