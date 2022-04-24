package com.ews.stguo.testproject.test.stream;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class MonoTest {

    @Test
    public void test01() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Mono<String> mono = Mono.fromCallable(() -> {
            System.out.println("Executing...");
            Thread.sleep(5000);
            return "aaa";
        }).publishOn(Schedulers.fromExecutorService(executorService));
        mono.subscribe(str -> {
            System.out.println(LocalDateTime.now() + "_" + Thread.currentThread().getName() + "_" + str);
        });
        System.out.println(LocalDateTime.now() + "_" + Thread.currentThread().getName() + "_bbb");
//        Thread.sleep(10000);
         System.out.println(mono.block());
         System.out.println(mono.block());
         System.out.println(mono.block());
    }

}
