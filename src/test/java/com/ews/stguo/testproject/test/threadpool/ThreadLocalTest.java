package com.ews.stguo.testproject.test.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ThreadLocalTest {

    public static ThreadLocal<String> tl = new ThreadLocal<>();

    @Test
    public void testThreadLocal_01() throws Exception {
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setNameFormat("test-pool-%d");
        threadFactoryBuilder.setDaemon(false);
        ThreadFactory threadFactory = threadFactoryBuilder.build();
        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10), r -> {
            Thread thread = threadFactory.newThread(r);
            try {
                Field threadLocals = Thread.class.getDeclaredField("threadLocals");
                threadLocals.setAccessible(true);
                threadLocals.set(thread, threadLocals.get(Thread.currentThread()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return thread;
        });
        tl.set("ThreadLocal test value - 1");
        executorService.submit(() -> {
            System.out.printf("%s is Running. message: %s%n", Thread.currentThread().getName(), tl.get());
        });

//        tl.set("ThreadLocal test value - 1");
//        Thread t = new TtlThread(() -> {
//            System.out.printf("%s is Running.%n", Thread.currentThread().getName());
//            System.out.println(tl.get());
//        }, "Test-01");
//        t.start();

        Thread.sleep(5000);
    }

    @Test
    public void test() throws Exception {

        Mono<String> mt = Mono.<Void>empty().publish(c -> Mono.subscriberContext().map(ctx -> {
            String context = ctx.get("key");
            System.out.println(Thread.currentThread() + "getRequestContext");
            return context;
        }).flatMap(context -> Mono.fromSupplier(() -> {
            String s = UUID.randomUUID() + "_" + context;
            System.out.println(Thread.currentThread() + " Init " + s);
            return s;
        }).subscribeOn(Schedulers.newElastic("thread-subscribeOn")))).subscriberContext(ctx -> {
            ctx = ctx.put("key", "context");
            System.out.println(Thread.currentThread() + " subscriberContext ");
            return ctx;
        });
        mt.block();
        Thread.sleep(3000);
    }

    @Test
    public void test03() throws Exception {
        Publisher<Mono<String>> publisher = new Publisher<>();
        Runnable target = () -> {
            for (int i = 0; i < 1; i++) {
                publisher.addMessage(Mono.just(String.valueOf(i)).flatMap(ii -> {
                    System.out.println("test:" + Thread.currentThread().getName());
                    return Mono.subscriberContext().publishOn(Schedulers.newElastic("thread-publishOn")).map(ctx -> {
                        String context = ctx.get("key");
                        ThreadLocalTest.tl.set(context);
                        System.out.println("GetRequestContext:" + Thread.currentThread().getName() + ":" + context);
                        return context;
                    }).flatMap(context -> {
                        System.out.println("ExecutingBefore: " + Thread.currentThread().getName() + ":" + context + ":" + ThreadLocalTest.tl.get());
                        return Mono.fromSupplier(() -> {
                            System.out.println("Executing: " + Thread.currentThread().getName() + ":" + context + ":" + ThreadLocalTest.tl.get());
                            return context;
                        })/*.publishOn(Schedulers.newElastic("thread-publishOn"))*/;
                    });
                }));
            }
            publisher.stop();
        };
        Thread t1 = new Thread(target, "Publisher");

        int size = 10;
        Consumer<Mono<String>> consumer = m -> {
            m.subscriberContext(ctx -> {
                ctx = ctx.put("key", UUID.randomUUID().toString());
                System.out.println("Init:" + Thread.currentThread().getName());
                return ctx;
            }).subscribe(context -> {
                System.out.println("Block:" + Thread.currentThread().getName() + ":" + context);
            });
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        t1.start();
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setNameFormat("subscriber-pool-%d");
        threadFactoryBuilder.setDaemon(false);
        ThreadFactory threadFactory = threadFactoryBuilder.build();
        ExecutorService executorService = new ThreadPoolExecutor(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10), r -> {
            Thread thread = threadFactory.newThread(r);
            try {
                Field threadLocals = Thread.class.getDeclaredField("threadLocals");
                threadLocals.setAccessible(true);
                threadLocals.set(thread, threadLocals.get(Thread.currentThread()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return thread;
        });
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                Subscriber<Mono<String>> subscriber = new Subscriber<>(consumer);
                publisher.subscribeOn(subscriber);
            }, executorService));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get();
    }

}

class Publisher<T> {

    private final ReentrantLock readLock = new ReentrantLock();
    private final ReentrantLock writeLock = new ReentrantLock();
    private final List<T> messages;
    private int writeIndex = 0;
    private int readIndex = 0;
    private final int maxMessages = 1024;
    private boolean completed = false;

    public Publisher() {
        messages = new ArrayList<>();
        for (int i = 0; i < maxMessages; i++) {
            messages.add(null);
        }
    }

    public void subscribeOn(Subscriber<T> subscriber) {
        Subscription<T> subscription = new Subscription<>(this, subscriber);
        subscriber.onSubscribe(subscription);
    }

    public void request(int num, Subscriber<T> subscriber) {
        if (completed) {
            subscriber.onCompleted();
            return;
        }
        for (int i = 0; i < num; i++) {
            int index = getReadIndex();
            readLock.unlock();
            if (index == -1) {
                subscriber.onCompleted();
                return;
            }
            T t = messages.get(index);
            messages.set(i, null);
            subscriber.onNext(t);
        }
    }

    public void addMessage(T message) {
        if (completed) {
            return;
        }
        int index = getWriteIndex();
        writeLock.unlock();
        if (index == -1) {
            return;
        }
        messages.set(index, message);
    }

    private int getWriteIndex() {
        writeLock.lock();
        if (writeIndex == maxMessages) {
            writeIndex = 0;
            return writeIndex;
        }
        while (!completed && messages.get(writeIndex) != null) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (completed) {
            return -1;
        }
        return writeIndex++;
    }

    private synchronized int getReadIndex() {
        readLock.lock();
        if (readIndex == maxMessages) {
            readIndex = 0;
        }
        while (!completed && messages.get(readIndex) == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (completed) {
            return -1;
        }
        return readIndex++;
    }

    public synchronized void stop() {
        this.completed = true;
    }

}

class Subscriber<T> {

    private Subscription<T> subscription;
    private final Consumer<T> consumer;

    public Subscriber(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    public void onSubscribe(Subscription<T> subscription) {
        System.out.println(Thread.currentThread() + ":On subscribe.");
        this.subscription = subscription;
        subscription.request(1);
    }

    public void onNext(T message) {
        consumer.accept(message);
        subscription.request(1);
    }

    public void onCompleted() {
        System.out.println("Completed");
    }

}

class Subscription<T> {

    private final Publisher<T> publisher;
    private final Subscriber<T> subscriber;

    public Subscription(Publisher<T> publisher, Subscriber<T> subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    public void request(int num) {
        publisher.request(num, subscriber);
    }

}