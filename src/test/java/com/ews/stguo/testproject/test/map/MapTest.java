package com.ews.stguo.testproject.test.map;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class MapTest {

    @Test
    public void test01() {
        String[] strs = new String[2000000];
        for (int i = 0; i < strs.length; i++) {
            strs[i] = UUID.randomUUID().toString();
        }
        Map<String, Map<String, Integer>> m = new HashMap<>();
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < strs.length; i++) {
            map.put(strs[i], i);
        }
        m.put("a", map);
        Thread t = new Thread(() -> {
            while (Thread.interrupted()) {
                Map<String, Integer> map2 = new HashMap<>();
                for (int i = 0; i < strs.length; i++) {
                    map2.put(strs[i], i);
                }
                m.put("a", map2);
            }
        });
//        t.start();
        long l = System.currentTimeMillis();
        for (int k = 0; k < 200; k++) {
            Map<String, Integer> map2 = m.get("a");
            for (int i = 0; i < strs.length; i++) {
                if (map.containsKey(strs[i]) && map2.get(strs[i]) == i) {

                }
            }
        }
        System.out.println(System.currentTimeMillis() - l + "ms");
        t.interrupt();
    }

}
