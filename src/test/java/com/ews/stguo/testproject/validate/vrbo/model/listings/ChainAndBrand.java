package com.ews.stguo.testproject.validate.vrbo.model.listings;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ChainAndBrand {

    private Integer chainId;
    private String chainName;
    private Integer brandId;
    private String brandName;

    public Integer getChainId() {
        return chainId;
    }

    public void setChainId(Integer chainId) {
        this.chainId = chainId;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
}
