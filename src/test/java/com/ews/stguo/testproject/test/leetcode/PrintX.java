package com.ews.stguo.testproject.test.leetcode;

import org.junit.Test;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class PrintX {

    @Test
    public void testPrintX() throws Exception {
        printX(1);
        System.out.println("---------------------");
        printX(2);
        System.out.println("---------------------");
        printX(3);
        System.out.println("---------------------");
        printX(4);
        System.out.println("---------------------");
        printX(5);
        System.out.println("---------------------");
        printX(6);
        System.out.println("---------------------");
        printX(7);
        System.out.println("---------------------");
        printX(8);
        System.out.println("---------------------");
    }

    private void printX(int num) {
        for (int i = 0; i < num; i++) {
            for (int j = 0; j < num; j++) {
                if (j == i) {
                    System.out.print("X");
                } else if (j == (num - 1 - i)) {
                    System.out.print("X");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

}
