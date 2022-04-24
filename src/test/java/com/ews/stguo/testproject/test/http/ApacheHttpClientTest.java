package com.ews.stguo.testproject.test.http;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ApacheHttpClientTest {

    @Test
    public void test02() throws Exception {
//        System.setProperty("sun.net.inetaddr.ttl", "20");
//        System.out.println(System.getProperty("stguo.test"));
//        System.out.println(System.getProperty("file.encoding"));
        System.out.println(System.getProperty("sun.net.inetaddr.ttl"));

        PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager();
        clientConnectionManager.setMaxTotal(2);

//        CloseableHttpClient httpClient = HttpClients.createDefault();

        BufferedWriter bw = RWFileUtils.getWriter("httpclient-test.txt");
        for (int i = 0; i < 20; i++) {
            CloseableHttpClient httpClient = getHttpClient(clientConnectionManager);
            bw.write("=========================" + (i + 1) + "=============================");
            bw.newLine();
            bw.flush();
            sendRequestWithSingleClient(bw, httpClient);
//            sendRequestWithClientPerReqeust(bw);
            bw.flush();
            TimeUnit.SECONDS.sleep(5);
        }
        bw.close();
    }

    private CloseableHttpClient getHttpClient(PoolingHttpClientConnectionManager clientConnectionManager) {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        return HttpClients.custom()
                .setConnectionManager(clientConnectionManager)
                .setDefaultRequestConfig(config)
                .build();
    }

    private void sendRequestWithClientPerReqeust(BufferedWriter bw) throws Exception {
        sendRequestWithSingleClient(bw, HttpClients.createDefault());
    }

    private void sendRequestWithSingleClient(BufferedWriter bw, CloseableHttpClient httpClient) throws Exception {
        bw.write(String.valueOf(httpClient.hashCode()));
        bw.newLine();
        bw.flush();
        HttpGet httpGet = new HttpGet("http://steven.bin.top:18843/test/http");
        try(CloseableHttpResponse response = httpClient.execute(httpGet)) {
            printResult(bw, response);
        } catch (Exception e) {
            e.printStackTrace();
            bw.write("Error: " + e.getMessage());
            bw.newLine();
            bw.flush();
        } finally {
            httpGet.releaseConnection();
        }
    }

    private void printResult(BufferedWriter bw, CloseableHttpResponse response) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            bw.write(reader.readLine());
            bw.newLine();
            bw.flush();
        }
    }

}
