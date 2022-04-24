package com.ews.stguo.testproject.utils.client;

import com.expedia.www.ews.models.propertyinfo.v2.request.PropertyInfoRequest;
import com.expedia.www.ews.models.propertyinfo.v2.response.PropertyInfoResponseType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ContentProviderClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private ContentProviderClient() {

    }

    public static PropertyInfoResponseType getPropertyInfoV2(String url, List<String> ids, CloseableHttpClient httpClient) throws Exception {
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        PropertyInfoRequest request = new PropertyInfoRequest();
        request.setIds(ids);
        ByteArrayEntity entity = new ByteArrayEntity(MAPPER.writeValueAsBytes(request));
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Accept-Encoding", "gzip, deflate");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpClient.execute(httpPost);
             BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                     StandardCharsets.UTF_8))) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String inputLine;
                StringBuilder responseData = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    responseData.append(inputLine);
                }

                return MAPPER.readValue(responseData.toString(), PropertyInfoResponseType.class);
            } else if (statusCode == 404) {
                System.out.println("ContentProvider status code is 404!");
                return null;
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
