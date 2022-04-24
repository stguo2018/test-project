package com.ews.stguo.testproject.utils.text;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class TrimUtils {

    public static String trim(String str) {
        if (str != null) {
            str = str.replace(" ", "");
        }
        if (StringUtils.isBlank(str)) {
            return str;
        }
        if (Objects.equals("null", str.trim())) {
            return null;
        }
        return str.trim();
    }

}
