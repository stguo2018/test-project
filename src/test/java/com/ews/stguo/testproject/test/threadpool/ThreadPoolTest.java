package com.ews.stguo.testproject.test.threadpool;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.ews.stguo.testproject.utils.client.ClientCreator;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ThreadPoolTest {

    @Test
    public void test01() throws Exception {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            futures.addAll(exe());
        }
        Assert.assertNotNull(futures);
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
        allFutures.thenRun(() -> System.out.println("done"));
        System.out.println("======================");
        allFutures.get();
        TimeUnit.SECONDS.sleep(555);
    }

    private List<CompletableFuture<Void>> exe() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 10; j++) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName());
                }
            }, executorService));
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
//        allFutures.thenRun(executorService::shutdown);
        return futures;
    }

    @Test
    public void test02() throws Exception {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            futures.add(CompletableFuture.runAsync(this::t));
        }
        Assert.assertNotNull(futures);
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
        allFuture.get();
        System.out.println("Done");
    }

    private void t() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        List<C> cs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            C c = new C();
            cs.add(c);
            final Object lock = new Object();
            for (int j = 0; j < 10; j++) {
                futures.add(print("name-" + i, lock, executorService, c));
            }
        }
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
        allFuture.thenRun(executorService::shutdown);
        try {
            allFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (C c : cs) {
            System.out.println("C:" + c.getA());
        }
    }

    class C {
        private int a = 0;

        public void add() {
            a = a + 1;
        }

        public int getA() {
            return this.a;
        }
    }

    private CompletableFuture<Void> print(String name, final Object lock, ExecutorService executorService, C c) {
        return CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 1000; i++) {
                synchronized (lock) {
//                    try {
//                        TimeUnit.SECONDS.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println(LocalDateTime.now() + ":" + name + "-" + i);
                    c.add();
                }
            }
        }, executorService);
    }

    @Test
    public void test03() throws Exception {
        System.out.println("CPU:" + Runtime.getRuntime().availableProcessors());
        AtomicInteger atomicInteger = new AtomicInteger(0);
        RateLimiter rateLimiter = RateLimiter.create(40);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Object o = new Object();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            synchronized (o) {
                System.out.println("ForkJoinCoommonPoolSize:" + ForkJoinPool.commonPool().getPoolSize() + "--TPS:" + atomicInteger.get());
                atomicInteger.set(0);
            }
        }, 0, 1, TimeUnit.SECONDS);
        Set<String> set = new ConcurrentHashSet<>();
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        ExecutorService executorService2 = Executors.newFixedThreadPool(100);
        CloseableHttpClient httpClient = ClientCreator.createHttpClient();
        WebClient webClient = ClientCreator.createWebClient();
        String url = "https://localhost:8443/simple/tryGetDelay?delay=2000";
        for (int k = 0; k < 10000; k++) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                futures.add(CompletableFuture.runAsync(() -> {
                    String threadName = Thread.currentThread().getName();
                    if (!set.contains(threadName)) {
                        set.add(threadName);
                        System.out.println(LocalDateTime.now() + ":::::" + threadName + "(" + set.size() + ")");
                    } else {
//                        System.out.println("Repeat1:" + LocalDateTime.now() + ":::::" + threadName);
                    }
//                    Mono<String> responseMono = simulateCallAPIWithDelay2(httpClient, webClient, url, set, rateLimiter, o, atomicInteger);
//                    Mono<String> responseMono2 = simulateCallAPIWithDelay2(httpClient, webClient, url, set, rateLimiter, o, atomicInteger);
//                    responseMono.block();
//                    responseMono2.block();
                    CompletableFuture<Void> f1 = CompletableFuture.runAsync(() ->
                            simulateCallAPIWithDelay(httpClient, webClient, url, set, rateLimiter, o, atomicInteger), executorService2);
                    CompletableFuture<Void> f2 = CompletableFuture.runAsync(() ->
                            simulateCallAPIWithDelay(httpClient, webClient, url, set, rateLimiter, o, atomicInteger), executorService2);
                    try {
                        CompletableFuture.allOf(f1, f2).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }/*, executorService*/));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get();
        }
    }

    private void simulateCallAPIWithDelay(CloseableHttpClient httpClient, WebClient webClient, String url, Set<String> set,
                                          RateLimiter rateLimiter, Object o, AtomicInteger atomicInteger) {
        String threadName = Thread.currentThread().getName();
        if (!set.contains(threadName)) {
            set.add(threadName);
            System.out.println(LocalDateTime.now() + ":::::" + threadName + "(" + set.size() + ")");
        } else {
            // System.out.println("Repeat2:" + LocalDateTime.now() + ":::::" + threadName2);
        }
        rateLimiter.acquire();
        synchronized (o) {
            atomicInteger.incrementAndGet();
        }
//        callAPIWithHttpClient(url, httpClient, atomicInteger);
        callAPIWebClient(url, webClient);
//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private Mono<String> simulateCallAPIWithDelay2(CloseableHttpClient httpClient, WebClient webClient, String url, Set<String> set,
                                           RateLimiter rateLimiter, Object o, AtomicInteger atomicInteger) {
        String threadName = Thread.currentThread().getName();
        if (!set.contains(threadName)) {
            set.add(threadName);
            System.out.println(LocalDateTime.now() + ":::::" + threadName + "(" + set.size() + ")");
        } else {
            // System.out.println("Repeat2:" + LocalDateTime.now() + ":::::" + threadName2);
        }
        rateLimiter.acquire();
        synchronized (o) {
            atomicInteger.incrementAndGet();
        }
//        callAPIWithHttpClient(url, httpClient, atomicInteger);
        return callAPIWebClient2(url, webClient);
//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private void callAPIWithHttpClient(String url, CloseableHttpClient httpClient, AtomicInteger atomicInteger) {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpGet.abort();
                System.out.println(response.getStatusLine().getReasonPhrase());
            } else {
                String str = EntityUtils.toString(response.getEntity());
//                if (atomicInteger.get() == 4) {
//                    System.out.println(str);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
        }
    }

    private void callAPIWebClient(String url, WebClient webClient) {
        String str = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
    }

    private Mono<String> callAPIWebClient2(String url, WebClient webClient) {
        return webClient.get().uri(url).retrieve().bodyToMono(String.class);
    }

}
