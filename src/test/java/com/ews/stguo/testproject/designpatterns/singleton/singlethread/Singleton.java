package com.ews.stguo.testproject.designpatterns.singleton.singlethread;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Singleton {

    private static Singleton instance;
    private String value;

    private Singleton(String value) {
        // The following code emulates slow initialization
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.value = value;
    }

    public static Singleton getInstance(String value) {
        if (instance == null) {
            instance = new Singleton(value);
        }
        return instance;
    }

    public String getValue() {
        return value;
    }
}
