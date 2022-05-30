package com.ews.stguo.testproject.designpatterns.factorymethod;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Test {

    @org.junit.Test
    public void test() {
        Dialog dialog = new WindowsDialog();
        dialog.render();;
        dialog = new HTMLDialog();
        dialog.render();
    }

}
