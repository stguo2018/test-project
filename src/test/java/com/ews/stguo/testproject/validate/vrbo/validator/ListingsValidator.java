package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.validate.vrbo.model.listings.ListingsModel;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.BRANDID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.BRANDNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHAINID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHAINNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIABOOKABLE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMBOOKABLE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.INSTANTBOOK;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.INVENTORYSOURCE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYTYPEID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYTYPENAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.QUOTEANDHOLD;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.REFERENCEPRICECURRENCY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.REFERENCEPRICEVALUE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.STARRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOBOOKABLE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ListingsValidator extends VrboValidator<ListingsModel> {

    public ListingsValidator(Set<Integer> hotelIds) {
        super(hotelIds);
    }

    @Override
    public String getFileType() {
        return "Listings";
    }

    @Override
    public Class<ListingsModel> getClazz() {
        return ListingsModel.class;
    }

    @Override
    public void analyse(int index, ListingsModel model) {
        if (model.getPropertyId() != null) {
            if (StringUtils.isNotBlank(model.getPropertyId().getExpedia())) {
                hotelIds.remove(Integer.parseInt(model.getPropertyId().getExpedia()));
                updateCounter(EXPEDIAID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getHcom())) {
                updateCounter(HCOMID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getVrbo())) {
                updateCounter(VRBOID);
            }
        }

        if (model.getBookable() != null) {
            updateCounter(EXPEDIABOOKABLE);
            updateCounter(HCOMBOOKABLE);
            updateCounter(VRBOBOOKABLE);
        }

        if (model.getPropertyType() != null) {
            if (model.getPropertyType().getId() != null) {
                updateCounter(PROPERTYTYPEID);
            }
            if (StringUtils.isNotBlank(model.getPropertyType().getName())) {
                updateCounter(PROPERTYTYPENAME);
            }
        }

        if (StringUtils.isNotBlank(model.getInventorySource())) {
            updateCounter(INVENTORYSOURCE);
        }
        if (StringUtils.isNotBlank(model.getStarRating())) {
            updateCounter(STARRATING);
        }

        if (model.getReferencePrice() != null) {
            if (StringUtils.isNotBlank(model.getReferencePrice().getValue())) {
                updateCounter(REFERENCEPRICEVALUE);
            }
            if (StringUtils.isNotBlank(model.getReferencePrice().getCurrency())) {
                updateCounter(REFERENCEPRICECURRENCY);
            }
        }

        if (model.getChainAndBrand() != null) {
            if (model.getChainAndBrand().getChainId() != null) {
                updateCounter(CHAINID);
            }
            if (StringUtils.isNotBlank(model.getChainAndBrand().getChainName())) {
                updateCounter(CHAINNAME);
            }
            if (model.getChainAndBrand().getBrandId() != null) {
                updateCounter(BRANDID);
            }
            if (StringUtils.isNotBlank(model.getChainAndBrand().getBrandName())) {
                updateCounter(BRANDNAME);
            }
        }

        if (model.getVrboPropertyType() != null) {
            if (model.getVrboPropertyType().getInstantBook() != null) {
                updateCounter(INSTANTBOOK);
            }
            if (model.getVrboPropertyType().getQuoteAndHold() != null) {
                updateCounter(QUOTEANDHOLD);
            }
        }
    }

}
