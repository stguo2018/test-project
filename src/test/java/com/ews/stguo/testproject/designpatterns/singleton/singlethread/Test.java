package com.ews.stguo.testproject.designpatterns.singleton.singlethread;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Test {

    @org.junit.Test
    public void test() {
        System.out.println("If you see the same value, then singleton was reused (yay!)" + "\n" +
                "If you see different values, then 2 singletons were created (booo!!)" + "\n\n" +
                "RESULT:" + "\n");
        Singleton singleton = Singleton.getInstance("FOO");
        Singleton anotherSingleton = Singleton.getInstance("BAR");
        System.out.println(singleton.getValue());
        System.out.println(anotherSingleton.getValue());
    }

}
