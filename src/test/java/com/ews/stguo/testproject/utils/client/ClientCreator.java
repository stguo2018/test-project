package com.ews.stguo.testproject.utils.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ClientCreator {

    private ClientCreator() {

    }

    public static CloseableHttpClient createHttpClient() {
        return createHttpClient(true, 200, 5000, 5000, 25000);
    }

    public static CloseableHttpClient createHttpClient(boolean skipSSH, int maxConnections, int connectTimeout,
                                                int requestTimeout, int socketTimeout) {
        try {
            PoolingHttpClientConnectionManager clientConnectionManager = null;
            if (skipSSH) {
                SSLContext sslContext =
                        SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
                SSLConnectionSocketFactory sslConnectionSocketFactory =
                        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslConnectionSocketFactory)
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .build();
                clientConnectionManager = new PoolingHttpClientConnectionManager(registry);
            } else {
                clientConnectionManager = new PoolingHttpClientConnectionManager();
            }
            clientConnectionManager.setMaxTotal(maxConnections);
            clientConnectionManager.setDefaultMaxPerRoute(maxConnections);

            RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(requestTimeout)
                    .setSocketTimeout(socketTimeout)
                    .build();

            return HttpClients.custom()
                    .setConnectionManager(clientConnectionManager)
                    .setDefaultRequestConfig(config)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WebClient createWebClient() {
        return createWebClient(true, true, 15000, 15000);
    }

    public static WebClient createWebClient(boolean skipSSH, boolean compressionEnabled, int connectTimeout, int requestTimeout) {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(tc -> {
                    tc = tc.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                            .option(ChannelOption.SO_KEEPALIVE, false)
                            .doOnConnected(con ->
                                    con.addHandlerLast(new ReadTimeoutHandler(requestTimeout, TimeUnit.MILLISECONDS)));
                    return tc;
                });
        if (compressionEnabled) {
            httpClient = httpClient.compress(compressionEnabled);
        }
        if (skipSSH) {
            httpClient = httpClient.secure(sslContextSpec -> sslContextSpec
                    .sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)));
        }
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
