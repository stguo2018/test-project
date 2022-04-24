package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.compress.CompressUtils;
import com.ews.stguo.testproject.validate.vrbo.model.listings.ListingsModel;
import com.ews.stguo.testproject.validate.vrbo.model.summary.SummaryModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.TOTALHOTEL;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public abstract class VrboComparator<T> {

    private static final String BASE_PATH = "E:/";
    private static final ObjectMapper OM = new ObjectMapper();
    private final Map<String, Object> LOCKS = new HashMap<>();
    private final Map<String, Integer> COUNTER = new HashMap<>();
    private final JedisPool jedisPool;
    private final int total;
    private final Map<String, Map<String, List<T>>> noMatchData = new HashMap<>();

    protected Map<String, T> fileDates = new ConcurrentHashMap<>();
    protected Set<Integer> hotelIds;
    protected Map<String, Pair<String, String>> vrboWebLikeMapping;
    protected Map<String, String> instantBookMapping;
    private int current;

    private long totalSize;
    private long analyzedSize;

    private Map<String, String> tspIdMap = new HashMap<>();
    private Set<String> hasVrboPropertyIds = new ConcurrentHashSet<>();

    public VrboComparator(Set<Integer> hotelIds) {
        this.hotelIds = new HashSet<>(hotelIds);
        this.total = hotelIds.size();
        COUNTER.put(TOTALHOTEL, hotelIds.size());

        if (getClazz() == ListingsModel.class) {
            try (BufferedReader br = RWFileUtils.getReader("", "E:\\ews-29840\\sddp-feed-control-file-4.csv")) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns[2] == null) {
                        System.out.println(line);
                    }
                    tspIdMap.put(columns[0], columns[2]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxWaitMillis(10000);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        jedisPool = new JedisPool(poolConfig, "localhost", 6379, 10000);
    }

    public VrboComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping, Map<String, String> instantBookMapping) {
        this(hotelIds);
        this.vrboWebLikeMapping = vrboWebLikeMapping;
        this.instantBookMapping = instantBookMapping;
    }

    public VrboComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping) {
        this(hotelIds);
        this.vrboWebLikeMapping = vrboWebLikeMapping;
    }

    public void compare() {
        readDateFromSDP();
        compareData();
        recordResult();
        recordNoMatchAsExcel();
        System.out.println(getFileType() + " is Done.");
        System.out.println("---------------------------------");
    }

    private void recordResult() {
        try (BufferedWriter bw = RWFileUtils.getWriter(BASE_PATH, getOutputFilePath())) {
            COUNTER.forEach((k, v) -> {
                try {
                    bw.write(k + ": " + v);
                    bw.newLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compareData() {
        // Compare.
        System.out.println(getFileType() + "-Compare starting.");
        if (MapUtils.isEmpty(fileDates)) {
            System.out.println(getFileType() + "-Compare done.");
            return;
        }
        ScheduledExecutorService compareScheduled = Executors.newScheduledThreadPool(1);
        compareScheduled.scheduleAtFixedRate(this::printProcess2, 5, 5, TimeUnit.SECONDS);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<List<Integer>> partition = Lists.partition(new ArrayList<>(hotelIds), Math.min(20000, hotelIds.size()));
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (List<Integer> p : partition) {
            futureList.add(CompletableFuture.runAsync(() -> {
                for (Integer hotelId : p) {
                    increaseCurrent();
                    String dateStr = getDataFromCache(hotelId);
                    try {
                        JSONObject object = new JSONObject(dateStr);
                        int statusCode = Optional.of(object).map(er -> er.optInt("statusCode")).orElse(500);
                        boolean flag = true;
                        if (statusCode != 200) {
                            updateCounter("ednMiss");
                            flag = false;
                        }
                        if (!fileDates.containsKey(String.valueOf(hotelId))) {
                            updateCounter("sdpMiss");
                            flag = false;
                        }
                        if (!flag) {
                            continue;
                        }
                        Optional<JSONObject> responseEntity = Optional.of(object).map(er -> er.optJSONObject("responseEntity"));
                        compareData(fileDates.get(String.valueOf(hotelId)), responseEntity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, executorService));
        }
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        compareScheduled.shutdown();
        this.current = this.total;
        printProcess2();
        System.out.println(getFileType() + "-Compare done.");
    }

    private void readDateFromSDP() {
        int fileSize = getClazz() == ListingsModel.class ? 1 : getFileSize();
        ExecutorService executors = Executors.newFixedThreadPool(fileSize);
        ScheduledExecutorService readScheduled = Executors.newScheduledThreadPool(1);
        readScheduled.scheduleAtFixedRate(this::printProcess, 5, 5, TimeUnit.SECONDS);

        String fileNameFormat = getFileNameFormat();
        CountDownLatch countDownLatch = new CountDownLatch(fileSize);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Read data from the file.
        System.out.println(getFileType() + "-Read data from the file.");
        for (int i = 1; i <= getFileSize(); i++) {
            String fileName = String.format(fileNameFormat, i);
            futures.add(CompletableFuture.runAsync(() -> read(fileDates, fileName, countDownLatch), executors));
        }

        // Waiting for result.
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            executors.shutdown();
            readScheduled.shutdown();
            this.analyzedSize = this.totalSize;
            printProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(getFileType() + "-Read data from the file is done.");
        try (BufferedWriter bw = RWFileUtils.getWriter("Able to get VrboPropertyId from the LCS.csv")) {
            bw.write("ecomId");
            bw.newLine();
            for (String hasVrboPropertyId : hasVrboPropertyIds) {
                bw.write(hasVrboPropertyId);
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void read(Map<String, T> fileDatas, String fileName, CountDownLatch countDownLatch) {
        try (BufferedReader br = RWFileUtils.getReader(BASE_PATH, fileName);
             FileInputStream fis = new FileInputStream(BASE_PATH + fileName)) {
            sumTotalSize(fis.available());
            countDownLatch.countDown();
            countDownLatch.await();
            String line;
            try (BufferedWriter bw = RWFileUtils.getWriter("", "E:\\ews-29840\\Listings\\InventorySource-Vrbo.csv", true)) {
                while ((line = br.readLine()) != null) {
                    increaseAnalyzedSize(line.getBytes().length + 1);
                    try {
                        T t = fromJson(line);
                        if (t != null) {
                            String id = getId(t);
                            if (t instanceof SummaryModel && StringUtils.isNotBlank(((SummaryModel) t).getPropertyId().getVrbo())) {
                                hasVrboPropertyIds.add(id);
                            }
                            if (hotelIds.contains(Integer.parseInt(id))) {
                                fileDatas.put(id, t);
                            }
                            if (getClazz() == ListingsModel.class) {
                                ListingsModel m = (ListingsModel) t;
                                String tspIds = tspIdMap.get(id);
                                if (/*StringUtils.equalsIgnoreCase(m.getInventorySource(), "expedia") &&*/ tspIds  != null &&
                                        (tspIds.contains("83") || tspIds.contains("103"))
                                        && vrboWebLikeMapping.get(id) != null) {
                                    bw.write(String.format("%s,%s,%s", id,
                                            Optional.ofNullable(vrboWebLikeMapping.get(id)).map(Pair::getRight).orElse("null"),
                                            tspIds));
                                    bw.newLine();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDataFromCache(Integer hotelId) {
        byte[] byteKey = String.valueOf(hotelId).getBytes(StandardCharsets.UTF_8);
        byte[] byteValue = getByteValue(byteKey);
        if (byteValue != null && byteValue.length > 0) {
            try {
                byteValue = CompressUtils.decompress(byteValue);
                return new String(byteValue, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private byte[] getByteValue(byte[] byteKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(byteKey);
        }
    }

    private synchronized void sumTotalSize(int size) {
        this.totalSize += size;
    }

    private synchronized void increaseAnalyzedSize(int size) {
        this.analyzedSize += size;
    }

    private synchronized void increaseCurrent() {
        this.current++;
    }

    private void printProcess() {
        double rate = (double) this.analyzedSize / this.totalSize * 100;
        System.out.printf("%s-%.2f%%-%d%n", getFileType(), rate, fileDates.size());
    }

    private void printProcess2() {
        double rate = (double) this.current / this.total * 100;
        System.out.printf("%s-%.2f%%(%d/%d)%n", getFileType(), rate, current, total);
    }

    private T fromJson(String jsonStr) throws Exception {
        return OM.readValue(jsonStr.trim(), getClazz());
    }

    private synchronized Object getLock(String key) {
        return LOCKS.computeIfAbsent(key, k -> new Object());
    }

    protected String getFileNameFormat() {
        return "ews-29840/" + getFileType() + "/expedia-lodging-%d-all.jsonl";
    }

    protected String getOutputFilePath() {
        return "ews-29840/" + getFileType() + ".compare.output";
    }


    protected int getFileSize() {
        return 6;
    }

    protected void updateCounter(String key) {
        synchronized (getLock(key)) {
            COUNTER.put(key, COUNTER.getOrDefault(key, 0) + 1);
        }
    }

    protected synchronized void updateNoMatchData(String type, T t, String dataInEDN) {
        Map<String, List<T>> map = noMatchData.computeIfAbsent(type, k -> new TreeMap<>());
        List<T> dataInSDPs = map.computeIfAbsent(dataInEDN, k -> new ArrayList<>());
        dataInSDPs.add(t);
    }

    protected synchronized void updateNoMatchData(T t, String dataInEDN) {
        updateNoMatchData(null, t, dataInEDN);
    }

    protected Map<String, List<T>> getNoMatchData(String type) {
        return noMatchData.getOrDefault(type, new TreeMap<>());
    }

    protected Map<String, List<T>> getNoMatchData() {
        return getNoMatchData(null);
    }

    public abstract String getFileType();

    public abstract Class<T> getClazz();

    public abstract String getId(T data);

    public abstract void compareData(T model, Optional<JSONObject> response);

    public abstract void recordNoMatchAsExcel();

}
