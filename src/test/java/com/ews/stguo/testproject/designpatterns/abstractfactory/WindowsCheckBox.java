package com.ews.stguo.testproject.designpatterns.abstractfactory;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class WindowsCheckBox implements CheckBox {
    @Override
    public void render() {
        System.out.println("Windows checkbox render.");
    }

    @Override
    public void onSelect() {
        System.out.println("Windows checkbox select.");
    }
}
