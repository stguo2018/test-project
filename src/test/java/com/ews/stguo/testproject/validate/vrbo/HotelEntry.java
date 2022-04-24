package com.ews.stguo.testproject.validate.vrbo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelEntry {

    @JsonProperty("HotelId")
    private Long hotelID;

    @JsonProperty("Active")
    private boolean active;

    @JsonProperty("Provider")
    private List<Long> provider;

    public Long getHotelID() {
        return hotelID;
    }

    public void setHotelID(Long hotelID) {
        this.hotelID = hotelID;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Long> getProvider() {
        return provider;
    }

    public void setProvider(List<Long> provider) {
        this.provider = provider;
    }
}
