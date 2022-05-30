package com.ews.stguo.testproject.designpatterns.abstractfactory;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class HTMLCheckBox implements CheckBox {
    @Override
    public void render() {
        System.out.println("HTML checkbox render.");
    }

    @Override
    public void onSelect() {
        System.out.println("HTML checkbox select.");
    }
}
