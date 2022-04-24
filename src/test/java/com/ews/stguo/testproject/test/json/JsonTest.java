package com.ews.stguo.testproject.test.json;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class JsonTest {

    private static final ObjectMapper om = new ObjectMapper();

    @Test
    public void testJson() throws Exception {
        String data;
        try (BufferedReader reader = RWFileUtils.getReader("test.txt")) {
            data  = reader.readLine();
        }
        System.out.println(data);
        Assert.assertNotNull(data);
        class JsonStruct {
            private int id;
            private String data;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getData() {
                return data;
            }

            public void setData(String data) {
                this.data = data;
            }
        }
        JsonStruct jsonStruct = new JsonStruct();
        jsonStruct.setId(1);
        jsonStruct.setData(data);
        String json = om.writeValueAsString(jsonStruct);
        System.out.println(json);
        try (BufferedWriter writer = RWFileUtils.getWriter("text2.txt")) {
            writer.write(json);
            writer.flush();
        }
    }

}
