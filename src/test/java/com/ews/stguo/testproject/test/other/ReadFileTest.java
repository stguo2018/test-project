package com.ews.stguo.testproject.test.other;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opencsv.CSVReader;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ReadFileTest {

    private static final String BASE_PATH = "C:/Users/stguo/Downloads/";

    @Test
    public void test1() throws Exception {

//        String[] paths = {"ControlFile-EDE-GOOGLE-BEX.tsv",
//        "control-file.csv",
//        "ControlFile-GOOGLE-VR-ECOM.tsv",
//        "ControlFile-GOOGLE-VR-ECOM-PRICE.tsv",
//        "ControlFile-GOOGLE-VR-HCOM.tsv",
//        "ControlFile-EDE-GOOGLE-HCOM-PRICE.tsv",
//        "ControlFile-EDE-GOOGLE-BEX-PRICE.tsv"};
        String[] paths = {"ControlFile-GOOGLE-VR-HCOM.tsv"};
        Set<Integer> hotelIds = Sets.newHashSet();
        for (String path : paths) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(BASE_PATH + path)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (StringUtils.isBlank(line) || Objects.equals(line, "HOTEL_ID")) {
                        continue;
                    }
                    String[] columns = line.trim().split(",");
                    if (columns.length > 2) {
                        if (Integer.parseInt(columns[1].trim()) != 1 || !columns[2].trim().contains("24")) {
                            continue;
                        }
                    } else if (columns.length == 2) {
                        continue;
                    }
                    hotelIds.add(Integer.parseInt(columns[0].trim()));
                }
            }
        }
        Assert.assertNotNull(hotelIds);
        System.out.println(hotelIds.size());
    }

    @Test
    public void test02() throws Exception {
//        String[] paths = {"old_vsc-local-1.tsv", "new_vsc-local-1.tsv"};
        String[] paths = {"old_vsc-local-1.tsv", "vsc-local-1.tsv"};
        Map<String, Set<Integer>> hotelIds = Maps.newHashMap();
        CountDownLatch countDownLatch = new CountDownLatch(paths.length);
        for (String path : paths) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(BASE_PATH + path), StandardCharsets.UTF_8));
            new Thread(() -> {
                int count = 0;
                String[] line = null;
                try (CSVReader cr = new CSVReader(br, '\t')) {
                    Assert.assertNotNull(cr);
                    count = 0;
                    while ((line = cr.readNext()) != null) {
                        if (++count == 1) {
                            continue;
                        }
                        if (count > 0 && count % 100000 == 0) {
                            System.out.println(path + " loading..., current size: " + count);
                        }
                        Set<Integer> hotelIdList = hotelIds.computeIfAbsent(path, key -> Sets.newHashSet());
                        hotelIdList.add(Integer.parseInt(line[6].trim()));
                    }
                    countDownLatch.countDown();
                } catch (Exception ex) {
                    System.out.println("File Path: " + path + ", line: " + count + ", line content: " + Arrays.deepToString(line));
                    ex.printStackTrace();
                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.await();
        System.out.println("Old size: " + hotelIds.get(paths[0]).size());
        System.out.println("New size: " + hotelIds.get(paths[1]).size());
        int decreaseSize = CollectionUtils.removeAll(hotelIds.get(paths[0]), hotelIds.get(paths[1])).size();
        int increaseSize = CollectionUtils.removeAll(hotelIds.get(paths[1]), hotelIds.get(paths[0])).size();
        System.out.println("Decrease Size: " + decreaseSize);
        System.out.println("Increase Size: " + increaseSize);
    }

    @Test
    public void test03() throws Exception {
        Set<Integer> feedFileHotelIds = Sets.newHashSet();
        int countLine = 0;
        String[] paths = {"expedia-local-vr-1.xml", "expedia-local-vr-2.xml", "expedia-local-vr-3.xml"};
        for (String path : paths) {
            try (BufferedReader br = getReader(path)) {
                String line;
                while ((line = br.readLine()) != null) {
                    countLine++;
                    Pattern pattern = Pattern.compile("^.*<id>(\\d+)</id>.*$");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        Integer hotelId = Integer.parseInt(matcher.group(1).trim());
                        feedFileHotelIds.add(hotelId);
                    }
                }
            }
        }
        System.out.println("Count read line: " + countLine);
        System.out.println("Feed file hotel id quantity: " + feedFileHotelIds.size());
        Set<Integer> missingHotelIds = Sets.newHashSet();
        try (BufferedReader br = getReader("ControlFile-GOOGLE-VR-ECOM-PRICE.tsv")) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().toUpperCase().equals("HOTEL_ID")) {
                    continue;
                }
                Integer hotelId = Integer.parseInt(line.trim());
                if (!feedFileHotelIds.contains(hotelId)) {
                    missingHotelIds.add(Integer.parseInt(line.trim()));
                }
            }
        }
        Assert.assertNotNull(missingHotelIds);
        for (Integer missingHotelId : missingHotelIds) {
            System.out.print(missingHotelId + ",");
        }
        System.out.println("Missing hotel id quantity: " + missingHotelIds.size());
    }

    @Test
    public void test04() throws Exception {
        String[] paths = {"old-price.tsv", "old-feed.tsv"};
        Map<String, Set<Integer>> hotelIdsMap = Maps.newHashMap();
        for (String path : paths) {
            try (BufferedReader br = getReader(path)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if ("HOTEL_ID".equals(line.trim())) {
                        continue;
                    }
                    Set<Integer> hotelIds = hotelIdsMap.computeIfAbsent(path, key -> Sets.newHashSet());
                    hotelIds.add(Integer.parseInt(line.trim()));
                }
            }
        }
        Assert.assertNotNull(hotelIdsMap);
        Set<Integer> oldPriceHotelIds = hotelIdsMap.get(paths[0]);
        Set<Integer> oldFeedHotelIds = hotelIdsMap.get(paths[1]);
        System.out.println("Old price hotel ids size: " + oldPriceHotelIds.size());
        System.out.println("Old feed hotel ids size: " + oldFeedHotelIds.size());
        System.out.println("For the old price, old feed decrease: " + CollectionUtils.removeAll(oldPriceHotelIds, oldFeedHotelIds).size());
        System.out.println("For the old feed, old price decrease: " + CollectionUtils.removeAll(oldFeedHotelIds, oldPriceHotelIds).size());
    }

    @Test
    public void test05() throws Exception {
        String[] paths = {"new-price.tsv", "old-feed.tsv"};
        Set<Integer> totalHotelIds = Sets.newHashSet();
        for (String path : paths) {
            try (BufferedReader br = getReader(path)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if ("HOTEL_ID".equals(line.trim())) {
                        continue;
                    }
                    totalHotelIds.add(Integer.parseInt(line.trim()));
                }
            }
        }
        Assert.assertNotNull(totalHotelIds);
        System.out.println("Total hotel ids size: " + totalHotelIds.size());
        try (BufferedWriter bw = getWriter("new-feed.tsv")) {
            bw.write("HOTEL_ID");
            bw.newLine();
            for (Integer hotelId : totalHotelIds) {
                bw.write(String.valueOf(hotelId));
                bw.newLine();
            }
            bw.flush();
        }
        System.out.println("Success!");
    }

    @Test
    public void test06() throws Exception {
        String[] paths = {"ControlFile-EDE-GOOGLE-BEX.tsv", "ControlFile-GOOGLE-VR-ECOM.tsv"};
        Set<Integer> bexHotelIDs = Sets.newHashSet();
        try (BufferedReader br = getReader(paths[0])) {
            String line;
            while ((line = br.readLine()) != null) {
                if ("HOTEL_ID".equals(line.trim())) {
                    continue;
                }
                bexHotelIDs.add(Integer.parseInt(line.trim()));
            }
        }
        Assert.assertNotNull(bexHotelIDs);
        Assert.assertTrue(CollectionUtils.isNotEmpty(bexHotelIDs));
        System.out.println("Bex hotel id count: " + bexHotelIDs.size());

        Set<Integer> vrHotelIDs = Sets.newHashSet();
        try (BufferedReader br = getReader(paths[1])) {
            String line;
            while ((line = br.readLine()) != null) {
                if ("HOTEL_ID".equals(line.trim())) {
                    continue;
                }
                vrHotelIDs.add(Integer.parseInt(line.trim()));
            }
        }
        Assert.assertNotNull(vrHotelIDs);
        Assert.assertTrue(CollectionUtils.isNotEmpty(vrHotelIDs));
        System.out.println("VR hotel id count: " + vrHotelIDs.size());

        Set<Integer> blackHotelIDs = Sets.newHashSet();
        for (Integer bexHotelID : bexHotelIDs) {
            if (vrHotelIDs.contains(bexHotelID)) {
                blackHotelIDs.add(bexHotelID);
            }
        }
        System.out.println("Black hotel id count: " + blackHotelIDs.size());
//        List<Integer> toWriteList = Lists.newArrayList(blackHotelIDs);
//        toWriteList.sort(Comparator.comparingInt(v -> v));
//        try (BufferedWriter bw = getWriter("BalckHotelIDs.tsv")) {
//            bw.write("HOTEL_ID");
//            bw.newLine();
//            for (Integer blackHotelID : toWriteList) {
//                bw.write(String.valueOf(blackHotelID));
//                bw.newLine();
//            }
//            bw.flush();
//        }
        List<Integer> sampleList = sampleList = Lists.newArrayList();
        ;
        SecureRandom secureRandom = new SecureRandom();
        try (BufferedWriter bw = getWriter("SamplingStatisticsResult.tsv")) {
            for (Integer blackHotelID : blackHotelIDs) {
                sampleList.add(blackHotelID);
                if (sampleList.size() % 1000 == 0) {
                    bw.write(String.valueOf(sampleList.get(secureRandom.nextInt(1000))));
                    bw.newLine();
                    sampleList = Lists.newArrayList();
                }
            }
            bw.flush();
        }

    }

    @Test
    public void test07() throws Exception {
        String[] paths = {"ews-hotels-to-expedia-id-mapping.xml", "ControlFile-GOOGLE-BEX-PRICE-ROLLING-OUT_10000.tsv"};
        Map<String, String> hotelIdMap = Maps.newHashMap();
        try (BufferedReader br = getReader(paths[0])) {
            String line;
            Pattern pattern = Pattern.compile("^<Entry hcomId=\"(\\d+)\" expediaId=\"(\\d+)\".*$");
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.trim());
                while (matcher.find()) {
                    String hcomId = matcher.group(1);
                    String ecomId = matcher.group(2);
                    hotelIdMap.put(ecomId, hcomId);
                }
            }
        }
        Assert.assertNotNull(hotelIdMap);
        Assert.assertEquals(791141, hotelIdMap.size());
        List<Integer> ecomHotelIds = Lists.newArrayList();
        Set<Integer> noRepeat = Sets.newHashSet();
        try (BufferedReader br = getReader(paths[1])) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("HOTEL_ID")) {
                    continue;
                }
                Integer hotelId = Integer.parseInt(line.trim());
                if (noRepeat.add(hotelId)) {
                    ecomHotelIds.add(hotelId);
                }
            }
        }
        Assert.assertNotNull(ecomHotelIds);
        Assert.assertEquals(9999, ecomHotelIds.size());
        int lineCount = 0;
        try (BufferedWriter bw = getWriter("HcomHotelIds.tsv")) {
            bw.write("ecomId\thcomId");
            bw.newLine();
            for (Integer ecomHotelId : ecomHotelIds) {
                String hcomHotelId = hotelIdMap.get(String.valueOf(ecomHotelId));
                if (hcomHotelId != null) {
                    bw.write(String.valueOf(ecomHotelId) + "\t" + hcomHotelId);
                    bw.newLine();
                    lineCount++;
                }
                if (lineCount == 500) {
                    break;
                }
            }
            bw.flush();
        }
        Assert.assertEquals(500, lineCount);
    }

    @Test
    public void test08() throws Exception {
        String[] paths = {"ews-hotels-to-expedia-id-mapping.xml"};
        Map<String, String> hcomToEcomMap = Maps.newHashMap();
        Map<String, String> ecomToHcomMap = Maps.newHashMap();
        try (BufferedReader br = getReader(paths[0])) {
            String line;
            Pattern pattern = Pattern.compile("^<Entry hcomId=\"(\\d+)\" expediaId=\"(\\d+)\".*$");
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.trim());
                while (matcher.find()) {
                    String hcomId = matcher.group(1);
                    String ecomId = matcher.group(2);
                    hcomToEcomMap.put(hcomId, ecomId);
                    ecomToHcomMap.put(ecomId, hcomId);
                }
            }
        }
        Assert.assertNotNull(hcomToEcomMap);
        Assert.assertNotNull(ecomToHcomMap);
        String hotelIdStr = "291435,264057,345663,264776,291434,485642,767919968,189985,701676,240922,887586528,341894," +
                "235521,416116,416115,426621792,236815,324257,122968,237460,484904,1285545024,200116,374818,600413696," +
                "224269,441179,315345,120181,130847,308255,326060,152989,224793,620800,490069,544022,644010,108723," +
                "495272,181328,124574,35099136,263227,671674176,689123,107590,170861,631783,254342,369844,469262," +
                "112199,138863,113001,600121,307899,363785,542391,621129,403932736,210885,237042,510980,682974496," +
                "332635,374758,234093,126775,236947";
        String[] hotelIds = hotelIdStr.split(",");
        StringBuilder sb = new StringBuilder();
        for (String hotelId : hotelIds) {
            sb.append(hcomToEcomMap.get(hotelId)).append(",");
        }
        System.out.println(sb.toString());
    }

    @Test
    public void test09() throws Exception {
        String[] paths = {"countryCodeMapping.txt", "UserCountryCodeMapping.json"};
        Map<String, String> countryCodeMap = Maps.newHashMap();
        try (BufferedReader br = getReader(paths[0])) {
            String line;
            Pattern pattern = Pattern.compile("^hotel/alpha2CountryCode\\.(\\d+)=(\\w+)$");
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.trim());
                while (matcher.find()) {
                    String tpId = matcher.group(1);
                    String countryCode = matcher.group(2);
                    countryCodeMap.put(tpId, countryCode);
                }
            }
        }
        Assert.assertNotNull(countryCodeMap);
        try (BufferedWriter bw = getWriter(paths[1])) {
            bw.write(new ObjectMapper().writeValueAsString(countryCodeMap));
            bw.newLine();
            bw.flush();
        }
        System.out.println("End..");
    }

    @Test
    public void test10() throws Exception {
        String[] paths = {"ControlFile-EDE-GOOGLE-BEX-PRICE.tsv", "HotelGroup_Google_ECOM_us.csv"};
        Random random = new Random();
        int score = random.nextInt(50) + 1;
        int capacity = 0;
        int lineCount = 0;
        try (BufferedReader br = getReader(paths[0]);
             BufferedWriter bw = getWriter(paths[1])) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("HOTEL_ID")) {
                    bw.write("hotelGroupName,hotelId,threshold");
                    bw.newLine();
                    continue;
                }
                lineCount++;
                if (lineCount > (capacity * 50) && lineCount % score == 0) {
                    bw.write("BlacklistHotelGroupZero," + line.trim() + ",100");
                    bw.newLine();
                    score = random.nextInt(50) + 1;
                    capacity++;
                }
            }
            bw.flush();
        }
    }

    @Test
    public void test11() throws Exception {
        String path = "review_content_data.tsv";
        int count = 1000000;
        Random random = new Random();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (BufferedWriter bw = getWriter(path)) {
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < 10; j++) {
                    String sb = i +
                            "\t" + "Expedia" +
                            "\t" + "en" +
                            "\t" + (random.nextInt(5) + 1) +
                            "\t" + UUID.randomUUID().toString() +
                            "\t" + LocalDateTime.now().format(dateTimeFormatter);
                    bw.write(sb);
                    bw.newLine();
                }
            }
            bw.flush();
        }
    }

    @Test
    public void test12() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 200; i++) {
            sb.append(random.nextInt(1000000)).append(",");
        }
        System.out.println(sb.toString());
    }

    @Test
    public void test13() throws Exception {
//        String[] paths = {"ews-hotels-to-expedia-id-mapping.xml",
//                "ControlFile-EDE-GOOGLE-HCOM-PRICE.tsv",
//                "review_content_data_Expedia_de.tsv",
//                "review_content_data_Hotels_de.tsv"};

//        String[] paths = {"ews-hotels-to-expedia-id-mapping.xml",
//                "ControlFile-EDE-GOOGLE-HCOM-PRICE.tsv",
//                "review_content_data_Expedia_en.tsv",
//                "review_content_data_Hotels_en.tsv"};
//        Map<String, String> hotelIdMap = Maps.newHashMap();
//        try (BufferedReader br = getReader(paths[0])) {
//            String line;
//            Pattern pattern = Pattern.compile("^<Entry hcomId=\"(\\d+)\" expediaId=\"(\\d+)\".*$");
//            while ((line = br.readLine()) != null) {
//                Matcher matcher = pattern.matcher(line.trim());
//                while (matcher.find()) {
//                    String hcomId = matcher.group(1);
//                    String ecomId = matcher.group(2);
//                    hotelIdMap.put(hcomId, ecomId);
//                }
//            }
//        }
//        Assert.assertNotNull(hotelIdMap);

        String[] paths = {
                "ControlFile-EDE-GOOGLE-BEX-PRICE.tsv",
                "review_content_data_Expedia_sv.tsv",
                "review_content_data_Expedia_sv_2.tsv"
        };
        int k = 1;

        List<String> ecomHotelIds = new ArrayList<>();
        try (BufferedReader br = getReader(paths[0])) {
            String line;
            while ((line = br.readLine()) != null) {
                if (Objects.equals(line, "HOTEL_ID")) {
                    continue;
                }
//                String ecomHotelId = hotelIdMap.get(line.trim());
                String ecomHotelId = line.trim();
                if (ecomHotelId != null) {
                    ecomHotelIds.add(ecomHotelId);
                }
            }
        }
        List<String[]> datas = new ArrayList<>();
        try (BufferedReader br = getReader(paths[1])) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columnsData = line.split("\t");
                if (columnsData.length == 5) {
                    datas.add(columnsData);
                }
            }
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Random random = new Random();
        try (BufferedWriter bw = getWriter(paths[2])) {
            for (String ecomHotelId : ecomHotelIds) {
                LocalDate localDate = null;
                for (int i = 0; i < 10; i++) {
                    String[] data = datas.get(random.nextInt(datas.size()));
                    if (localDate == null) {
                        localDate = LocalDate.parse(data[4], dateTimeFormatter);
                    } else {
                        localDate = localDate.minusDays(1);
                    }
                    String datetime = localDate.atTime(0, 0, 0).format(dateTimeFormatter2);
                    String str = ecomHotelId + "\t"
                            + data[1] + "\t"
                            + (Integer.parseInt(data[2]) * k) + "\t"
                            + data[3] + "\t"
                            + datetime;
                    bw.write(str);
                    bw.newLine();
                }
            }
            bw.flush();
        }

    }

    @Test
    public void test14() throws Exception {
        String path = "review_content_data_Expedia_en (1).tsv";
        int count = 0;
        try (BufferedReader br = getReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (count++ >= 10) {
                    break;
                }
                System.out.println(line);
            }
        }
    }

    @Test
    public void test15() throws Exception {
        String paht = "review_content_data_Expedia_en.tsv";
        System.out.println("A\001C\003B\002");
        try (BufferedReader br = getReader(paht)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("No one here to check me in at 11:15 AM")) {
                    System.out.println(line);
//                    System.out.println(StringEscapeUtils.escapeCsv(line));
                    return;
                }
            }
        }
    }

    @Test
    public void test16() throws Exception {
        String temp = "temp/";
        String temp2 = "temp2/";
        String paths[] = new String[]{
                "review_content_data_Expedia_de.tsv",
                "review_content_data_Expedia_en.tsv",
                "review_content_data_Expedia_es.tsv",
                "review_content_data_Expedia_fr.tsv",
                "review_content_data_Expedia_sv.tsv",
                "review_content_data_Hotels_de.tsv",
                "review_content_data_Hotels_en.tsv"
        };
        for (String path : paths) {
            try (BufferedReader br = getReader(temp + path);
                 BufferedWriter bw = getWriter(temp2 + path)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns;
                    if ((columns = line.split("\t")).length != 5) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < columns.length; i++) {
                            sb.append(columns[i]);
                            if (i <= 2 || i == columns.length - 2) {
                                sb.append("\u0001");
                            } else if (i < (columns.length - 2)) {
                                sb.append(columns[i]).append("\t");
                            }
                        }
                        line = sb.toString();
                        System.out.println(line);
                    } else {
                        line = line.replace("\t", "\u0001");
                    }
                    bw.write(line);
                    bw.newLine();
                }
                bw.flush();
            }
        }
    }

    @Test
    public void test17() throws Exception {
        String a = "��";
        byte[] as = a.getBytes(StandardCharsets.US_ASCII);
//        System.out.println(System.getProperty("file.encoding"));
        System.out.println(Arrays.toString(as));
        System.out.println(new String(as, StandardCharsets.UTF_8));
//        byte[] bs = a.getBytes(StandardCharsets.UTF_8);
//        byte[] cs = a.getBytes(StandardCharsets.ISO_8859_1);
//        try (ByteArrayInputStream bais = new ByteArrayInputStream(as);
//            FileOutputStream fos = new FileOutputStream(BASE_PATH + "a.txt", true)) {
//            byte[] buffer = new byte[1024];
//            int marker;
//            while ((marker = bais.read(buffer)) != -1) {
//                fos.write(buffer, 0, marker);
//            }
//            fos.flush();
//        }
//        try (ByteArrayInputStream bais = new ByteArrayInputStream(bs);
//             FileOutputStream fos = new FileOutputStream(BASE_PATH + "a.txt", true)) {
//            byte[] buffer = new byte[1024];
//            int marker;
//            while ((marker = bais.read(buffer)) != -1) {
//                fos.write(buffer, 0, marker);
//            }
//            fos.flush();
//        }
//        try (ByteArrayInputStream bais = new ByteArrayInputStream(cs);
//             FileOutputStream fos = new FileOutputStream(BASE_PATH + "a.txt", true)) {
//            byte[] buffer = new byte[1024];
//            int marker;
//            while ((marker = bais.read(buffer)) != -1) {
//                fos.write(buffer, 0, marker);
//            }
//            fos.flush();
//        }
    }

    @Test
    public void test18() throws Exception {
        String[] path = {"ews-hotel-static-data.csv",
                "ews-hotel-static-ecomid-destinationid-mapping.csv",
                "ews-hotel-static-ecomid-hcomid-mapping.csv"
        };
        Map<String, String> newDestinationIdMapping = new HashMap<>();
        try (BufferedReader br = getReader(path[1])) {
            String line = br.readLine();
            if (line != null) {
                while ((line = br.readLine()) != null) {
                    String[] lineArray = line.trim().split(",", -1);
                    String key = lineArray[0].trim();
                    String value = lineArray[1].trim();
                    newDestinationIdMapping.put(key, value);
                }
            }
        }

        Map<String, String> newHotelIdMapping = new HashMap<>();
        try (BufferedReader br = getReader(path[2])) {
            String line = br.readLine();
            if (line != null) {
                while ((line = br.readLine()) != null) {
                    String[] lineArray = line.trim().split(",", -1);
                    String key = lineArray[1].trim();
                    String value = lineArray[0].trim();
                    newHotelIdMapping.put(key, value);
                }
            }
        }
        Assert.assertTrue(MapUtils.isNotEmpty(newDestinationIdMapping));
        Assert.assertTrue(MapUtils.isNotEmpty(newHotelIdMapping));

        int missDestinationIdCount = 0;
        int notMatchDestinationIdCount = 0;
        int matchDestinationIdCount = 0;
        int oldDestinationIdCount = 0;
        int missHotelIdCount = 0;
        int notMatchHotelIdCount = 0;
        int matchHotelIdCount = 0;
        int oldHotelIdCount = 0;
        try (BufferedReader br = getReader(path[0])) {
            String line = br.readLine();
            if (line != null) {
                while ((line = br.readLine()) != null) {
                    String[] lineArray = line.trim().split(",", -1);
                    if (StringUtils.isNotBlank(lineArray[2].trim()) && StringUtils.isNotBlank(lineArray[1].trim())) {
                        oldDestinationIdCount++;
                        String key = lineArray[2].trim();
                        String value = lineArray[1].trim();
                        if (!newDestinationIdMapping.containsKey(key)) {
                            missDestinationIdCount++;
                        } else if (!Objects.equals(newDestinationIdMapping.get(key), value)) {
                            notMatchDestinationIdCount++;
                            if (notMatchDestinationIdCount < 10) {
                                System.out.println("New: " + key + " -> " + newDestinationIdMapping.get(key) +
                                        ", Old: " + key + " -> " + value);
                            }
                        } else {
                            matchDestinationIdCount++;
                        }
                    }
                    if (StringUtils.isNotBlank(lineArray[3].trim()) && StringUtils.isNotBlank(lineArray[2].trim())) {
                        oldHotelIdCount++;
                        String key = lineArray[3].trim();
                        String value = lineArray[2].trim();
                        if (!newHotelIdMapping.containsKey(key)) {
                            missHotelIdCount++;
                        } else if (!Objects.equals(newHotelIdMapping.get(key), value)) {
                            notMatchHotelIdCount++;
                        } else {
                            matchHotelIdCount++;
                        }
                    }
                }
            }
        }

        System.out.println("=============================================================================================");
        System.out.println("Destination id mapping size compare - new compare to old: new->" + newDestinationIdMapping.size() + ", old->" + oldDestinationIdCount);
        System.out.println("Destination id mapping miss count - new compare to old: " + missDestinationIdCount);
        System.out.println("Destination id mapping not match count - new compare to old: " + notMatchDestinationIdCount);
        System.out.println("Destination id mapping match count - new compare to old: " + matchDestinationIdCount);
        System.out.println("Sum: missCount(" + missDestinationIdCount + ") + notMatchCount(" + notMatchDestinationIdCount + ") " +
                "+ matchCount(" + matchDestinationIdCount + ") = "
                + (missDestinationIdCount + notMatchDestinationIdCount + matchDestinationIdCount) + " = " +
                "oldDestinationIdMappingSize(" + oldDestinationIdCount + ")");
        System.out.println("=============================================================================================");
        System.out.println("Hotel id mapping size compare - new compare to old: new->" + newHotelIdMapping.size() + ", old->" + oldHotelIdCount);
        System.out.println("Hotel id mapping miss count - new compare to old: " + missHotelIdCount);
        System.out.println("Hotel id mapping not match count - new compare to old: " + notMatchHotelIdCount);
        System.out.println("Hotel id mapping match count - new compare to old: " + matchHotelIdCount);
        System.out.println("Sum: missCount(" + missHotelIdCount + ") + notMatchCount(" + notMatchHotelIdCount + ") " +
                "+ matchCount(" + matchHotelIdCount + ") = "
                + (missHotelIdCount + notMatchHotelIdCount + matchHotelIdCount) + " = " +
                "oldHotelIdMappingSize(" + oldHotelIdCount + ")");
        System.out.println("=============================================================================================");
    }

    @Test
    public void test19() throws Exception {
        String[] paths = new String[] {
                "orbitz-local-reviews-hotels-en_us_old.xml",
                "orbitz-local-reviews-hotels-en_us_new.xml"
        };
        class Review {
            float reviewRating;
            int reviewCount;
            int reviewNumber;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Review review = (Review) o;
                return Float.compare(review.reviewRating, reviewRating) == 0 &&
                        reviewCount == review.reviewCount &&
                        reviewNumber == review.reviewNumber;
            }

            public float getReviewRating() {
                return reviewRating;
            }

            public void setReviewRating(float reviewRating) {
                this.reviewRating = reviewRating;
            }

            public int getReviewCount() {
                return reviewCount;
            }

            public void setReviewCount(int reviewCount) {
                this.reviewCount = reviewCount;
            }

            public int getReviewNumber() {
                return reviewNumber;
            }

            public void setReviewNumber(int reviewNumber) {
                this.reviewNumber = reviewNumber;
            }
        }
        Pattern idPattern = Pattern.compile("^[\\w\\W]*<id>(\\d+)</id>[\\w\\W]*$");
        Pattern reviewPattern = Pattern.compile("</review>");
        Pattern reviewNumberPattern = Pattern.compile("^[\\w\\W]*<attr name=\"num_reviews\">(\\d+)</attr>[\\w\\W]*$");
        Pattern reviewRatingPattern = Pattern.compile("^[\\w\\W]*<attr name=\"rating\">(\\d+\\.\\d+)</attr>[\\w\\W]*$");
        Map<Integer, Review> oldReviews = new HashMap<>();
        Map<Integer, Review> newReviews = new HashMap<>();
        for (int i = 0; i < 2; i++) {
            try (BufferedReader br = getReader(paths[i])) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher idMatcher = idPattern.matcher(line);
                    Matcher reviewMatcher = reviewPattern.matcher(line);
                    Matcher reviewNumberMatcher = reviewNumberPattern.matcher(line);
                    Matcher reviewRatingMatcher = reviewRatingPattern.matcher(line);
                    if (!idMatcher.matches()) {
                        continue;
                    }
                    idMatcher.reset();
                    Integer hotelId = null;
                    while (idMatcher.find()) {
                        hotelId = Integer.parseInt(idMatcher.group(1));
                    }
                    int reviewCount = 0;
                    while (reviewMatcher.find()) {
                        reviewCount++;
                    }
                    int reviewNumber = 0;
                    while (reviewNumberMatcher.find()) {
                        reviewNumber = Integer.parseInt(reviewNumberMatcher.group(1));
                    }
                    float reviewRating = 0.0f;
                    while (reviewRatingMatcher.find()) {
                        reviewRating = Float.parseFloat(reviewRatingMatcher.group(1));
                    }
                    if (hotelId != null) {
                        Review review = new Review();
                        review.reviewCount = reviewCount;
                        review.reviewNumber = reviewNumber;
                        review.reviewRating = reviewRating;
                        if (i == 0) {
                            oldReviews.put(hotelId, review);
                        } else {
                            newReviews.put(hotelId, review);
                        }
                    }
                }
            }
        }
        Assert.assertTrue(MapUtils.isNotEmpty(oldReviews));
        Assert.assertTrue(MapUtils.isNotEmpty(newReviews));
        System.out.println("Old hotel number:" + oldReviews.size());
        System.out.println("New hotel number:" + newReviews.size());
        System.out.println("================");
        Set<Integer> missHotelInOld = new HashSet<>(oldReviews.keySet());
        missHotelInOld.removeAll(newReviews.keySet());
        Set<Integer> missHotelInNew = new HashSet<>(newReviews.keySet());
        missHotelInNew.removeAll(oldReviews.keySet());
        System.out.println("Miss hotel number in old:" + missHotelInOld.size());
        System.out.println("Miss hotel number in new:" + missHotelInNew.size());
        int matchSize = 0;
        Map<Integer, Pair<Review, Review>> notMatchReview = new HashMap<>();
        for (Map.Entry<Integer, Review> entry : newReviews.entrySet()) {
            if (missHotelInOld.contains(entry.getKey()) || missHotelInNew.contains(entry.getKey())) {
                continue;
            }
            Review oldReview = oldReviews.get(entry.getKey());
            Review newReview = entry.getValue();
            if (oldReview.equals(newReview)) {
                matchSize++;
            } else {
                notMatchReview.put(entry.getKey(), Pair.of(oldReview, newReview));
            }
        }
        System.out.println("Match count:" + matchSize);
        System.out.println("Not match count:" + notMatchReview.size());
        ObjectMapper om = new ObjectMapper();
        try (BufferedWriter bw = getWriter("NotMatch.txt")) {
            String missHotelId = "Hotel id not in the old feed file(number:" + missHotelInOld.size() + "):";
            if (CollectionUtils.isNotEmpty(missHotelInOld)) {
                missHotelId += "\n" + missHotelInOld.stream().map(String::valueOf).collect(Collectors.joining(","));
            } else {
                missHotelId += "No hotel ids";
            }
            bw.write(missHotelId);
            bw.newLine();
            bw.newLine();
            missHotelId = "Hotel id not in the new feed file(number:" + missHotelInNew.size() + "):";
            if (CollectionUtils.isNotEmpty(missHotelInNew)) {
                missHotelId += "\n" + missHotelInNew.stream().map(String::valueOf).collect(Collectors.joining(","));
            } else {
                missHotelId += "No hotel ids";
            }
            bw.write(missHotelId);
            bw.newLine();
            bw.write("====================Not match data============================");
            for (Map.Entry<Integer, Pair<Review, Review>> entry : notMatchReview.entrySet()) {
                String str = entry.getKey() + ":\n" + om.writeValueAsString(entry.getValue().getLeft()) + "\n" +
                        om.writeValueAsString(entry.getValue().getRight());
                bw.write(str);
                bw.newLine();
                bw.newLine();
            }
            bw.flush();
        }
    }

    @Test
    public void test20() throws Exception {
        String[] paths = new String[] {
                "HA-Vrbo_ListingSourceSite-BrandID-Map_03Sep2019.xlsx",
                "ews-hotel-static-listingsourcesite-brandid-mapping.csv"
        };
        String lineTemplate = "%s,%s,%s,%s,%s,%s";
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(BASE_PATH + paths[0]));
            BufferedWriter bw = getWriter(paths[1])) {
            String firstLine = String.format(lineTemplate, "ListingSourceSite(AliasName)", "BrandId", "Name", "Locale",
                    "HumanReadableName", "Notes");
            bw.write(firstLine);
            bw.newLine();
            Sheet sheet = wb.getSheetAt(0);
            Assert.assertNotNull(sheet);
            Iterator<Row> rowsIterator = sheet.rowIterator();
            if (rowsIterator.hasNext()) {
                rowsIterator.next();
            }
            while (rowsIterator.hasNext()) {
                Row row = rowsIterator.next();
                Object[] values = new Object[6];
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null) {
                        values[i] = "";
                        continue;
                    }
                    String value;
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            value = String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
                            break;
                        case STRING:
                            value = cell.getStringCellValue();
                            break;
                        default:
                            value = "";
                    }
                    values[i] = value.trim();
                }
                String content = String.format(lineTemplate, values);
                bw.write(content);
                bw.newLine();
            }
            bw.flush();
        }
    }

    @Test
    public void test21() throws Exception {
        String path = BASE_PATH + "BrandReviewSummary-2020-12-18.tar.gz";
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(path));
             ArchiveInputStream acis = new ArchiveStreamFactory().createArchiveInputStream("tar", gzis);
             BufferedReader br = new BufferedReader(new InputStreamReader(gzis))) {
            TarArchiveEntry tarArchiveEntry = (TarArchiveEntry) acis.getNextEntry();
            int count = 0;
            int index1 = 0;
            int index2 = 0;
            if (tarArchiveEntry != null) {
                String line;
                while ((line = br.readLine()) != null) {
//                    count++;
//                    if (count > 10) {
//                        break;
//                    }
                    if (line.contains("917519-expedia")) {
                        index1 = count;
                        System.out.println(line);
                    } else if (line.contains("917519-expedia"))
                    count++;
                }
                System.out.println("Index: " + count);
            }
        }
    }

    @Test

    private BufferedWriter getWriter(String path) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(BASE_PATH + path), StandardCharsets.UTF_8));
    }

    private BufferedReader getReader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(BASE_PATH + path), StandardCharsets.UTF_8));
    }

}
