package com.ews.stguo.testproject.validate.vrbo.model.images;

import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ImagesModel {

    private PropertyId propertyId;
    private Thumbnail thumbnail;
    private Hero hero;
    private Map<String, List<Images>> images;

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(PropertyId propertyId) {
        this.propertyId = propertyId;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Hero getHero() {
        return hero;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }

    public Map<String, List<Images>> getImages() {
        return images;
    }

    public void setImages(Map<String, List<Images>> images) {
        this.images = images;
    }
}
