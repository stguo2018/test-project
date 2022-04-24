package com.ews.stguo.testproject.test.http;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LDSResponseTest {

    @Test
    public void test01() throws Exception {
        Map<Integer, Integer> hcomIdToEcomIdMapping = new HashMap<>();
        Map<Integer, Integer> ecomIdTohcomIdMapping = new HashMap<>();
        try (BufferedReader reader = RWFileUtils.getReader("ews-hotel-static-ecomid-hcomid-mapping.csv")) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] ids = line.split(",");
                hcomIdToEcomIdMapping.put(Integer.parseInt(ids[1]), Integer.parseInt(ids[0]));
                ecomIdTohcomIdMapping.put(Integer.parseInt(ids[0]), Integer.parseInt(ids[1]));
            }
        }
        Assert.assertEquals(1811678, hcomIdToEcomIdMapping.size());
        Assert.assertEquals(1811678, ecomIdTohcomIdMapping.size());
        System.out.println(hcomIdToEcomIdMapping.size());

        List<Integer> ecomHotelIds = new ArrayList<>();
        try (BufferedReader reader = RWFileUtils.getReader("ecom.txt")) {
            String line;
            while ((line = reader.readLine()) != null) {
                ecomHotelIds.add(Integer.parseInt(line));
            }
        }
        Assert.assertEquals(231115, ecomHotelIds.size());
        System.out.println(ecomHotelIds.size());

        List<Integer> hcomHotelIds = new ArrayList<>();
        int count = 0;
        try (BufferedReader reader = RWFileUtils.getReader("hcom.txt")) {
            String line;
            while ((line = reader.readLine()) != null) {
                Integer hcomId = Integer.parseInt(line);
                if (hcomIdToEcomIdMapping.containsKey(hcomId)) {
                    hcomHotelIds.add(hcomIdToEcomIdMapping.get(hcomId));
                    count++;
                }
            }
        }

        Assert.assertEquals(count, hcomHotelIds.size());
        System.out.println(hcomHotelIds.size());

