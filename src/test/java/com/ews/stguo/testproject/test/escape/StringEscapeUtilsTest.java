package com.ews.stguo.testproject.test.escape;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class StringEscapeUtilsTest {

    @Test
    public void testEscape() {
        System.out.println("转义HTML3: " + StringEscapeUtils.escapeHtml3("<a href=’’>亿客行</a>"));
        System.out.println("反转义HTML4: " + StringEscapeUtils.escapeHtml4("<a href=’’>亿客行</a>"));
        System.out.println("转义HTML3: " + StringEscapeUtils.unescapeHtml3("&lt;a href=’’&gt;亿客行&lt;/a&gt;"));
        System.out.println("反转义HTML7: " + StringEscapeUtils.unescapeHtml4("&lt;a href=&rsquo;&rsquo;&gt;亿客行&lt;/a&gt;"));
        System.out.println("--------------------------------");
        System.out.println("转成Unicode编: " + StringEscapeUtils.escapeJava("亿客行"));
        System.out.println("Unicode编码转成中文: " + StringEscapeUtils.unescapeJava("\u4EBF\u5BA2\u884C"));
        System.out.println("--------------------------------");
        System.out.println("转义XML110: " + StringEscapeUtils.escapeXml10("<name>亿客行'行'\b\uD83D\uDC4D</name>"));
        System.out.println("转义XML111: " + StringEscapeUtils.escapeXml11("<name>亿客行'行'\b\uD83D\uDC4D</name>"));
        System.out.println("反转义XML: " + StringEscapeUtils.unescapeXml("&lt;name&gt;亿客行&apos;行&apos;&lt;/name&gt;"));
        System.out.println("--------------------------------");
        String json = getData();
        System.out.println("转义Json: " + StringEscapeUtils.escapeJson(json));
        String newJson = json.replace("\"", "-\"");
        System.out.println(newJson);
    }

    @Test
    public void test2() throws Exception {
        for (int i = 1; i <= 9; i++) {
            List<String> results = calculation(4, 3, 2, i, 24);
            if (CollectionUtils.isNotEmpty(results)) {
                System.out.println("Num - " + i);
                for (String result : results) {
                    System.out.println(result);
                }
                System.out.println("----------------------------");
            }
        }
    }

    private List<String> calculation(int x, int y, int z, int c, int result) throws Exception {
        List<String> results = new ArrayList<>();
        // (xy)zc, (xyz)c, x(yz)c, x(yzc), xy(zc)
        List<String[]> operators = buildOperators();
        for (String[] operator : operators) {
            for (int i = 0; i < 5; i++) {
                ScriptEngineManager factory = new ScriptEngineManager();
                ScriptEngine engine = factory.getEngineByName("JavaScript");
                String script;
                if (i == 0) {
                    script = String.format("(%d %s %d) %s %d %s %d", x, operator[0], y, operator[1], z, operator[2], c);
                } else if (i == 1) {
                    script = String.format("(%d %s %d %s %d) %s %d", x, operator[0], y, operator[1], z, operator[2], c);
                } else if (i == 2) {
                    script = String.format("%d %s (%d %s %d) %s %d", x, operator[0], y, operator[1], z, operator[2], c);
                } else if (i == 3) {
                    script = String.format("%d %s (%d %s %d %s %d)", x, operator[0], y, operator[1], z, operator[2], c);
                } else {
                    script = String.format("%d %s %d %s (%d %s %d)", x, operator[0], y, operator[1], z, operator[2], c);
                }
                Object o = engine.eval(script);
                try {
                    if (new BigDecimal(String.valueOf(o)).compareTo(new BigDecimal(result)) == 0) {
                        results.add(script + " = 24");
                    }
                } catch (NumberFormatException e) {
                    // System.out.println("error calculation: " + script);
                }
            }
        }
        return results;
    }

    private List<String[]> buildOperators() {
        String[] baseOperators = {"+", "-", "*", "/"};
        List<String[]> operators = new ArrayList<>();
        for (String o1 : baseOperators) {
            for (String o2 : baseOperators) {
                for (String o3 : baseOperators) {
                    operators.add(new String[] {o1 , o2, o3});
                }
            }
        }
        return operators;
    }

    private String getData() {
        String data;
        try (BufferedReader br = RWFileUtils.getReader("text.txt")) {
            data = br.readLine();
        } catch (Exception e) {
            data = null;
        }
        return data;
    }

}
