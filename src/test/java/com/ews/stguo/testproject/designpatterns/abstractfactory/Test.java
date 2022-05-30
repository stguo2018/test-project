package com.ews.stguo.testproject.designpatterns.abstractfactory;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Test {

    @org.junit.Test
    public void test() {
        FormTable formTable = new WindowsFormTable();
        formTable.render();;
        formTable = new HTMLFormTable();
        formTable.render();
    }

}
