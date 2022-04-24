package com.ews.stguo.testproject.test.other;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Tttttest {

    @Test
    public void test01() throws Exception {
        System.out.println("Init stage 1....");
        List<Integer> list1 = IntStream.range(0, 1000000).boxed().collect(Collectors.toList());
        System.out.println("Init stage 2....");
        List<Integer> list2 = IntStream.range(500000, 1500000).boxed().collect(Collectors.toList());
        System.out.println("Init stage 3....");
        Set<Integer> list3 = new HashSet<>(list2);
        System.out.println("Init done.");
        final long startTime = System.currentTimeMillis();
        list1.removeAll(list2);
        System.out.println("list1 size:" + list1.size());
        System.out.println("time" + (System.currentTimeMillis() - startTime));
    }

    @Test
    public void test02() throws Exception {
        String a = "Hello";
        // \u000d a="world";
        System.out.println(a);
        // \u000a a="hello world!";
        System.out.println(a);
    }

}
