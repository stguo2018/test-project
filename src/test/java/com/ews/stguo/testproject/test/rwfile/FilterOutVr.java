package com.ews.stguo.testproject.test.rwfile;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class FilterOutVr {

    @Test
    public void tet01() throws Exception {
        String[] paths = new String[]{
                "old.txt", "new.txt"
        };
        Set<String> hotelIds = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader(paths[0])) {
            String line;
            while ((line = br.readLine()) != null) {
                hotelIds.add(line.trim());
            }
        }
        System.out.println("1");

        Set<String> hotelIds2 = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader(paths[1])) {
            String line;
            while ((line = br.readLine()) != null) {
                hotelIds2.add(line.trim());
            }
        }
        System.out.println("2");

        try (BufferedWriter bw = RWFileUtils.getWriter("add.txt")) {
            for (String line : hotelIds2) {
                if (!hotelIds.contains(line)) {
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.flush();
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("remove.txt")) {
            for (String line : hotelIds) {
                if (!hotelIds2.contains(line)) {
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.flush();
        }
        System.out.println("Done");
    }

    @Test
    public void test02() throws Exception {
        String pathOld = "03.29/expedia-local-%d.xml";
        String pathNew = "04.06/expedia-local-%d.xml";
        Pattern pattern = Pattern.compile("<id>(\\d+)</id>");
        for (int i = 1; i <= 10; i++) {
            try (BufferedReader br1 = RWFileUtils.getReader(String.format(pathOld, i));
                 BufferedReader br2 = RWFileUtils.getReader(String.format(pathNew, i));
                 BufferedWriter bw1 = RWFileUtils.getWriter("old.txt", true);
                 BufferedWriter bw2 = RWFileUtils.getWriter("new.txt", true)) {
                String line;
                while ((line = br1.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        bw1.write(matcher.group(1));
                        bw1.newLine();
                    }
                }
                bw1.flush();

                while ((line = br2.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        bw2.write(matcher.group(1));
                        bw2.newLine();
                    }
                }
                bw2.flush();
            }
            System.out.println("Phase " + i + " is done.");
        }
    }

    @Test
    public void test03() throws Exception {
        String[] paths = new String[]{
                "ecom vr hotel list.csv",
                "hcom vr hotel list.csv",
                "google-ecom-vr-adjusted-list.csv",
                "google-hcom-vr-adjusted-list.csv",
                "ControlFile-EDE-GOOGLE-BEX.tsv",
                "ControlFile-GOOGLE-VR-ECOM.tsv",
                "feed-control-file.csv",
                "ControlFile-GOOGLE-VR-HCOM.tsv"
        };
        Set<Integer> ecomVrHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(paths[0]);
        Set<Integer> hcomVrHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(paths[1]);
        Set<Integer> googleEcomVrHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(paths[2]);
        Set<Integer> googleHcomVrHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(paths[3]);
        Set<Integer> googleEdeHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(paths[4]);
        Set<Integer> googleEdeVrEcomHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(paths[5]);
        Set<Integer> googleFeedHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(paths[6]);
        Set<Integer> googleEdeVrHcomHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(paths[7]);
        Assert.assertNotNull(ecomVrHotelIds);

//        googleEdeHotelIds.removeAll(googleEdeVrEcomHotelIds);
//        googleFeedHotelIds.removeAll(googleEdeVrHcomHotelIds);

        Collection collection1 = CollectionUtils.retainAll(ecomVrHotelIds, googleEcomVrHotelIds);
        Collection collection2 = CollectionUtils.retainAll(hcomVrHotelIds, googleHcomVrHotelIds);
        Collection collection3 = CollectionUtils.removeAll(ecomVrHotelIds, googleEcomVrHotelIds);
        Collection collection4 = CollectionUtils.removeAll(hcomVrHotelIds, googleHcomVrHotelIds);
        Collection collection5 = CollectionUtils.removeAll(googleEcomVrHotelIds, ecomVrHotelIds);
        Collection collection6 = CollectionUtils.removeAll(googleHcomVrHotelIds, hcomVrHotelIds);
        Collection collection7 = CollectionUtils.retainAll(googleEcomVrHotelIds, googleEdeHotelIds);
        Collection collection8 = CollectionUtils.retainAll(googleHcomVrHotelIds, googleFeedHotelIds);
        Collection collection9 = CollectionUtils.retainAll(ecomVrHotelIds, googleEdeHotelIds);
        Collection collection10 = CollectionUtils.retainAll(hcomVrHotelIds, googleFeedHotelIds);
        System.out.println("ecomVrHotelIds retainAll googleEcomVrHotelIds: " + collection1.size());
        System.out.println("hcomVrHotelIds retainAll googleHcomVrHotelIds: " + collection2.size());
        System.out.println("ecomVrHotelIds removeAll googleEcomVrHotelIds: " + collection3.size());
        System.out.println("hcomVrHotelIds removeAll googleHcomVrHotelIds: " + collection4.size());
        System.out.println("googleEcomVrHotelIds removeAll ecomVrHotelIds: " + collection5.size());
        System.out.println("googleHcomVrHotelIds removeAll hcomVrHotelIds: " + collection6.size());
        System.out.println("googleEcomVrHotelIds retainAll googleEdeHotelIds: " + collection7.size());
        System.out.println("googleHcomVrHotelIds retainAll googleFeedHotelIds: " + collection8.size());
        System.out.println("ecomVrHotelIds retainAll googleEdeHotelIds: " + collection9.size());
        System.out.println("hcomVrHotelIds retainAll googleFeedHotelIds: " + collection10.size());

        Collection collection11 = CollectionUtils.removeAll(ecomVrHotelIds, ecomVrHotelIds);
        Collection collection12 = CollectionUtils.removeAll(hcomVrHotelIds, ecomVrHotelIds);
        Collection collection13 = CollectionUtils.removeAll(googleEcomVrHotelIds, ecomVrHotelIds);
        Collection collection14 = CollectionUtils.removeAll(googleHcomVrHotelIds, ecomVrHotelIds);
        Collection collection15 = CollectionUtils.removeAll(googleEdeHotelIds, ecomVrHotelIds);
        Collection collection16 = CollectionUtils.removeAll(googleFeedHotelIds, ecomVrHotelIds);

        Collection collection17 = CollectionUtils.retainAll(ecomVrHotelIds, ecomVrHotelIds);
        Collection collection18 = CollectionUtils.retainAll(hcomVrHotelIds, ecomVrHotelIds);
        Collection collection19 = CollectionUtils.retainAll(googleEcomVrHotelIds, ecomVrHotelIds);
        Collection collection20 = CollectionUtils.retainAll(googleHcomVrHotelIds, ecomVrHotelIds);
        Collection collection21 = CollectionUtils.retainAll(googleEdeHotelIds, ecomVrHotelIds);
        Collection collection22 = CollectionUtils.retainAll(googleFeedHotelIds, ecomVrHotelIds);
        System.out.println("-----------------E: " + googleEdeHotelIds.size());
        System.out.println("-----------------H: " + googleFeedHotelIds.size());
        System.out.println("11111: " + collection11.size());
        System.out.println("22222: " + collection12.size());
        System.out.println("33333: " + collection13.size());
        System.out.println("44444: " + collection14.size());
        System.out.println("55555: " + collection15.size());
        System.out.println("66666: " + collection16.size());
        System.out.println("77777: " + collection17.size());
        System.out.println("88888: " + collection18.size());
        System.out.println("99999: " + collection19.size());
        System.out.println("101010: " + collection20.size());
        System.out.println("111111: " + collection21.size());
        System.out.println("121212: " + collection22.size());
    }

    @Test
    public void test04() throws Exception {
        System.out.println(isHeader("4564,1,24;83,STR"));
        System.out.println(isHeader("4564,1,24;83"));
        System.out.println(isHeader("expedia_id"));
        System.out.println(isHeader("EXPEDIA"));
        System.out.println(isHeader("1EXPEDIA"));
        System.out.println(isHeader(""));
    }

    private boolean isHeader(String line) {
        return !line.split(",")[0].matches("[0-9]+");
    }

}
