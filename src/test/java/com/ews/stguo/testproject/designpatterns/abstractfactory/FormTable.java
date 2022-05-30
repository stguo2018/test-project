package com.ews.stguo.testproject.designpatterns.abstractfactory;

import com.ews.stguo.testproject.designpatterns.factorymethod.Button;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public abstract class FormTable {

    public void render() {
        Button button = createButton();
        button.render();
        CheckBox checkBox = createCheckBox();
        checkBox.render();
        checkBox.onSelect();
        button.onClick();
    }

    public abstract Button createButton();

    public abstract CheckBox createCheckBox();

}
