package com.ews.stguo.testproject.feed.common.listings;

import cn.hutool.json.JSONUtil;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.validate.vrbo.model.listings.ListingsModel;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ListingsTest {

    private static final String FILE_TEMP = "expedia-lodging-%d-all.jsonl";

    @Test
    public void test01() throws Exception {
        int count = 2656421;
        int bound = count / 50000;
        System.out.println(bound);
        Random random = new SecureRandom();
        int round = 0;
        int lineCount = 0;
        int instantBookable = 0;
        int instantUnbookable = 0;
        int quoteAndHoldBookable = 0;
        int quoteAndHoldUnbookable = 0;
        try (BufferedWriter bw = RWFileUtils.getWriter("Contents.csv")) {
            int lineIndex = random.nextInt(bound);
            for (int i = 1; i <= 3; i++) {
                try (BufferedReader br = RWFileUtils.getReader(String.format(FILE_TEMP, i))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        ListingsModel model = JSONUtil.toBean(line, ListingsModel.class);
                        String vrboId = model.getPropertyId().getVrbo();
                        boolean vrboBookable = model.getBookable().isVrbo();
                        Boolean instantBook = model.getVrboPropertyType().getInstantBook();
                        if (lineCount == lineIndex && StringUtils.isNotBlank(vrboId)) {
                            if ((BooleanUtils.isTrue(instantBook) || round >= 40000) && (vrboBookable || round % 4 == 0)) {
                                bw.write(line);
                                bw.flush();
                                round++;
                                lineIndex = round * bound + random.nextInt(bound);
                                if (BooleanUtils.isTrue(instantBook) && vrboBookable) {
                                    instantBookable++;
                                } else if (BooleanUtils.isTrue(instantBook) && !vrboBookable) {
                                    instantUnbookable++;
                                } else if (BooleanUtils.isFalse(instantBook) && vrboBookable) {
                                    quoteAndHoldBookable++;
                                } else if (BooleanUtils.isFalse(instantBook) && !vrboBookable) {
                                    quoteAndHoldUnbookable++;
                                }
                                continue;
                            }
                            lineIndex++;
                        }
                        lineCount++;
                    }
                }
            }
        }
        System.out.println(instantBookable);
        System.out.println(instantUnbookable);
        System.out.println(quoteAndHoldBookable);
        System.out.println(quoteAndHoldUnbookable);
    }

}
