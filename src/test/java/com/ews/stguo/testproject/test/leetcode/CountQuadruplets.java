package com.ews.stguo.testproject.test.leetcode;

import org.junit.Test;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class CountQuadruplets {

    @Test
    public void test() throws Exception {
        System.out.println(countQuadruplets(new int[]{36,13,38,87,2,90,53,62,44,10,100}));
    }

    private int countQuadruplets(int[] nums) {
        int count = 0;
        if (nums == null || nums.length < 4) {
            return count;
        }
        int st = 0;
        int nd = 1;
        int rd = 2;
        int maxSt = nums.length - 4;
        int maxNd = nums.length - 3;
        int maxRd = nums.length - 2;
        do {
            for (int i = rd + 1; i < nums.length; i++) {
                int result = nums[st] + nums[nd] + nums[rd];
                if (result == nums[i]) {
                    System.out.printf("%d+%d+%d=%d%n", nums[st], nums[nd], nums[rd], result);
                    ++count;
                }
            }
            if (rd < maxRd) {
                ++rd;
            } else if (nd < maxNd) {
                ++nd;
                rd = nd + 1;
            } else {
                ++st;
                nd = st + 1;
            }
        } while (st <= maxSt);
        return count;
    }

    private int countQuadruplets2(int[] nums) {
        int count = 0;
        if (nums == null || nums.length < 4) {
            return count;
        }
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                for (int k = j + 1; k < nums.length; k++) {
                    for (int l = k + 1; l < nums.length; l++) {
                        if (nums[l] == nums[i] + nums[j] + nums[k]) {
                            ++count;
                            System.out.printf("%d+%d+%d=%d%n", nums[i], nums[j], nums[k], nums[l]);
                        }
                    }
                }
            }
        }
        return count;
    }

}
