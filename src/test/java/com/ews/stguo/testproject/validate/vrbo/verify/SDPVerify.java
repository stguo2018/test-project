package com.ews.stguo.testproject.validate.vrbo.verify;

import com.ews.stguo.testproject.utils.client.DatabaseClient;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public abstract class SDPVerify {

    protected abstract void dataStorage(Connection conn, JSONObject data, String ecomId, int fileIndex) throws Exception;

    protected void verify(String type, String controlFilePath) throws Exception {
        Set<String> controlFileIds = ControlFileRWUtils
                .loadHotelIdStrByPaths(controlFilePath)
                .stream().map(s -> s.split(",")[0]).collect(Collectors.toSet());
        System.out.println("ControlFileSize:" + controlFileIds.size());

        DataSource dataSource = DatabaseClient.getDataSource("jdbc:mysql://localhost:3306/sdp?useUnicode=true&characterEncoding=utf8");

        Set<String> sdpIds = new HashSet<>();
        Set<String> missIds = new HashSet<>();
        Set<String> contents = new HashSet<>();
        Random random = new Random();
        int count = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 1; i <= 3; i++) {
            int fileIndex = i;
            try (BufferedReader br = RWFileUtils.getReader(String.format("expedia-lodging-%d-all.jsonl", i))) {
                String line;
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    JSONObject o = new JSONObject(line);
                    JSONObject propertyId = o.optJSONObject("propertyId");
                    int randomValue = random.nextInt(100);
                    if (randomValue >= 99) {
                        contents.add(line);
                    }
                    if (propertyId != null) {
                        String ecomId = propertyId.optString("expedia");
                        sdpIds.add(ecomId);
                        if (!controlFileIds.contains(ecomId)) {
                            missIds.add(ecomId);
                        }
                        String l = line;
                        futures.add(CompletableFuture.runAsync(() -> {
//                            try {
//                                Connection conn = hikariDataSource.getConnection();
                            try (Connection conn = dataSource.getConnection()) {
//                            try {
                                dataStorage(conn/*null*/, o, ecomId, fileIndex);
                            } catch (Exception e) {
                                System.out.println(l);
                                throw new RuntimeException(e);
                            }
                        }, executorService));
                        if (++count % 10000 == 0) {
                            try {
                                CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get();
                                futures = new ArrayList<>();
                            } catch (Exception e) {
                                throw e;
                            }
                            System.out.println(count + "/" + controlFileIds.size());
                        }
                    }
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).get();
            }
        }
        List<String> lossIds = ListUtils.removeAll(controlFileIds, sdpIds);
        System.out.println("SDPIds:" + sdpIds.size());
        System.out.println("MissIds:" + missIds.size());
        System.out.println("LostIds:" + lossIds.size());
        System.out.println("Contents:" + contents.size());
        try (BufferedWriter bw = RWFileUtils.getWriter("SDP/" + type + "-MissIds.csv")) {
            for (String sdpId : missIds) {
                bw.write(sdpId);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("SDP/" + type + "-LostIds.csv")) {
            for (String sdpId : lossIds) {
                bw.write(sdpId);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("SDP/" + type + "-HotelIdsInSDP.csv")) {
            for (String sdpId : sdpIds) {
                bw.write(sdpId);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("SDP/" + type + "-RandomContents.jsonl")) {
            for (String sdpId : contents) {
                bw.write(sdpId);
                bw.newLine();
            }
            bw.flush();
        }
    }

}
