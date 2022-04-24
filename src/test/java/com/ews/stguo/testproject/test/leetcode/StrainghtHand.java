package com.ews.stguo.testproject.test.leetcode;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class StrainghtHand {

    @Test
    public void test() throws Exception {
        System.out.println(isNStraightHand(new int[]{1, 2, 3 ,4 ,5}, 4));
    }

    private boolean isNStraightHand(int[] hand, int groupSize) {
        if (hand == null || hand.length == 0 || groupSize <= 0 || hand.length % groupSize != 0) {
            return false;
        }
        // Sort
        for (int i = 0; i < hand.length - 1; i++) {
            for (int j = 0; j < hand.length - 1 - i; j++) {
                if (hand[j] > hand[j + 1]) {
                    hand[j] = hand[j] + hand[j + 1];
                    hand[j + 1] = hand[j] - hand[j + 1];
                    hand[j] = hand[j] - hand[j + 1];
                }
            }
        }
        // Group
        Map<Integer, Integer> count = new HashMap<>();
        for (int i : hand) {
            count.put(i, count.getOrDefault(i, 0) + 1);
        }
        for (int i : hand) {
            Integer v = count.get(i);
            if (v == 0) continue;
            for (int j = 0; j < groupSize; j++) {
                Integer v2 = count.getOrDefault(j + i, 0);
                if (v2 == 0) return false;
                count.put(j + i, v2 - 1);
            }
        }
        return true;
    }

}
