package com.ews.stguo.testproject.test.thread;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class SynchronizedTest {

    @Test
    public void test01() throws Exception {
        TestClass testClass = new TestClass();
        new Thread(() -> testClass.print2("Message1")).start();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread t2 = new Thread(() -> testClass.print2("Message2"));
        t2.start();
        t2.join();
    }

}

class TestClass {

    public synchronized void print1(String str) {
        System.out.println(LocalDateTime.now() + "print1: " + str);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print2(String str) {
        synchronized (str) {
            System.out.println(LocalDateTime.now() + "print2: " + str);
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
