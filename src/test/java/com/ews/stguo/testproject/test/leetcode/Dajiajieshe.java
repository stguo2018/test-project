package com.ews.stguo.testproject.test.leetcode;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Dajiajieshe {

    @Test
    public void test() throws Exception {
//        System.out.println(rob(new int[] {2,3,2}));
        System.out.println(rob(new int[]{2, 3, 2, 100, 2, 12, 3, 11}));
    }

    public int rob(int[] nums) {
        if (nums.length == 1) {
            return nums[0];
        }
        if (nums.length == 2) {
            return Math.max(nums[0], nums[1]);
        }
        return Math.max(max(nums, 0, nums.length - 2), max(nums, 1, nums.length - 1));
    }

    public int max(int[] nums, int start, int end) {
        int first = nums[start];
        int second = Math.max(first, nums[start + 1]);
        for (int i = start + 2; i <= end; i++) {
            int temp = second;
            second = Math.max(first + nums[i], second);
            first = temp;
        }
        return second;
    }

}
