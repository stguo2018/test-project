package com.ews.stguo.testproject.utils.client;

import com.expedia.e3.shopsvc.shared.thirdparty.lcs.PropertyContentRoot;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LcsClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static List<PropertyContentRoot> callLcs(String tempUrl, List<String> ids, CloseableHttpClient httpClient) throws Exception {
        String url = String.format(tempUrl, String.join(",", ids));
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User", "EWSLPP");
        httpGet.setHeader("Accept-Encoding", "gzip");
        httpGet.setHeader("request-id", UUID.randomUUID().toString());
        try (CloseableHttpResponse response = httpClient.execute(httpGet);
             BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                     StandardCharsets.UTF_8))) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String inputLine;
                StringBuilder responseData = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    responseData.append(inputLine);
                }

                return MAPPER.readValue(responseData.toString(),
                        new TypeReference<List<PropertyContentRoot>>() {});
            } else if (statusCode == 404) {
                System.out.println("Lcs status code is 404!");
                return Lists.newArrayList();
            } else {
                String inputLine;
                StringBuilder responseData = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    responseData.append(inputLine);
                }
                throw new Exception(responseData.toString());
            }

        }
    }

}