//        try (BufferedWriter writer = RWFileUtils.getWriter("hcom-vr-control-file.csv")) {
//            writer.write("HOTEL_ID");
//            writer.newLine();
//            for (Integer hcomHotelId : hcomHotelIds) {
//                writer.write(String.valueOf(hcomHotelId));
//                writer.newLine();
//            }
//            writer.flush();
//        }

        Set<Integer> combinedHotelIds = new HashSet<>(ecomHotelIds);
        combinedHotelIds.addAll(hcomHotelIds);
        List<List<Integer>> partitions = Lists.partition(new ArrayList<>(combinedHotelIds), 100);
        RateLimiter rateLimiter = RateLimiter.create(10);
        String baseUrl = "https://localhost:44572/properties/shells?propertyIds=";
        CloseableHttpClient httpClient = createHttpClient();
        Map<Integer, Integer> structureTypeIdMapping = new HashMap<>();
        Map<Integer, List<Integer>> structureTypeIdMapping2 = new HashMap<>();
        for (List<Integer> partition : partitions) {
            String ldsUrl = baseUrl + Joiner.on(",").join(partition);
            HttpGet httpGet = new HttpGet(ldsUrl);
            httpGet.addHeader("User", "EWSLPP");
            httpGet.addHeader("Accept-Encoding", "gzip");
            httpGet.addHeader("request-id", UUID.randomUUID().toString());
            rateLimiter.acquire();
            try (CloseableHttpResponse response = httpClient.execute(httpGet);
                 BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String inputLine;
                    StringBuilder responseData = new StringBuilder();
                    while ((inputLine = br.readLine()) != null) {
                        responseData.append(inputLine);
                    }
                    JSONArray array = new JSONArray(responseData.toString());
                    if (array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            if (object != null) {
                                Integer structureTypeId = object.getInt("structureTypeID");
                                structureTypeIdMapping.put(object.getInt("propertyID"), structureTypeId);
                                List<Integer> hotelIds = structureTypeIdMapping2.get(structureTypeId);
                                if (hotelIds == null) {
                                    hotelIds = new ArrayList<>();
                                    structureTypeIdMapping2.putIfAbsent(structureTypeId, hotelIds);
                                }
                                hotelIds.add(object.getInt("propertyID"));
                            }
                        }
                    }
                } else {
                    System.out.println("Error: " + statusCode);
                }
            }
        }
        System.out.println(structureTypeIdMapping.size());

        Set<Integer> vrStructureTypeIdSet = new HashSet<>(Arrays.asList(7, 9, 10, 11, 14, 16, 17, 18, 22, 23, 24, 33));
        Map<Integer, List<Integer>> counter = new HashMap<>();
        int totalVRCount = 0;
        int totalNonVRCount = 0;
        int nullCount = 0;
        try (BufferedWriter writer1 = RWFileUtils.getWriter("ecom-VR.txt")) {
            for (Integer ecomHotelId : ecomHotelIds) {
                final Integer structureTypeId = structureTypeIdMapping.get(ecomHotelId);
                if (structureTypeId == null) {
                    nullCount++;
                    continue;
                }
                if (structureTypeIdMapping.containsKey(ecomHotelId) &&
                        vrStructureTypeIdSet.contains(structureTypeId)) {
                    writer1.write(ecomHotelId + "," + structureTypeIdMapping.get(ecomHotelId));
                    writer1.newLine();
                    writer1.flush();
                    totalVRCount++;
                } else {
                    List<Integer> hotelIds = counter.computeIfAbsent(structureTypeId, k -> new ArrayList<>());
                    hotelIds.add(ecomHotelId);
                    totalNonVRCount++;
                }
            }
        }
        try (BufferedWriter writer = RWFileUtils.getWriter("ecom-VR-record.txt");
             BufferedWriter writer2 = RWFileUtils.getWriter("ecom-NVR.txt")) {
            for (Map.Entry<Integer, List<Integer>> entry : counter.entrySet()) {
                double percent = (double) entry.getValue().size() / totalNonVRCount * 100;
                writer.write("StructureTypeId: " + entry.getKey() + ", Count: " + entry.getValue().size() +
                        ", Percent: " + percent + "%");
                writer.newLine();
            }
            writer.write("AllTotalCount: " + ecomHotelIds.size() + ", VR: " + totalVRCount
                    + ", NonVR: " + totalNonVRCount
                    + ", Null: " + nullCount);
            writer.newLine();
            writer.flush();
            for (int i = 0; i < 43; i++) {
                if (counter.get(i) == null) {
                    continue;
                }
                for (Integer ecomHotelId : counter.get(i)) {
                    writer2.write(ecomHotelId + "," + structureTypeIdMapping.get(ecomHotelId));
                    writer2.newLine();
                }
            }
            writer2.flush();
        }
        System.out.println("ecom done");

        counter = new HashMap<>();
        totalVRCount = 0;
        totalNonVRCount = 0;
        nullCount = 0;
        try (BufferedWriter writer1 = RWFileUtils.getWriter("hcom-VR.txt")) {
            for (Integer hcomHotelId : hcomHotelIds) {
                final Integer structureTypeId = structureTypeIdMapping.get(hcomHotelId);
                if (structureTypeId == null) {
                    nullCount++;
                    continue;
                }
                if (structureTypeIdMapping.containsKey(hcomHotelId) &&
                        vrStructureTypeIdSet.contains(structureTypeIdMapping.get(hcomHotelId))) {
                    writer1.write(hcomHotelId + "," + ecomIdTohcomIdMapping.get(hcomHotelId) + ","
                            + structureTypeIdMapping.get(hcomHotelId));
                    writer1.newLine();
                    writer1.flush();
                    totalVRCount++;
                } else {
                    List<Integer> hotelIds = counter.computeIfAbsent(structureTypeId, k -> new ArrayList<>());
                    hotelIds.add(hcomHotelId);
                    totalNonVRCount++;
                }
            }
        }
        try (BufferedWriter writer = RWFileUtils.getWriter("hcom-VR-record.txt");
             BufferedWriter writer2 = RWFileUtils.getWriter("hcom-NVR.txt")) {
            for (Map.Entry<Integer, List<Integer>> entry : counter.entrySet()) {
                double percent = (double) entry.getValue().size() / totalNonVRCount * 100;
                writer.write("StructureTypeId: " + entry.getKey() + ", Count: " + entry.getValue().size() +
                        ", Percent: " + percent + "%");
                writer.newLine();
            }
            writer.write("AllTotalCount: " + ecomHotelIds.size() + ", VR: " + totalVRCount
                    + ", NonVR: " + totalNonVRCount
                    + ", Null: " + nullCount);
            writer.newLine();
            writer.flush();
            for (int i = 0; i < 43; i++) {
                if (counter.get(i) == null) {
                    continue;
                }
                for (Integer hcomHotelId : counter.get(i)) {
                    writer2.write(hcomHotelId + "," + ecomIdTohcomIdMapping.get(hcomHotelId) + ","
                            + structureTypeIdMapping.get(hcomHotelId));
                    writer2.newLine();
                }
            }
            writer2.flush();
        }
        System.out.println("hcom done");
    }

    @Test
    public void test02() throws Exception {
        String id = "2021-4-8-1";
        String[] paths = new String[]{
                "feed-control-file.csv",
                "ControlFile-EDE-GOOGLE-BEX.tsv",
                "ControlFile-GOOGLE-VR-ECOM.tsv",
                "ControlFile-GOOGLE-VR-HCOM.tsv"
        };
        long startTime = System.currentTimeMillis();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>Starting load hotelIds...");
        Set<Integer> ecomVrHotelIds = new HashSet<>(getHotelIds(paths[2]));
        Set<Integer> ecomHotelIds = getHotelIds(paths[1], ecomVrHotelIds);
        Set<Integer> hcomVrHotelIds = new HashSet<>(getHotelIds(paths[3]));
        Set<Integer> hcomHotelIds = getHotelIds(paths[0], hcomVrHotelIds);
        Set<Integer> allHotelIds = new HashSet<>(ecomHotelIds);
        allHotelIds.addAll(hcomHotelIds);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>End load hotelIds, " + (System.currentTimeMillis() - startTime) + "ms");

        startTime = System.currentTimeMillis();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>Starting call LDS...");
        Map<Integer, List<Integer>> structureTypeIdsWithHotelIds = new HashMap<>();
        Set<Integer> ecomMissingLds = new HashSet<>(ecomHotelIds);
        Set<Integer> hcomMissingLds = new HashSet<>(hcomHotelIds);
        List<List<Integer>> partitions = Lists.partition(new ArrayList<>(allHotelIds), 10000);
        int count = 1;
        for (List<Integer> partition : partitions) {
            System.out.println("Executing call LDS, progress: " + count + "/" + partitions.size());
            List<JSONArray> allLdsResults = getAllLdsResults(partition);
            count++;
            Assert.assertTrue(CollectionUtils.isNotEmpty(allLdsResults));
            for (JSONArray array : allLdsResults) {
                if (array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        if (object != null) {
                            Integer structureTypeId = object.getInt("structureTypeID");
                            Integer propertyID = object.getInt("propertyID");
                            ecomMissingLds.remove(propertyID);
                            hcomMissingLds.remove(propertyID);
                            List<Integer> propertyIds = structureTypeIdsWithHotelIds
                                    .computeIfAbsent(structureTypeId, k -> new ArrayList<>());
                            propertyIds.add(propertyID);
                        }
                    }
                }
            }
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>End call LDS, " + (System.currentTimeMillis() - startTime) + "ms");

        startTime = System.currentTimeMillis();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>Starting generate record...");
        Set<Integer> vrStructureTypeIdSet = new HashSet<>(Arrays.asList(7, 9, 10, 11, 14, 16, 17, 18, 22, 23, 24, 33));
        try (BufferedWriter bwEcomRecord = RWFileUtils.getWriter(id + "/ecom hotel list record.txt");
             BufferedWriter bwHcomRecord = RWFileUtils.getWriter(id + "/hcom hotel list record.txt");
             BufferedWriter bwEcom = RWFileUtils.getWriter(id + "/ecom vr hotel list.txt");
             BufferedWriter bwEcom2 = RWFileUtils.getWriter(id + "/ecom cl hotel list.txt");
             BufferedWriter bwHcom = RWFileUtils.getWriter(id + "/hcom vr hotel list.txt");
             BufferedWriter bwHcom2 = RWFileUtils.getWriter(id + "/hcom cl hotel list.txt")) {
            int ecomValidateTotalCount = 0;
            int hcomValidateTotalCount = 0;
            int ecomInvalidateTotalCount = 0;
            int hcomInvalidateTotalCount = 0;
            for (int i = -1; i < 100; i++) {
                if ((i == -1 && CollectionUtils.isEmpty(structureTypeIdsWithHotelIds.get(null))) || CollectionUtils.isEmpty(structureTypeIdsWithHotelIds.get(i))) {
                    continue;
                }
                List<Integer> hotelIds = i == -1 ? structureTypeIdsWithHotelIds.get(null) : structureTypeIdsWithHotelIds.get(i);
                String p = i == -1 ? id + "/structureTypeId_null.txt" : id + "/structureTypeId_" + i + ".txt";
                try (BufferedWriter bw = RWFileUtils.getWriter(p)) {
                    int ecomVRHotelIdsCount = 0;
                    int ecomCLHotelIdsCount = 0;
                    int hcomVRHotelIdsCount = 0;
                    int hcomCLHotelIdsCount = 0;
                    for (Integer hotelId : hotelIds) {
                        // Write structureTypeId file.
                        bw.write(String.valueOf(hotelId));
                        bw.newLine();

                        if (ecomHotelIds.contains(hotelId)) {
                            if (vrStructureTypeIdSet.contains(i)) {
                                ecomVRHotelIdsCount++;
                                // Write ecom vr hotel list file.
                                bwEcom.write(String.format("%d,%d", hotelId, i));
                                bwEcom.newLine();
                            } else {
                                ecomCLHotelIdsCount++;
                                // Write ecom cl hotel list file.
                                bwEcom2.write(String.format("%d,%d", hotelId, i));
                                bwEcom2.newLine();
                            }
                        }

                        if (hcomHotelIds.contains(hotelId)) {
                            if (vrStructureTypeIdSet.contains(i)) {
                                hcomVRHotelIdsCount++;
                                // Write hcom vr hotel list file.
                                bwHcom.write(String.format("%d,%d", hotelId, i));
                                bwHcom.newLine();
                            } else {
                                hcomCLHotelIdsCount++;
                                // Write hcom cl hotel list file.
                                bwHcom2.write(String.format("%d,%d", hotelId, i));
                                bwHcom2.newLine();
                            }
                        }
                    }
                    bw.flush();
                    bwEcom.flush();
                    bwEcom2.flush();
                    bwHcom.flush();
                    bwHcom2.flush();
                    if (i != -1) {
                        ecomValidateTotalCount += (ecomVRHotelIdsCount + ecomCLHotelIdsCount);
                        hcomValidateTotalCount += (hcomVRHotelIdsCount + hcomCLHotelIdsCount);
                    } else {
                        ecomInvalidateTotalCount += (ecomVRHotelIdsCount + ecomCLHotelIdsCount);
                        hcomInvalidateTotalCount += (hcomVRHotelIdsCount + hcomCLHotelIdsCount);
                    }
                    bwEcomRecord.write(String.format("StructureTypeId: %d, Total: %d, VR: %d, CL: %d", i,
                            (ecomVRHotelIdsCount + ecomCLHotelIdsCount), ecomVRHotelIdsCount, ecomCLHotelIdsCount));
                    bwEcomRecord.newLine();
                    bwHcomRecord.write(String.format("StructureTypeId: %d, Total: %d, VR: %d, CL: %d", i,
                            (hcomVRHotelIdsCount + hcomCLHotelIdsCount), hcomVRHotelIdsCount, hcomCLHotelIdsCount));
                    bwHcomRecord.newLine();
                }
            }
            bwEcomRecord.write(String.format("TotalHotelIds: %d, ValidateHotelIds: %d, InvalidateHotelIds: %d, MissingHotelIds: %d",
                    ecomHotelIds.size(), ecomValidateTotalCount, ecomInvalidateTotalCount, ecomMissingLds.size()));
            bwEcomRecord.newLine();
            bwHcomRecord.write(String.format("TotalHotelIds: %d, ValidateHotelIds: %d, InvalidateHotelIds: %d, MissingHotelIds: %d",
                    hcomHotelIds.size(), hcomValidateTotalCount, hcomInvalidateTotalCount, hcomMissingLds.size()));
            bwHcomRecord.newLine();
            bwEcomRecord.flush();
            bwHcomRecord.flush();
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>End generate record, " + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Test
    public void test03() throws Exception {
        String path = "test/removed hotel id list.txt";
        Set<Integer> hotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(path);
        List<List<Integer>> partitions = Lists.partition(new ArrayList<>(hotelIds), 10000);
        Map<Integer, Map<Integer, String>> datas = new HashMap<>();
        int count = 1;
        for (List<Integer> partition : partitions) {
            System.out.println(count + "/" + partitions.size());
            count++;
            List<JSONArray> allLdsResults = getAllLdsResults(partition);
            for (JSONArray array : allLdsResults) {
                if (array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        if (object != null) {
                            Integer structureTypeId = object.getInt("structureTypeID");
                            Integer propertyID = object.getInt("propertyID");
                            String tspIds = getTspIds(object);
                            Map<Integer, String> map = datas.computeIfAbsent(structureTypeId, k -> new HashMap<>());
                            map.put(propertyID, String.format("%d,%d,%s", propertyID, structureTypeId, tspIds));
                        }
                    }
                }
            }
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("test/removed list.csv")) {
            bw.write("hotel_id,structure_type_id,tsp_ids");
            bw.newLine();
            Set<Integer> missingHotelIds = new HashSet<>(hotelIds);
            for (int i = 0; i < 44; i++) {
                if (datas.containsKey(i)) {
                    System.out.println("Size-" + i + ": " + datas.get(i).size());
                    for (Map.Entry<Integer, String> entry : datas.get(i).entrySet()) {
                        bw.write(entry.getValue());
                        bw.newLine();
                        missingHotelIds.remove(entry.getKey());
                    }
                }
            }
            for (Integer hotelId : new ArrayList<>(missingHotelIds)) {
                bw.write(String.format("%d,%s,%s", hotelId, "Unknown", "Unknown"));
                bw.newLine();
            }
            bw.flush();
        }
    }

    private Set<Integer> getHotelIds(String path) throws Exception {
        return getHotelIds(path, null);
    }

    private Set<Integer> getHotelIds(String path, Set<Integer> filter) throws Exception {
        Set<Integer> hotelIds = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    Integer hotelId = Integer.parseInt(line.trim().split(",")[0]);
                    if (CollectionUtils.isEmpty(filter) || !filter.contains(hotelId)) {
                        hotelIds.add(hotelId);
                    }
                } catch (Exception e) {

                }
            }
        }
        return hotelIds;
    }

    private List<JSONArray> getAllLdsResults(Collection<Integer> hotelIds) throws Exception {
        CloseableHttpClient httpClient = createHttpClient();
        List<List<Integer>> partitions = Lists.partition(new ArrayList<>(hotelIds), 100);
        List<JSONArray> allLdsResults = new ArrayList<>();
        RateLimiter rateLimiter = RateLimiter.create(10);
        for (List<Integer> partition : partitions) {
            rateLimiter.acquire();
            JSONArray ldsResults = getLdsResults(httpClient, partition);
            if (ldsResults != null) {
                allLdsResults.add(ldsResults);
            }
        }
        return allLdsResults;
    }

    private JSONArray getLdsResults(CloseableHttpClient httpClient, List<Integer> hotelIds) throws Exception {
        String baseUrl = "https://localhost:44572/properties/shells?propertyIds=";
        String ldsUrl = baseUrl + Joiner.on(",").join(hotelIds);
        HttpGet httpGet = new HttpGet(ldsUrl);
        httpGet.addHeader("User", "EWSLPP");
        httpGet.addHeader("Accept-Encoding", "gzip");
        httpGet.addHeader("request-id", UUID.randomUUID().toString());
        try (CloseableHttpResponse response = httpClient.execute(httpGet);
             BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String inputLine;
                StringBuilder responseData = new StringBuilder();
                while ((inputLine = br.readLine()) != null) {
                    responseData.append(inputLine);
                }
                return new JSONArray(responseData.toString());
            } else {
                System.out.println("Error: " + statusCode);
            }
        }
        return null;
    }

    private String getTspIds(JSONObject object) throws Exception {
        List<Integer> tspIds = new ArrayList<>();
        JSONArray providers = object.optJSONArray("providers");
        if (providers != null && providers.length() > 0) {
            for (int i = 0; i < providers.length(); i++) {
                JSONObject provider = providers.getJSONObject(i);
                if (provider != null) {
                    Integer id = provider.getInt("id");
                    tspIds.add(id);
                }
            }
        }
        return CollectionUtils.isNotEmpty(tspIds) ? StringUtils.join(tspIds, ";") : "Unknown";
    }

    private CloseableHttpClient createHttpClient() throws Exception {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();

        return HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
    }

}
