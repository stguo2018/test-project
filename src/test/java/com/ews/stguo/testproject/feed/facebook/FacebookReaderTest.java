package com.ews.stguo.testproject.feed.facebook;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try (BufferedReader br = RWFileUtils.getReader(fileName);
             BufferedWriter bw = RWFileUtils.getWriter("FB extract record.csv")) {
            bw.write(String.format("%s,%s,%s,%s", "HcomId", "Country", "Latitude", "Longitude"));
            bw.newLine();
            String line;
            StringBuilder contentLine = new StringBuilder();
            while ((line = br.readLine()) != null) {
                contentLine.append(line.replace("\\", "").replace("\t\"\",\"\"postal_code\"\"", "\"\",\"\"postal_code\"\""));
                String[] columns = contentLine.toString().trim().split("\t");
                if (count++ < 10) {
//                    System.out.println(contentLine);
                }
                if (columns.length < 16) {
                    continue;
                }
                if (columns.length > 16) {
                    try {
                        while (!columns[1].startsWith("\"{\"\"iphone_url\"\":") && columns.length > 16) {
                            String[] newColumns = new String[columns.length-1];
                            int newIndex = 0;
                            for (int i = 0; i < columns.length; i++) {
                                newColumns[newIndex] = columns[i];
                                if (i == 0) {
                                    newColumns[newIndex] += columns[i + 1];
                                    i++;
                                }
                                newIndex++;
                            }
                            columns = newColumns;
                        }
                        while (((columns[11].equals("") && StringUtils.isNumeric(columns[12])) || !StringUtils.isNumeric(columns[11]))
                                && columns.length > 16) {
                            String[] newColumns = new String[columns.length-1];
                            int newIndex = 0;
                            for (int i = 0; i < columns.length; i++) {
                                newColumns[newIndex] = columns[i];
                                if (i == (11 - 1)) {
                                    newColumns[newIndex] += columns[i + 1];
                                    i++;
                                }
                                newIndex++;
                            }
                            columns = newColumns;
                        }
                    } catch (Exception e) {
                        System.out.println("-----------------ERROR:" + contentLine);
                    }
                }
                if (columns.length != 16) {
                    System.out.println(contentLine);
                }
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

}
