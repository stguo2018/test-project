package com.ews.stguo.testproject.http;

import com.ews.stguo.testproject.utils.client.ClientCreator;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class WebClientTest {

    @Test
    public void test01() throws Exception {
        WebClient webClient = ClientCreator.createWebClient();
        Mono<String> stringMono = webClient.get().uri("https://localhost:8443/simple/tryGetDelay?delay=200")
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(Exception.class, err -> {
                    System.out.println(LocalDateTime.now() + "---Happened error: " + err.getMessage());
                }).retryBackoff(2, Duration.ofSeconds(2));
        System.out.println(stringMono.block());
    }

}
