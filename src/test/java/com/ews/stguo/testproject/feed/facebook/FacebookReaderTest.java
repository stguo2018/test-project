package com.ews.stguo.testproject.feed.facebook;

import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class FacebookReaderTest {

    @Test
    public void test01() throws Exception {
        String fileName = "hotels-local-1.tsv";
        boolean head = true;
        int count = 0;
        List<String> missingCountryFiledProperties = new ArrayList<>();
        List<String> missingLatitudeFiledProperties = new ArrayList<>();
        List<String> missingLongitudeFiledProperties = new ArrayList<>();
        Map<String, Integer> countryCount = new HashMap<>();
        try (FacebookFileReader br = FacebookFileReader.readFile(fileName);
             BufferedWriter bw = RWFileUtils.getWriter("FB extract record.csv")) {
            bw.write(String.format("%s,%s,%s,%s", "HcomId", "Country", "Latitude", "Longitude"));
            bw.newLine();
            String[] columns;
            StringBuilder contentLine = new StringBuilder();
            while ((columns = br.readLineAsColumns()).length != 0) {
                if (head) {
                    for (int i = 0; i < columns.length; i++) {
                        if (head) {
                            head = false;
                        } else {
                            System.out.print(",");
                        }
                        System.out.print(columns[i] + "|" + i);
                    }
                    System.out.println();
                    contentLine.setLength(0);
                    continue;
                }
                String hotelId = columns[6];
                String address = columns[0];
                try {
                    String country = "";
                    if (missingCountryFiledProperties.size() < 10) {
                        String addressJson = address.substring(1, address.length() - 1).replace("\"\"", "\"");
                        JSONObject addr = new JSONObject(addressJson);
                        country = addr.optString("country");
                        countryCount.put(country, countryCount.getOrDefault(country, 0) + 1);
                        if (StringUtils.isBlank(country)) {
                            System.out.println(addressJson);
                            missingCountryFiledProperties.add(hotelId);
                        }
                    }
                    String latitude = columns[8];
                    float lo = Float.parseFloat(latitude);
                    if (missingLatitudeFiledProperties.size() < 10 && StringUtils.isBlank(latitude)) {
                        missingLatitudeFiledProperties.add(hotelId);
                    }
                    String longitude = columns[9];
                    float la = Float.parseFloat(longitude);
                    if (missingLongitudeFiledProperties.size() < 10 && StringUtils.isBlank(longitude)) {
                        missingLongitudeFiledProperties.add(hotelId);
                    }
                    country = StringUtils.isBlank(country) ? "NULL" : country;
                    latitude = StringUtils.isBlank(latitude) ? "NULL" : latitude;
                    longitude = StringUtils.isBlank(longitude) ? "NULL" : longitude;
                    bw.write(String.format("%s,%s,%s,%s", hotelId, country, latitude, longitude));
                    bw.newLine();
                    bw.flush();
                    if (missingCountryFiledProperties.size() == 10 && missingLatitudeFiledProperties.size() == 10 && missingLongitudeFiledProperties.size() == 10) {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("-----------------ERROR:" + contentLine);
                }
                contentLine.setLength(0);
            }
        }
        System.out.println(String.join(",", missingCountryFiledProperties));
        System.out.println(countryCount);
    }

    @Test
    public void testValidHcomFBFileFromControlFIle() throws Exception {
        Map<String, String> hcomToEcomIdMapping = ControlFileRWUtils.loadHotelIdStrByPaths("ews-hotel-static-ecomid-hcomid-mapping.csv")
                .stream().filter(s -> !StringUtils.equalsIgnoreCase(s, "ecomHotelId,hcomHotelId"))
                .map(s -> s.split(","))
                .collect(Collectors.toMap(cs -> cs[1], cs -> cs[0]));
        Set<String> ids = ControlFileRWUtils.loadHotelIdStrByPaths("hcom-feed-control-file.csv")
                .stream().map(line -> line.split(",")[0]).collect(Collectors.toSet());
        System.out.println("CountFileSize: " + ids.size());
        List<String> hotelIdsInFile = new ArrayList<>();
        List<String> additionalHotelIds = new ArrayList<>();
        try (FacebookFileReader facebookFileReader = FacebookFileReader.readFile("hotels-local-1.tsv")) {
            FacebookFileReader.DataLine dataLine = facebookFileReader.readDataLine(); // Skip first line.
            while ((dataLine = facebookFileReader.readDataLine()) != null) {
                String hotelId = hcomToEcomIdMapping.get(dataLine.getHotelId());
                if (hotelId != null) {
                    hotelIdsInFile.add(hotelId);
                    if (!ids.contains(hotelId)) {
                        additionalHotelIds.add(hotelId);
                    }
                } else {
                    System.out.println("No mapping: " + dataLine.getHotelId());
                }
            }
        }
        System.out.println("HotelSizeInFile: " + hotelIdsInFile.size());
        System.out.println("Additional HotelIds Size: " + additionalHotelIds.size());
        try (BufferedWriter bw = RWFileUtils.getWriter("AdditionalHotelIds.csv")) {
            for (String id : additionalHotelIds) {
                bw.write(id);
                bw.newLine();
            }
        }
        List<String> missedHotelIds = ListUtils.removeAll(ids, new HashSet<>(hotelIdsInFile));
        System.out.println("Missed HotelIds Size: " + missedHotelIds.size());
        try (BufferedWriter bw = RWFileUtils.getWriter("MissedHotelIds.csv")) {
            for (String id : missedHotelIds) {
                bw.write(id);
                bw.newLine();
            }
        }
        System.out.println(new BigDecimal(String.valueOf(hotelIdsInFile.size())).
                divide(new BigDecimal(String.valueOf(ids.size())), 6, RoundingMode.HALF_UP).toString());
    }

}
