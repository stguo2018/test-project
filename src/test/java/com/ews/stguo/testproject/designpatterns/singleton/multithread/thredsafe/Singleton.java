package com.ews.stguo.testproject.designpatterns.singleton.multithread.thredsafe;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Singleton {

    private static volatile Singleton instance;
    public String value;

    private Singleton(String value) {
        // The following code emulates slow initialization.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Singleton getInstance(String value) {
        if (instance != null) {
            return instance;
        }
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton(value);
            }
        }
        return instance;
    }

}
