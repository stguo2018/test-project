package com.ews.stguo.testproject.aws.qubole;

import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.google.common.collect.Lists;
import com.qubole.qds.sdk.java.client.QdsClient;
import com.qubole.qds.sdk.java.client.ResultLatch;
import com.qubole.qds.sdk.java.entities.ResultValue;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class QuboleTest {

    @Test
    public void test01() throws Exception {
        String querySentence = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                this.getClass().getClassLoader()
                        .getResourceAsStream("qubole/HiveQuery_ReviewContent2.sql")))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            querySentence = sb.toString();
        }
        List<String> hotelIds = ControlFileRWUtils.loadHotelIdStrByPaths("sddp-feed-control-file (4).csv").stream()
                .map(l -> l.split(",")[0]).collect(Collectors.toList());
        String apiToken = "cca3350de3874a44870e9a50e932716b5ae0ede397e14694b262c95debc684b6";
        String apiEndpoint = "https://api.qubole.com/api";
        String apiVersion = "v1.2";
        QdsClient qdsClient = QuboleClientFactory.createQdsClient(apiEndpoint, apiToken, apiVersion);

        List<List<String>> ps = Lists.partition(hotelIds, Math.min(hotelIds.size(), 50000));
        AtomicInteger i = new AtomicInteger(0);
        int psc = ps.size();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<String> p : ps) {
            String hotelIdsStr = p.stream()
                    .map(hotelId -> "'" + hotelId + "'")
                    .collect(Collectors.joining(","));
            String s = String.format(querySentence, hotelIdsStr, "en%");
            futures.add(CompletableFuture.runAsync(() -> {
                int id = -1;
                try {
                    id = qdsClient.command()
                            .hive()
                            .query(s)
                            .invoke()
                            .get().
                            getId();
                    ResultLatch rl = new ResultLatch(qdsClient, id);
                    if (!rl.await(60L, TimeUnit.MINUTES)) {
                        throw new RuntimeException("time out");
                    }
                    ResultValue resultValue = qdsClient.command().results(String.valueOf(id)).inline(true).invoke().get();
                    String results = resultValue.getResults();
                    System.out.print("(" + i.incrementAndGet() + "/" + psc + ")\n" + results + "|");
                } catch (Exception e) {
                    System.out.println("Error: " + id);
                    throw new RuntimeException(e);
                }
            }, executorService));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get();
    }

    @Test
    public void test02() throws Exception {
        String querySentence = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                this.getClass().getClassLoader()
                        .getResourceAsStream("qubole/Biddable_Presto_Query_hcom.sql")))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            querySentence = sb.toString();
        }
        List<String> hotelIds = ControlFileRWUtils.loadHotelIdStrByPaths("RemovedIds.csv").stream()
                .map(l -> l.split(",")[0]).collect(Collectors.toList());
        String apiToken = "cca3350de3874a44870e9a50e932716b5ae0ede397e14694b262c95debc684b6";
        String apiEndpoint = "https://api.qubole.com/api";
        String apiVersion = "v1.2";
        QdsClient qdsClient = QuboleClientFactory.createQdsClient(apiEndpoint, apiToken, apiVersion);

        List<List<String>> ps = Lists.partition(hotelIds, Math.min(hotelIds.size(), 21000));
        AtomicInteger i = new AtomicInteger(0);
        int psc = ps.size();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try (BufferedWriter bw = RWFileUtils.getWriter("QueryData.csv")) {
            for (List<String> p : ps) {
                String hotelIdsStr = String.join(",", p);
                String s = String.format(querySentence, hotelIdsStr);
                futures.add(CompletableFuture.runAsync(() -> {
                    int id = -1;
                    try {
                        id = qdsClient.command()
                                .presto()
                                .clusterLabel("waggledance-presto")
                                .query(s)
                                .invoke()
                                .get().
                                getId();
                        ResultLatch rl = new ResultLatch(qdsClient, id);
                        if (!rl.await(60L, TimeUnit.MINUTES)) {
                            throw new RuntimeException("time out");
                        }
                        ResultValue resultValue = qdsClient.command().results(String.valueOf(id)).inline(true).invoke().get();
                        String results = resultValue.getResults();
                        System.out.print("(" + i.incrementAndGet() + "/" + psc + ")\n");
                        synchronized (bw) {
                            bw.write(results);
                            bw.flush();
                        }
                    } catch (Exception e) {
                        System.out.println("Error: " + id);
                        throw new RuntimeException(e);
                    }
                }, executorService));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get();
        }
    }

    @Test
    public void testQueryReviewByPresto() throws Exception {
        String querySentence;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                this.getClass().getClassLoader()
                        .getResourceAsStream("qubole/HiveQuery_ReviewContent3.sql")))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            querySentence = sb.toString();
        }
        List<String> hotelIds = ControlFileRWUtils.loadHotelIdStrByPaths("sddp-feed-control-file (1).csv").stream()
                .map(l -> l.split(",")[0]).collect(Collectors.toList());
        String apiToken = "cca3350de3874a44870e9a50e932716b5ae0ede397e14694b262c95debc684b6";
        String apiEndpoint = "https://api.qubole.com/api";
        String apiVersion = "v1.2";
        QdsClient qdsClient = QuboleClientFactory.createQdsClient(apiEndpoint, apiToken, apiVersion);

        List<List<String>> ps = Lists.partition(hotelIds, Math.min(hotelIds.size(), 50000));
        try (BufferedWriter bw = RWFileUtils.getWriter("QueryData.csv")) {
            String hotelIdsStr = "'" + String.join("','", ps.get(0)) + "'";
            String s = String.format(querySentence, hotelIdsStr, "en%", "Expedia");
            try {
                int id = qdsClient.command()
                        .presto()
                        .clusterLabel("waggledance-presto")
                        .query(s)
                        .invoke()
                        .get().
                        getId();
                ResultLatch rl = new ResultLatch(qdsClient, id);
                if (!rl.await(60L, TimeUnit.MINUTES)) {
                    throw new RuntimeException("time out");
                }
                ResultValue resultValue = qdsClient.command().results(String.valueOf(id)).inline(true).invoke().get();
                String results = resultValue.getResults();
                bw.write(results);
                bw.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
