package com.ews.stguo.testproject.designpatterns.abstractfactory;

import com.ews.stguo.testproject.designpatterns.factorymethod.Button;
import com.ews.stguo.testproject.designpatterns.factorymethod.WindowsButton;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class WindowsFormTable extends FormTable {
    @Override
    public Button createButton() {
        return new WindowsButton();
    }

    @Override
    public CheckBox createCheckBox() {
        return new WindowsCheckBox();
    }
}
