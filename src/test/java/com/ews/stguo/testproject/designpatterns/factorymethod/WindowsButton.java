package com.ews.stguo.testproject.designpatterns.factorymethod;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class WindowsButton implements Button {
    @Override
    public void render() {
        System.out.println("Windows button render.");
    }

    @Override
    public void onClick() {
        System.out.println("Windows button click.");
    }
}
