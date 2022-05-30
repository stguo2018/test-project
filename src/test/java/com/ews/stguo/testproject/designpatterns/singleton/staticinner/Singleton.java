package com.ews.stguo.testproject.designpatterns.singleton.staticinner;

import java.util.UUID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Singleton {

    private String value;

    private Singleton(String value) {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.value = value;
    }

    private static class SingletonHolder {

        static {
            System.out.println("Static inner class loaded!!");
        }

        private static final Singleton INSTANCE = new Singleton(UUID.randomUUID().toString());
    }

    public static Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public String getValue() {
        return value;
    }

}
