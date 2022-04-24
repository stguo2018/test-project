package com.ews.stguo.testproject.test.feed;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ValidateVacationRentalFeedFile {

    @Test
    public void test01() throws Exception {
        String[] paths = new String[]{"expedia-lodging-1-all.jsonl", "expedia-lodging-2-all.jsonl", "expedia-lodging-3-all.jsonl",
                "expedia-lodging-4-all.jsonl", "expedia-lodging-5-all.jsonl", "expedia-lodging-6-all.jsonl"};
        for (String path : paths) {
            try (BufferedReader reader = RWFileUtils.getReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JSONObject jsonObject = new JSONObject(line);
                    Assert.assertNotNull(jsonObject);
                }
            }
        }
    }

    @Test
    public void test02() throws Exception {
        String str = "{\"a\": 1, \"b\": 2}";
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Tt tt = objectMapper.readValue(str, Tt.class);
        Assert.assertNotNull(tt);
    }

    static class Tt {

        private Integer a;

        public Integer getA() {
            return a;
        }

        public void setA(Integer a) {
            this.a = a;
        }
    }

}
