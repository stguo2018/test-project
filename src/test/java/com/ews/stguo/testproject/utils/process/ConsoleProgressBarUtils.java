package com.ews.stguo.testproject.utils.process;

import java.text.DecimalFormat;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ConsoleProgressBarUtils {

    private static final DecimalFormat FORMAT = new DecimalFormat("#.##%");

    public static void show(float progress) {
        show(100, '>', progress);
    }

    public static void show(int barLength, char showChar, float progress) {
        if (progress < 0 || progress > 100) {
            return;
        }
        reset();

        float rate = progress / 100;
        draw(barLength, showChar, rate);
//        if (progress == 100) {
        // 因为测试不换行就不会打印，可能是控制台的问题，暂时注释
        System.out.print('\n');
//        }
    }

    private static void reset() {
        System.out.print('\r');
    }

    private static void draw(int barLength, char showChar, float rate) {
        int length = (int) (barLength * rate);
        System.out.print("Progress: [");
        for (int i = 0; i < length; i++) {
            System.out.print(showChar);
        }
        for (int i = 0; i < barLength - length; i++) {
            System.out.print(" ");
        }
        System.out.print("]" + FORMAT.format(rate));
    }

}
