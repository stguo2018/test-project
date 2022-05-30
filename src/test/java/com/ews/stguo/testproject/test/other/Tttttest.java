package com.ews.stguo.testproject.test.other;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
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

    @Test
    public void test03() throws Exception {
        Random random = new Random();
        List<List<Integer>> weekDays = IntStream.range(0, 7).boxed().map(day ->
                IntStream.range(0, 24).boxed().map(hour -> random.nextInt(100)).collect(Collectors.toList())
        ).collect(Collectors.toList());
        // simulation
        for (int j = 0; j < weekDays.size(); j++) {
            System.out.println("---------------------------Day" + (j + 1) + "--------------------------------");
            List<Integer> day = weekDays.get(j);
            StringBuilder sb1 = new StringBuilder("[0(" + day.get(0) + ")");
            StringBuilder sb2 = new StringBuilder("[0-0(" + day.get(0) + ")");
            for (int i = 1; i < day.size(); i++) {
                int previousHour = day.get(i - 1);
                int currentHour = day.get(i);
                day.set(i, currentHour + previousHour);
                sb1.append(",").append(i).append("(").append(currentHour).append(")");
                sb2.append(",0-").append(i).append("(").append(day.get(i)).append(")");
            }
            sb1.append("]");
            sb2.append("]");
            System.out.println("Before:" + sb1);
            System.out.println("After:" + sb2);
        }
        weekDays.forEach(day -> {

        });
    }

}
