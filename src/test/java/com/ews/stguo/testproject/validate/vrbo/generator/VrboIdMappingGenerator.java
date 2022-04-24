package com.ews.stguo.testproject.validate.vrbo.generator;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class VrboIdMappingGenerator {

    private static final String URL = "https://3pispy.prod.lodgingshared.expedia.com/pdq/property?idType=lodgingDirectoryId&id=%d";
    private static final String URL2 = "http://property-directory-query-production.us-east-1-vpc-d9087bbe.slb-internal.prod.aws.away.black/supply/property-directory/v2/properties";
    private static final String TOKEN = "JWT-Bearer eyJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJhcHBsaWNhdGlvbiIsInN1YiI6InhhcGktcHJvcGVydHktZGlyZWN0b3J5I" +
            "iwic2NwIjoiSU5URVJOQUwsT0FVVEhfRU5EUE9JTlQsSU5URVJOQUxfSE9NRUFXQVksSU5URVJOQUxfRElTVFJJQlVUSU9OLE1QU19TQ09SSU5HLERJU" +
            "kVDVE9SWV9TVkNfUFJPUEVSVFksRElSRUNUT1JZX1NWQ19TVVBQTElFUixSVU5USU1FX1NZU1RFTV9DT05GSUcsUFJPUEVSVFlfU0hFTExfQ1JFQVRFL" +
            "FBST1BFUlRZX1NIRUxMX1VQREFURSxQUk9QRVJUWV9ESVJFQ1RPUllfQ09NTUFORF9FWEVDIiwiaXNzIjoiYXBpLWF1dGgtdjIiLCJleHAiOjE2MzEyMTU" +
            "yMTQsImlhdCI6MTYzMDYxMDQxNCwianRpIjoiMGMwZjY5MWYtOTFkNC00MGY0LTliMmEtY2JlYWUzM2UwOTI1IiwiY2lkIjoiODNlMTRiNTMtZDhiNC00MTg4" +
            "LWE2NjQtM2MwZjVlNDllMDlmIn0.mMKLREslUkF8URdJKL2FSZYgIIMh8hyN1d5KOb3gxNVYnOfMMvpkhBbkLoCtATSMXdF_Yh1OUdF1Wyp2bGBIs1YghRxfLqjM" +
            "kL1Gm5rK8Q8Loes90SBj-0Ym5b2VcpHByaQXSh9GkTBw2NgGhViT8tBQc8xU6SmnYYeLLfMYt7Sw-DWhA75xW7EynUcYxiqT78XxVd35Tl1giXTvhaWPS96Ftuc7pR" +
            "0QVWcwmQxZI5qh0cnC5QyH1qRqtucfpCs1HfhbIRaYMXs-2eIrsVMRr7IU0DMBwudM97wLxLEtoQNG8dRVBBErIoGW-JUjJXZ9hLnyX5VCQ3AGacrZvTnz4w";
    private final CloseableHttpClient httpClient;

    private int totalSize;
    private int currentSize;
    private long startTime;

    public VrboIdMappingGenerator() throws Exception {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager(registry);
        clientConnectionManager.setMaxTotal(100);

        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();

        httpClient = HttpClients.custom()
                .setConnectionManager(clientConnectionManager)
                .setDefaultRequestConfig(config)
                .build();
    }

    public void generate(List<Integer> hotelIds) throws Exception {
        totalSize = hotelIds.size();
        currentSize = 0;
        startTime = System.currentTimeMillis();
        final ExecutorService executors = Executors.newFixedThreadPool(10);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this::calculateProcess, 5, 5, TimeUnit.SECONDS);
        RateLimiter rateLimiter = RateLimiter.create(10);
        System.out.println("Start generating mapping file...");
        BlockingQueue<String> queue = new LinkedBlockingDeque<>(1000000);
        int batchSize = (hotelIds.size() + 10) / 10 ;
        List<List<Integer>> partitions1 = Lists.partition(hotelIds, batchSize);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<Integer> partition1 : partitions1) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    List<List<Integer>> partitions2 = Lists.partition(partition1, Math.min(100, partition1.size()));
                    for (List<Integer> ids : partitions2) {
                        rateLimiter.acquire();
                        List<String> dateLines = getDateLines(ids);
                        if (Optional.ofNullable(dateLines).isPresent()) {
                            for (String line : dateLines) {
                                queue.put(line);
                            }
                        }
                        increase(ids.size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        Thread t1 = new Thread(() -> {
            try (BufferedWriter bw = RWFileUtils.getWriter("vrboIdMapping.csv")) {
                int count = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    String line = queue.take();
                    bw.write(line);
                    bw.newLine();
                    if (count++ % 100 == 0) {
                        bw.flush();
                    }
                }
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t1.start();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        executors.shutdown();
        TimeUnit.SECONDS.sleep(10);
        t1.interrupt();
        scheduledExecutorService.shutdown();
        currentSize = totalSize;
        calculateProcess();
    }

    private synchronized void increase(int size) {
        currentSize += size;
    }

    private void calculateProcess() {
        double rate = (double) currentSize / totalSize;
        BigDecimal usedTime = new BigDecimal((System.currentTimeMillis() - startTime) / 1000);
        double maxRate = Math.max(rate, 0.0001);
        int estimatedFinishTime = usedTime.divide(new BigDecimal(maxRate), 6, RoundingMode.HALF_EVEN).intValue() - usedTime.intValue();
        int minutes = estimatedFinishTime / 60;
        int seconds = estimatedFinishTime % 60;
        String time = String.format("%dmin:%dsec", minutes, seconds);
        System.out.println(String.format("(%d/%d)%.2f%%, Used time: %dsec, Estimated finish time: %s", currentSize, totalSize, rate * 100, usedTime.intValue(), time));
    }

    private List<String> getDateLines(List<Integer> hotelIds) {
        HttpPost httpPost = new HttpPost(URL2);
        httpPost.setHeader("authorization", TOKEN);
        httpPost.setHeader("Content-Type", "application/json");
        String json = buildRequestBody(hotelIds).toString();
        httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(httpPost);
             BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String inputLine;
                StringBuilder responseData = new StringBuilder();
                while ((inputLine = br.readLine()) != null) {
                    responseData.append(inputLine);
                }
                return handResponse(responseData.toString());
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
        return null;
    }

    private RequestBody buildRequestBody(List<Integer> hotelIds) {
        RequestBody requestBody = new RequestBody();
        requestBody.setIds(hotelIds.stream().map(String::valueOf).collect(Collectors.toList()));
        requestBody.setIdType("lodgingDirectoryId");
        return requestBody;
    }

    private List<String> getDateLines(Integer hotelId) {
        HttpGet httpGet = new HttpGet(String.format(URL, hotelId));
        try (CloseableHttpResponse response = httpClient.execute(httpGet);
             BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String inputLine;
                StringBuilder responseData = new StringBuilder();
                while ((inputLine = br.readLine()) != null) {
                    responseData.append(inputLine);
                }
                return handResponse(hotelId, responseData.toString());
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
        return null;
    }

    private List<String> handResponse(String responseStr) throws JSONException {
        JSONObject responseObj = new JSONObject(responseStr);
        JSONArray results = responseObj.optJSONArray("results");
        if (results.length() <=0) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.optJSONObject(i);
            String id = Optional.ofNullable(result).map(r -> r.optString("id")).orElse("");
            Optional<JSONArray> propertyUnits = Optional.ofNullable(result).map(r -> r.optJSONArray("propertyUnits"));
            if (StringUtils.isNotBlank(id) && propertyUnits.isPresent()) {
                String rs = handResponse(id, propertyUnits.get());
                if (StringUtils.isNotBlank(rs)) {
                    lines.add(rs);
                }
            }
        }
        return lines;
    }

    private String handResponse(String hotelId, JSONArray propertyUnits) {
        if (propertyUnits.length() <=0) {
            return null;
        }
        if (propertyUnits.length() > 1) {
            System.out.println(hotelId);
        }
        JSONObject propertyUnit = propertyUnits.optJSONObject(0);
        Optional<JSONObject> property = Optional.ofNullable(propertyUnit).map(pu -> pu.optJSONObject("property"));
        Optional<JSONObject> homeAwayId = property.map(p -> p.optJSONObject("homeAwayId"));
        Optional<JSONObject> unit = Optional.ofNullable(propertyUnit).map(pu -> pu.optJSONObject("unit"));
        Optional<JSONObject> unitHomeAwayId = unit.map(u -> u.optJSONObject("homeAwayId"));
        String listingNamespace = homeAwayId.map(h -> h.optString("listingNamespace")).orElse("");
        String unitUrl = unitHomeAwayId.map(uha -> uha.optString("url")).orElse("");
        String listingTriad = unit.map(u -> u.optString("homeAwayTriad")).orElse("");
        if (StringUtils.isNotBlank(listingNamespace) && StringUtils.isNotBlank(unitUrl) && StringUtils.isNotBlank(listingTriad)) {
            return String.format("%s,%s,%s,%s", hotelId, unitUrl, listingTriad, listingNamespace);
        }
        return null;
    }

    private List<String> handResponse(Integer hotelId, String responseStr) throws JSONException {
        JSONObject responseObj = new JSONObject(responseStr);
        JSONArray pdqResponse = responseObj.optJSONArray("pdqResponse");
        if (pdqResponse.length() <=0) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < pdqResponse.length(); i++) {
            JSONObject pdq = pdqResponse.optJSONObject(i);
            Optional<JSONObject> unit = Optional.ofNullable(pdq).map(p -> p.optJSONObject("unit"));
            Optional<JSONObject> homeAwayId = unit.map(u -> u.optJSONObject("homeAwayId"));
            String unitUrl = homeAwayId.map(h -> h.optString("url")).orElse("");
            String listingTriad = unit.map(u -> u.optString("homeAwayTriad")).orElse("");
            if (StringUtils.isNotBlank(unitUrl) && StringUtils.isNotBlank(listingTriad)) {
                lines.add(String.format("%d,%s,%s", hotelId, unitUrl, listingTriad));
            }
        }
        return lines;
    }

}
