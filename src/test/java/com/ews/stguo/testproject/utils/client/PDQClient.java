package com.ews.stguo.testproject.utils.client;

import com.ews.stguo.testproject.validate.vrbo.generator.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class PDQClient {

    private static final String URL = "http://property-directory-query-production.us-east-1-vpc-d9087bbe." +
            "slb-internal.prod.aws.away.black/supply/property-directory/v2/properties";
    private static final String TOKEN = "Bearer NTZlNjg1OTItMDA2ZC00ZDg0LWExMTctOTIxMmU0ZjdhM2M3";

    public static Map<String, String> getVrboIdMappingFromPDQ(List<String> hotelIds, CloseableHttpClient httpClient) {
        HttpPost httpPost = new HttpPost(URL);
        httpPost.setHeader("Authorization", TOKEN);
        httpPost.setHeader("Content-Type", "application/json");
        String json = buildRequestBody(hotelIds).toString();
        httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        Map<String, String> map = new HashMap<>();
        try (CloseableHttpResponse response = httpClient.execute(httpPost);
             BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String inputLine;
                StringBuilder responseData = new StringBuilder();
                while ((inputLine = br.readLine()) != null) {
                    responseData.append(inputLine);
                }
                map.putAll(handResponse(responseData.toString()));
            } else {
                System.out.println("Error: " + statusCode);
            }
        } catch (JSONException e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            System.out.println("Failed to parse PDQ response." + stringWriter.toString());
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            System.out.println("Failed to call PDQ." + stringWriter.toString());
        }
        return map;
    }

    private static RequestBody buildRequestBody(List<String> hotelIds) {
        RequestBody requestBody = new RequestBody();
        requestBody.setIds(hotelIds);
        requestBody.setIdType("lodgingDirectoryId");
        return requestBody;
    }

    private static Map<String, String> handResponse(String responseStr) throws JSONException {
        JSONObject responseObj = new JSONObject(responseStr);
        JSONArray results = responseObj.optJSONArray("results");
        if (results.length() <=0) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.optJSONObject(i);
            String hotelId = Optional.ofNullable(result).map(r -> r.optString("id")).orElse("");
            Optional<JSONArray> propertyUnits = Optional.ofNullable(result).map(r -> r.optJSONArray("propertyUnits"));
            if (StringUtils.isNotBlank(hotelId) && propertyUnits.isPresent()) {
                if (propertyUnits.get().length() <=0) {
                    return null;
                }
                if (propertyUnits.get().length() > 1) {
//                    System.out.println(hotelId);
                }
                JSONObject propertyUnit = propertyUnits.get().optJSONObject(0);
                Optional<JSONObject> property = Optional.ofNullable(propertyUnit).map(pu -> pu.optJSONObject("property"));
                Optional<JSONObject> homeAwayId = property.map(p -> p.optJSONObject("homeAwayId"));
                Optional<JSONObject> unit = Optional.ofNullable(propertyUnit).map(pu -> pu.optJSONObject("unit"));
                Optional<JSONObject> unitHomeAwayId = unit.map(u -> u.optJSONObject("homeAwayId"));
                String listingNamespace = homeAwayId.map(h -> h.optString("listingNamespace")).orElse("");
                String unitUrl = unitHomeAwayId.map(uha -> uha.optString("url")).orElse("");
                String listingTriad = unit.map(u -> u.optString("homeAwayTriad")).orElse("");
                if (StringUtils.isNotBlank(listingNamespace) && StringUtils.isNotBlank(unitUrl) && StringUtils.isNotBlank(listingTriad)) {
                    map.put(hotelId, listingTriad);
                }
            }
        }
        return map;
    }

}
