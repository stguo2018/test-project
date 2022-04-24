package com.ews.stguo.testproject.utils.file;

import com.ews.stguo.testproject.TestProjectApplicationTests;
import com.ews.stguo.testproject.utils.file.filereader.FileReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class FileOperateUtilTest extends TestProjectApplicationTests {

    private void readFile(String filePath) throws Exception {
        for (FileReader fileReader : FileOperateUtil.readFile(filePath)) {
            try (FileReader fr = fileReader) {
                readLine(fr);
            }
        }
    }

    private void readFileWithSingleton(String filePath) throws Exception {
        try (FileReader fr = FileOperateUtil.readFileWithSingleton(filePath)) {
            readLine(fr);
        }
    }

    private void readLine(FileReader fileReader) throws Exception {
        Object line;
        int count = 0;
        String padding = StringUtils.repeat('=', 150);
        String title = StringUtils.center(fileReader.getReaderName(), 150, "%");
        Object[] raw = new Object[]{padding, title, padding};
        System.out.println(StringUtils.join(raw, "\r\n"));
        while ((line = fileReader.readLine()) != null) {
            count++;
            if (count < 1000) {
                System.out.println(line);
            } else {
                break;
            }
        }
    }

    @Test
    public void testNormalFileReader() throws Exception {
        String path = "E:\\workspace\\test-project\\bin\\expedia-local-vr-reviews-hotels-sv_se.xml";
        readFile(path);
    }

    @Test
    public void testZipFileReader() throws Exception {
        String path = "E:\\workspace\\test-project\\bin\\expedia-local-vr.xml.zip";
        readFile(path);
    }

    @Test
    public void testZipFileReader2() throws Exception {
        String path = "E:\\workspace\\test-project\\bin\\bin.zip";
        readFile(path);
    }

    @Test
    public void testZipFileReader3() throws Exception {
        String path = "E:\\workspace\\test-project\\bin\\expedia-local-vr.xml.zip";
        readFileWithSingleton(path);
    }

    @Test
    public void testZipFileReader4() throws Exception {
        String path = "E:\\workspace\\test-project\\bin\\hotels-local.xml.zip";
        readFile(path);
    }

    @Test
    public void testGzFileReader() throws Exception {
        String path = "E:\\workspace\\test-project\\bin\\ControlFile-EDE-GOOGLE-VR.tsv.gz";
        readFile(path);
    }

    private static Set<String> attributes = new HashSet<>(Arrays.asList("air_conditioned", "child_friendly", "has_airport_shuttle",
            "has_hot_tub", "kitchen_availability", "parking_type", "pets_allowed", "smoke_free_property", "wheelchair_accessible",
            "wifi_type"));

    @Test
    public void testReadAmenityFile() throws Exception {
        //String path = "C:\\Users\\stguo\\Downloads\\hotels-local-vr.xml.zip";
        String path = "C:\\Users\\stguo\\Downloads\\expedia-local-vr.xml.zip";
        Map<String, List<Record>> recordMap = new HashMap<>();
        for (FileReader fileReader : FileOperateUtil.readFile(path)) {
            try (FileReader fr = fileReader) {
                String line;
                while ((line = fr.readLine()) != null) {
                    setRecord(recordMap, line);
                }
            }
        }
        System.out.println(new ObjectMapper().writeValueAsString(recordMap));
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\stguo\\Downloads\\result_ecom.txt")))) {
            for (Map.Entry<String, List<Record>> stringListEntry : recordMap.entrySet()) {
                bw.write(stringListEntry.getKey() + ":");
                bw.newLine();
                for (Record record : stringListEntry.getValue()) {
                    bw.write(record.getHotelId() + ": " + record.getAttributeValue());
                    bw.newLine();
                }
                bw.newLine();
            }
            bw.flush();
        }
    }

    private void setRecord(Map<String, List<Record>> recordMap, String line) {
        String startLabel = "<id>";
        if (line == null || !line.contains(startLabel)) {
            return;
        }
        String endLabel = "</id>";
        String hotelId = line.substring(line.indexOf(startLabel) + startLabel.length(), line.indexOf(endLabel));
        endLabel = "</attr>";
        for (String attribute : attributes) {
            if (line.contains(attribute)) {
                startLabel = "<attr name=\"" + attribute + "\">";
                List<Record> records = recordMap.computeIfAbsent(attribute, k -> new ArrayList<>());
                String value = line.substring(line.indexOf(startLabel) + startLabel.length());
                value = value.substring(0, value.indexOf(endLabel));
                RecordBuilder.buildRecord(attribute, Integer.parseInt(hotelId), value, records);
            }
        }
    }

    static class Record {

        private Integer hotelId;
        private String attributeValue;

        public Integer getHotelId() {
            return hotelId;
        }

        public Record setHotelId(Integer hotelId) {
            this.hotelId = hotelId;
            return this;
        }

        public String getAttributeValue() {
            return attributeValue;
        }

        public Record setAttributeValue(String attributeValue) {
            this.attributeValue = attributeValue;
            return this;
        }
    }

    static class RecordBuilder {
        private static int maxCount = 10;

        private static final Map<String, Integer> countMap = new HashMap<>();

        public static void buildRecord(String attributeName, Integer hotelId, String attributeValue,List<Record> records) {
            Integer count = countMap.get(attributeName + "_" + attributeValue);
            if (count == null) {
                count = 0;
            }
            if (count < maxCount) {
                Record record = new Record();
                record.setHotelId(hotelId);
                record.setAttributeValue(attributeValue);
                records.add(record);
                count++;
                countMap.put(attributeName + "_" + attributeValue, count);
            }
        }
    }

    @Test
    public void testReadTsv() throws Exception {
        String path = "C:\\Users\\stguo\\Downloads\\hotels-local-reviews-hotels-en_us.xml (2).zip";
        for (FileReader fileReader : FileOperateUtil.readFile(path)) {
            try (FileReader fr = fileReader) {
                for (int i = 0; i < 10; i++) {
                    String line = fr.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println(fr.readLine());
                }
            }
        }
    }

    @Test
    public void testReadReviewControlFile() throws Exception {
        String path = "C:\\Users\\stguo\\Downloads\\expedia-local-reviews-hotels-en_us.xml (2).zip";
        for (FileReader fileReader : FileOperateUtil.readFile(path)) {
            try (FileReader fr = fileReader) {
                for (; ; ) {
                    String line = fr.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.contains("23904635")) {
                        System.out.println(line);
                        break;
                    }
                }
            }
        }
    }

}
