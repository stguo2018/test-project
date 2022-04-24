package com.ews.stguo.testproject.test.feed;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class UserReviewTest {

    @Test
    public void test01() throws Exception {
        String[] paths = new String[] {"new_expedia-local-reviews-hotels-en_us.xml",
        "old_expedia-local-reviews-hotels-en_us.xml"};
        Map<Integer, Integer> newUserReviews = new HashMap<>();
        Pattern pattern = Pattern.compile("^.*<id>(\\d+)</id>.*$");
        Pattern pattern2 = Pattern.compile("</body>");
        try (BufferedReader reader = RWFileUtils.getReader(paths[0])) {
            String line;
            while ((line = reader.readLine()) != null) {
                final Matcher matcher = pattern.matcher(line);
                Integer hotelId = null;
                while (matcher.find()) {
                    hotelId = Integer.parseInt(matcher.group(1));
                }
                if (hotelId != null) {
                    if (hotelId.equals(6285543)) {
                        System.out.println(line);
                    }
                    final Matcher matcher2 = pattern2.matcher(line);
                    int count = 0;
                    while (matcher2.find()) {
                        count++;
                    }
                    newUserReviews.put(hotelId, count);
                }
            }
        }

        Map<Integer, Integer> oldUserReviews = new HashMap<>();
        try (BufferedReader reader = RWFileUtils.getReader(paths[1])) {
            String line;
            while ((line = reader.readLine()) != null) {
                final Matcher matcher = pattern.matcher(line);
                Integer hotelId = null;
                while (matcher.find()) {
                    hotelId = Integer.parseInt(matcher.group(1));
                }
                if (hotelId != null) {
                    if (hotelId.equals(6285543)) {
                        System.out.println(line);
                    }
                    final Matcher matcher2 = pattern2.matcher(line);
                    int count = 0;
                    while (matcher2.find()) {
                        count++;
                    }
                    oldUserReviews.put(hotelId, count);
                }
            }
        }

        System.out.println("New:" + newUserReviews.size());
        System.out.println("Old:" + oldUserReviews.size());
//        try (final BufferedWriter writer = ReadFileUtils.getWriter("record.txt")) {
//            for (Integer key : newUserReviews.keySet()) {
//                if (oldUserReviews.get(key) != null && !oldUserReviews.get(key).equals(newUserReviews.get(key))) {
//                    String str = "HotelId:" + key + ", New:" + newUserReviews.get(key) + ", Old:" + oldUserReviews.get(key);
//                    writer.write(str);
//                    writer.newLine();
//                }
//            }
//            writer.flush();
//        }
    }

}
