package com.ews.stguo.testproject.designpatterns.factorymethod;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class WindowsDialog extends Dialog {

    @Override
    public Button createButton() {
        return new WindowsButton();
    }
}
