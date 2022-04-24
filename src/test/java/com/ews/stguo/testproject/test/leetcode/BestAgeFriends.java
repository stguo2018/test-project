package com.ews.stguo.testproject.test.leetcode;

import org.junit.Test;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class BestAgeFriends {

    @Test
    public void test01() throws Exception {
        System.out.println(numFriendRequests(new int[]{16,16}));
    }

    private int numFriendRequests(int[] ages) throws Exception {
        if (ages == null || ages.length == 0) {
            return 0;
        }
        int[][] count = new int[ages.length][ages.length];
        for (int i = 0; i < ages.length; i++) {
            for (int j = 0; j < ages.length; j++) {
                if (i == j || count[i][j] == 1 || count[j][i] == 1) continue;
                int x = ages[i];
                int y = ages[j];
                if (y <= 0.5 * x + 7 || y > x || (y > 100 && x < 100)) {
                    continue;
                }
                count[i][j] = 1;
            }
        }
        int c = 0;
        for (int[] i : count) {
            for (int i1 : i) {
                c += i1;
            }
        }
        return c;
    }

}
