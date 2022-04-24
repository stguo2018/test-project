package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MISSHOTEL;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.TOTALHOTEL;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public abstract class VrboValidator<T> {

    private static final String BASE_PATH = "E:/";
    private static final ObjectMapper OM = new ObjectMapper();
    private final Map<String, Object> LOCKS = new HashMap<>();
    private final Map<String, Integer> COUNTER = new HashMap<>();

    protected Set<Integer> hotelIds;
    private long totalSize;
    private long analyzedSize;

    public VrboValidator(Set<Integer> hotelIds) {
        this.hotelIds = new HashSet<>(hotelIds);
    }

    public void start() {
        System.out.println(getFileType() + " is Starting.");
        // Load control file.
        try {
            if (hotelIds == null) {
                hotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(true, "E:/ews-29840/sddp-feed-control-file.csv");
            }
            analyzedSize = 0;
            COUNTER.put(TOTALHOTEL, hotelIds.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this::printProcess, 5, 5, TimeUnit.SECONDS);
        String fileNameFormat = getFileNameFormat();
        ExecutorService executors = Executors.newFixedThreadPool(getFileSize());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(getFileSize());
        for (int i = 1; i <= getFileSize(); i++) {
            String fileName = String.format(fileNameFormat, i);
            int index = i;
            futures.add(CompletableFuture.runAsync(() -> readAndAnalyse(index, fileName, countDownLatch), executors));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            executors.shutdown();
            scheduledExecutorService.shutdown();
            this.analyzedSize = this.totalSize;
            printProcess();
            COUNTER.put(MISSHOTEL, hotelIds.size());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(getFileType() + " is Done.");
        System.out.println("---------------------------------");
    }

    protected void readAndAnalyse(int index, String fileName, CountDownLatch countDownLatch) {
        try (BufferedReader br = RWFileUtils.getReader(BASE_PATH, fileName);
             FileInputStream fis = new FileInputStream(BASE_PATH + fileName)) {
            sumTotalSize(fis.available());
            countDownLatch.countDown();
            countDownLatch.await();
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                lineCount++;
                increaseAnalyzedSize(line.getBytes().length + 1);
                try {
                    T t = fromJson(line);
                    if (t != null) {
                        analyse(index, t);
                    }
                } catch (Exception e) {
                    printErrLog(index, lineCount, line, e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private T fromJson(String jsonStr) throws Exception {
        return OM.readValue(jsonStr.trim(), getClazz());
    }

    private synchronized Object getLock(String key) {
        return LOCKS.computeIfAbsent(key, k -> new Object());
    }

    private void printErrLog(int index, int lineCount, String line, Throwable th) {
        try (BufferedWriter bw = RWFileUtils.getWriter(BASE_PATH, getErrFilePath(), true)) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            th.printStackTrace(pw);
            String content = String.format("index: %d, lineCount: %d, line: %s\n%s", index, lineCount, line, sw.toString());
            bw.write(content);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void sumTotalSize(int size) {
        this.totalSize += size;
    }

    private synchronized void increaseAnalyzedSize(int size) {
        this.analyzedSize += size;
    }

    private void printProcess() {
        double rate = (double) this.analyzedSize / this.totalSize * 100;
        System.out.println(String.format("%s-%.2f%%", getFileType(), rate));
    }

    protected String getFileNameFormat() {
        return "ews-29840/" + getFileType() + "/expedia-lodging-%d-all.jsonl";
    }

    protected String getOutputFilePath() {
        return "ews-29840/" + getFileType() + ".output";
    }

    protected String getErrFilePath() {
        return "ews-29840/" + getFileType() + ".err";
    }

    protected int getFileSize() {
        return 6;
    }

    protected void updateCounter(String key) {
        synchronized (getLock(key)) {
            COUNTER.put(key, COUNTER.getOrDefault(key, 0) + 1);
        }
    }

    public abstract String getFileType();

    public abstract Class<T> getClazz();

    public abstract void analyse(int index, T model);

}
