package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.validate.vrbo.model.descriptions.DescriptionsModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.AREADESCRIPTION;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYDESCRIPTION;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.RENOVATIONSANDCLOSURE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class DescriptionsValidator extends VrboValidator<DescriptionsModel> {

    private final Map<String, String> mappings = new HashMap<>();
    private BufferedWriter bw;
    private int count = 0;

    public DescriptionsValidator(Set<Integer> hotelIds) {
        super(hotelIds);
        try (BufferedReader br = RWFileUtils.getReader("", "E:/ews-29840/vrboIdMapping3.csv")) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                mappings.put(columns[0], columns[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            bw = RWFileUtils.getWriter("aaa.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFileType() {
        return "Descriptions";
    }

    @Override
    public Class<DescriptionsModel> getClazz() {
        return DescriptionsModel.class;
    }

    @Override
    public void analyse(int index, DescriptionsModel model) {
        if (index == 6 && mappings.containsKey(model.getPropertyId().getExpedia()) && count++ < 20 &&
                StringUtils.isNotBlank(model.getPropertyId().getVrbo()) && StringUtils.isNotBlank(model.getRenovationsAndClosure())) {
            try {
                bw.write(index + "---------" + mappings.get(model.getPropertyId().getExpedia()) + "---------" + model.getPropertyId().getExpedia() + "///" + new ObjectMapper().writeValueAsString(model));
                bw.newLine();
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        if (StringUtils.isNotBlank(model.getAreaDescription())) {
            updateCounter(AREADESCRIPTION);
        }
        if (StringUtils.isNotBlank(model.getPropertyDescription())) {
            updateCounter(PROPERTYDESCRIPTION);
        }
        if (StringUtils.isNotBlank(model.getRenovationsAndClosure())) {
            updateCounter(RENOVATIONSANDCLOSURE);
        }
    }
}
