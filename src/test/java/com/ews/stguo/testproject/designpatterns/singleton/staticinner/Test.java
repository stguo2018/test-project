package com.ews.stguo.testproject.designpatterns.singleton.staticinner;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Test {

    @org.junit.Test
    public void test() throws Exception {
        System.out.println("If you see the same value, then singleton was reused (yay!)" + "\n" +
                "If you see different values, then 2 singletons were created (booo!!)" + "\n\n" +
                "RESULT:" + "\n");
        Thread t1 = new Thread(() -> {
            System.out.println(Singleton.getInstance().getValue());
        });
        Thread t2 = new Thread(() -> {
            System.out.println(Singleton.getInstance().getValue());
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

}
