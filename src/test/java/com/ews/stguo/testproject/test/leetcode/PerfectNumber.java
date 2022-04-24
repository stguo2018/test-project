package com.ews.stguo.testproject.test.leetcode;

import org.junit.Test;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class PerfectNumber {

    @Test
    public void test() {
        System.out.println(checkPerfectNumber(2));
    }

    private boolean checkPerfectNumber(int num) {
        int sum = 0;
        for (int i = 1; i < num; i++) {
            if (num % i == 0) {
                sum += i;
            }
        }
        return num == sum;
    }

}
