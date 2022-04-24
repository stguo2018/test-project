package com.ews.stguo.testproject.validate.vrbo.generator;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class RequestBody {

    private static final ObjectMapper OM = new ObjectMapper();

    private List<String> ids;
    private String idType;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    @Override
    public String toString() {
        try {
            return OM.writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
