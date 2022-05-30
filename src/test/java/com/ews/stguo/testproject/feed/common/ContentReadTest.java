package com.ews.stguo.testproject.feed.common;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.junit.Test;

import java.io.BufferedReader;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ContentReadTest {

    public static final String GENERAL_FILE_TEMP = "expedia-lodging-%d-all.jsonl";

    @Test
    public void test01() throws Exception {
        for (int i = 1; i <= 3; i++) {
            try (BufferedReader br = RWFileUtils.getReader(String.format(GENERAL_FILE_TEMP, i))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("33555155")) {
                        System.out.println(line);
                        break;
                    }
                }
            }
        }
    }

}
