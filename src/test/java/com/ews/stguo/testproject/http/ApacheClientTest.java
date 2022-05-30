package com.ews.stguo.testproject.http;

import com.ews.stguo.testproject.utils.client.ClientCreator;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ApacheClientTest {

    @Test
    public void test01() throws Exception {
        CloseableHttpClient httpClient = ClientCreator.createHttpClient(true, 1, 5000, 5000, 5000);
        String url = "https://localhost:8443/simple/tryGetDelay?delay=200";
        for (int i = 0; i < 10; i++) {
            try (CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                    String line = br.readLine();
                    System.out.println(line);
                }
                Thread.sleep(7000);
            }
        }
    }

}
