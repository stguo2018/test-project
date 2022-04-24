package com.ews.stguo.testproject.test.leetcode;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class TwoNumSum {

    @Test
    public void test() throws Exception {
        System.out.println(Arrays.toString(twoSum(new int[] {3, 2, 3}, 6)));
    }

    public int[] twoSum(int[] nums, int target) {
        for (int i = 0; i < nums.length - 1; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[] {i, j};
                }
            }
        }
        return new int[] {-1, -1};
    }

}
