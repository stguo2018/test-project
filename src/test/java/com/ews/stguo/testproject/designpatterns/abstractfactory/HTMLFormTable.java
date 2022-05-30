package com.ews.stguo.testproject.designpatterns.abstractfactory;

import com.ews.stguo.testproject.designpatterns.factorymethod.Button;
import com.ews.stguo.testproject.designpatterns.factorymethod.HTMLButton;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class HTMLFormTable extends FormTable {
    @Override
    public Button createButton() {
        return new HTMLButton();
    }

    @Override
    public CheckBox createCheckBox() {
        return new HTMLCheckBox();
    }
}
