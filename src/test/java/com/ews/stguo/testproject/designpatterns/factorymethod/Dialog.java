package com.ews.stguo.testproject.designpatterns.factorymethod;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public abstract class Dialog {

    public void render() {
        Button okButton = createButton();
        okButton.onClick();
        okButton.render();
    }

    public abstract Button createButton();

}
