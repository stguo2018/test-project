package com.ews.stguo.testproject.validate.vrbo.model.summary;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Bookable {

    private boolean expedia;
    private boolean hcom;
    private boolean vrbo;

    public boolean isExpedia() {
        return expedia;
    }

    public void setExpedia(boolean expedia) {
        this.expedia = expedia;
    }

    public boolean isHcom() {
        return hcom;
    }

    public void setHcom(boolean hcom) {
        this.hcom = hcom;
    }

    public boolean isVrbo() {
        return vrbo;
    }

    public void setVrbo(boolean vrbo) {
        this.vrbo = vrbo;
    }
}
