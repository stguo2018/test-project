package com.ews.stguo.testproject.validate.vrbo.localization;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.file.ZipUtil;
import com.ews.stguo.testproject.validate.vrbo.model.descriptions.DescriptionsModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LocalizedValidateTest {

    @Test
    public void test_localization() throws Exception {
//        String[] locales = {
//            "ar_sa", "cs_cz", "da_dk", "de_de", "el_gr", "en_gb", "en_us", "es_es", "es_mx", "es_us",
//                "et_ee", "fi_fi", "fr_ca", "he_il", "hr_hr", "hu_hu", "id_id", "is_is", "it_it",
//                "ja_jp", "ko_kr", "lv_lv", "ms_my", "nb_no", "pl_pl"
//        };
//        String[] locales = {
//                "pt_br", "pt_pt", "ru_ru", "sk_sk", "sv_se", "th_th",
//                "tl_ph", "tr_tr", "uk_ua", "vi_vn", "zh_cn"
//        };
        String[] locales = {
                "fr_fr", "lt_lt", "nl_nl"
        };
        String basePath = "D:\\data\\";
        ObjectMapper om = new ObjectMapper();
        Map<String, List<String>> results = new HashMap<>();
//        try (BufferedReader reader = RWFileUtils.getReader("result2.csv")) {
//            reader.readLine();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] columns = line.split(",");
//                List<String> localList = new ArrayList<>();
//                for (int i = 1; i < columns.length; i++) {
//                    localList.add(columns[i]);
//                }
//                results.put(columns[0], localList);
//            }
//        }
        Map<String, Integer> localeCount = new HashMap<>();
        for (String locale : locales) {
            int count = 0;
            for (int i = 1; i <= 6; i++) {
                String path = basePath + locale + "\\" + String.format("expedia-lodging-%d-all.jsonl", i);
                try (BufferedReader reader = RWFileUtils.getReader("", path)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        DescriptionsModel model = om.readValue(line, DescriptionsModel.class);
                        if (model == null) {
                            continue;
                        }
                        String hotelId = model.getPropertyId().getExpedia();
                        List<String> localeList = results.computeIfAbsent(hotelId, k -> new ArrayList<>());
                        localeList.add(locale);
                        count++;
                    }
                }
                System.out.println("Done: " + path);
            }
            localeCount.put(locale, count);
        }
        try (BufferedWriter writer = RWFileUtils.getWriter("result.csv")) {
            writer.write("EcomId,Locales");
            writer.newLine();
            for (Map.Entry<String, List<String>> entry : results.entrySet()) {
                writer.write(String.format("%s,%s", entry.getKey(), String.join(";", entry.getValue())));
                writer.newLine();
            }
            writer.flush();
        }
        int baseCount = 5906327;
        try (BufferedWriter writer = RWFileUtils.getWriter("localeCount.csv")) {
            writer.write("Locale,Number,Percentage");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : localeCount.entrySet()) {
                BigDecimal percentage = new BigDecimal(String.valueOf(entry.getValue() * 100))
                        .divide(new BigDecimal(String.valueOf(baseCount)), 3, RoundingMode.HALF_EVEN);
                writer.write(String.format("%s,%d,%s", entry.getKey(), entry.getValue(), percentage + "%"));
                writer.newLine();
            }
            writer.flush();
        }
    }

    @Test
    public void test_localization_combined() throws Exception {
        Map<String, List<String>> results = new HashMap<>();
        try (BufferedReader reader = RWFileUtils.getReader("result.csv")) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                List<String> localList = new ArrayList<>();
                String[] columns2 = columns[1].split(";");
                for (int i = 0; i < columns2.length; i++) {
                    localList.add(columns2[i]);
                }
                results.put(columns[0], localList);
            }
        }

        try (BufferedReader reader = RWFileUtils.getReader("result3.csv")) {
            try (BufferedWriter writer = RWFileUtils.getWriter("result4.csv")) {
                writer.write("EcomId,Locales");
                writer.newLine();
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    List<String> localList = new ArrayList<>(Arrays.asList(columns).subList(1, columns.length));
                    if (results.containsKey(columns[0])) {
                        localList.addAll(results.get(columns[0]));
                        results.remove(columns[0]);
                    }
                    writer.write(String.format("%s,%s", columns[0], String.join(";", localList)));
                    writer.newLine();
                    writer.flush();

                }
                if (MapUtils.isNotEmpty(results)) {
                    for (Map.Entry<String, List<String>> entry : results.entrySet()) {
                        writer.write(String.format("%s,%s", entry.getKey(), String.join(";", entry.getValue())));
                        writer.newLine();
                    }
                    writer.flush();
                }
            }

        }

    }

    @Test
    public void test_localization_only_vrbo() throws Exception {
        Set<String> vrProperties = new HashSet<>(ControlFileRWUtils.loadHotelIdStrByPaths("vr.csv"));
        Map<String, Integer> localeCount = new HashMap<>();
        try (BufferedReader br = RWFileUtils.getReader("SDP-Description-locale-Details.csv")) {
            try (BufferedWriter bw = RWFileUtils.getWriter("SDP-Description-locale-Details(Only VR).csv")) {
                bw.write("EcomId,Locale");
                bw.newLine();
                bw.flush();
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (vrProperties.contains(columns[0])) {
                        bw.write(line);
                        bw.newLine();
                        String[] locales = columns[1].split(";");
                        for (String locale : locales) {
                            localeCount.put(locale, localeCount.getOrDefault(locale, 0) + 1);
                        }
                    }
                }
                bw.flush();
            }
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("SDP-Description-locale-Per(Only VR).csv")) {
            bw.write("Locale,Number,Percentage");
            bw.newLine();
            bw.flush();
            BigDecimal totalSize = new BigDecimal(String.valueOf(vrProperties.size()));
            BigDecimal hundred = new BigDecimal("100");
            for (Map.Entry<String, Integer> entry : localeCount.entrySet()) {
                BigDecimal pec = new BigDecimal(String.valueOf(entry.getValue()))
                        .multiply(hundred).divide(totalSize, 2, RoundingMode.HALF_UP);
                bw.write(String.format("%s,%d,%s", entry.getKey(), entry.getValue(), pec + "%"));
                bw.newLine();
            }
            bw.flush();
        }
    }

    @Test
    public void test_localization_report() throws Exception {
        String[] locales = {
                "en-US,en_us", "ar-SA,ar_sa", "cs-CZ,cs_cz", "da-DK,da_dk", "de-DE,de_de", "el-GR,el_gr", "en-GB,en_gb",
                "es-ES,es_es", "es-MX,es_mx", "es-US,es_us", "et-EE,et_ee", "fi-FI,fi_fi", "fr-CA,fr_ca", "he-IL,he_il",
                "hr-HR,hr_hr", "hu-HU,hu_hu", "id-ID,id_id", "is-IS,is_is", "it-IT,it_it", "ja-JP,ja_jp", "ko-KR,ko_kr",
                "lv-LV,lv_lv", "ms-MY,ms_my", "nb-NO,nb_no", "pl-PL,pl_pl", "pt-BR,pt_br", "pt-PT,pt_pt", "ru-RU,ru_ru",
                "sk-SK,sk_sk", "sv-SE,sv_se", "th-TH,th_th", "tl-PH,tl_ph", "tr-TR,tr_tr", "uk-UA,uk_ua", "vi-VN,vi_vn",
                "zh-CN,zh_cn", "fr-FR,fr_fr", "lt-LT,lt_lt", "nl-NL,nl_nl"
        };
//        String[] locales2 = {
//                "en-US,en_us", "es-ES,es_es"
//        };

//        amenities(locales);
//        images(locales);
        policies(locales);
    }

    private void amenities(String[] locales) throws Exception {
        Map<String, Function<JSONObject, String>> fields = new HashMap<>();
        fields.put("propertyAmenities", o -> {
            JSONObject propertyAmenities = o.optJSONObject("propertyAmenities");
            if (propertyAmenities != null) {
                return propertyAmenities.toString();
            }
            return null;
        });
        fields.put("roomAmenities", o -> {
            JSONObject roomAmenities = o.optJSONObject("roomAmenities");
            if (roomAmenities != null) {
                return roomAmenities.toString();
            }
            return null;
        });
        fields.put("cleanlinessAndSafety", o -> {
            JSONObject cleanlinessAndSafety = o.optJSONObject("cleanlinessAndSafety");
            if (cleanlinessAndSafety != null) {
                return cleanlinessAndSafety.toString();
            }
            return null;
        });
        readAndRecord(locales, "amenities", fields, 3);
    }

    private void images(String[] locales) throws Exception {
        Map<String, Function<JSONObject, String>> fields = new HashMap<>();
        fields.put("heroImageTitle", o -> {
            JSONObject propertyAmenities = o.optJSONObject("hero");
            if (propertyAmenities != null) {
                return propertyAmenities.toString();
            }
            return null;
        });
        fields.put("generalImageTitle", o -> {
            JSONObject roomAmenities = o.optJSONObject("images");
            if (roomAmenities != null) {
                return roomAmenities.toString();
            }
            return null;
        });
        readAndRecord(locales, "images", fields, 3);
    }

    private void policies(String[] locales) throws Exception {
        Map<String, Function<JSONObject, String>> fields = new HashMap<>();
        fields.put("checkInPolicy", o -> {
            JSONArray checkInPolicy = o.optJSONArray("checkInPolicy");
            if (checkInPolicy != null && checkInPolicy.length() > 0) {
                return checkInPolicy.toString();
            }
            return null;
        });
        fields.put("checkOutPolicy", o -> {
            JSONArray checkOutPolicy = o.optJSONArray("checkOutPolicy");
            if (checkOutPolicy != null && checkOutPolicy.length() > 0) {
                return checkOutPolicy.toString();
            }
            return null;
        });
        fields.put("petPolicy", o -> {
            JSONArray petPolicy = o.optJSONArray("petPolicy");
            if (petPolicy != null && petPolicy.length() > 0) {
                return petPolicy.toString();
            }
            return null;
        });
        fields.put("childrenAndExtraBedPolicy", o -> {
            JSONArray childrenAndExtraBedPolicy = o.optJSONArray("childrenAndExtraBedPolicy");
            if (childrenAndExtraBedPolicy != null && childrenAndExtraBedPolicy.length() > 0) {
                return childrenAndExtraBedPolicy.toString();
            }
            return null;
        });
        fields.put("checkInInstructions", o -> {
            JSONArray checkInInstructions = o.optJSONArray("checkInInstructions");
            if (checkInInstructions != null && checkInInstructions.length() > 0) {
                return checkInInstructions.toString();
            }
            return null;
        });
        fields.put("specialInstructions", o -> {
            JSONArray specialInstructions = o.optJSONArray("specialInstructions");
            if (specialInstructions != null && specialInstructions.length() > 0) {
                return specialInstructions.toString();
            }
            return null;
        });
        fields.put("knowBeforeYouGo", o -> {
            JSONArray knowBeforeYouGo = o.optJSONArray("knowBeforeYouGo");
            if (knowBeforeYouGo != null && knowBeforeYouGo.length() > 0) {
                return knowBeforeYouGo.toString();
            }
            return null;
        });
        fields.put("propertyFees", o -> {
            JSONObject paymentPolicy = o.optJSONObject("paymentPolicy");
            if (paymentPolicy != null) {
                JSONArray propertyFees = paymentPolicy.optJSONArray("propertyFees");
                if (propertyFees != null && propertyFees.length() > 0) {
                    return propertyFees.toString();
                }
            }
            return null;
        });
        fields.put("optionalExtras", o -> {
            JSONObject paymentPolicy = o.optJSONObject("paymentPolicy");
            if (paymentPolicy != null) {
                JSONArray optionalExtras = paymentPolicy.optJSONArray("optionalExtras");
                if (optionalExtras != null && optionalExtras.length() > 0) {
                    return optionalExtras.toString();
                }
            }
            return null;
        });
        readAndRecord(locales, "policies", fields, 3);
    }

    private void policies2(String[] locales) throws Exception {
        Map<String, Function<JSONObject, String>> fields = new HashMap<>();
        fields.put("checkInPolicy", o -> {
            JSONArray checkInPolicy = o.optJSONArray("checkInPolicy");
            if (checkInPolicy != null && checkInPolicy.length() > 0) {
                return checkInPolicy.toString();
            }
            return null;
        });
        fields.put("checkOutPolicy", o -> {
            JSONArray checkOutPolicy = o.optJSONArray("checkOutPolicy");
            if (checkOutPolicy != null && checkOutPolicy.length() > 0) {
                return checkOutPolicy.toString();
            }
            return null;
        });
        fields.put("petPolicy", o -> {
            JSONArray petPolicy = o.optJSONArray("petPolicy");
            if (petPolicy != null && petPolicy.length() > 0) {
                return petPolicy.toString();
            }
            return null;
        });
        fields.put("childrenAndExtraBedPolicy", o -> {
            JSONArray childrenAndExtraBedPolicy = o.optJSONArray("childrenAndExtraBedPolicy");
            if (childrenAndExtraBedPolicy != null && childrenAndExtraBedPolicy.length() > 0) {
                return childrenAndExtraBedPolicy.toString();
            }
            return null;
        });
        fields.put("checkInInstructions", o -> {
            JSONArray checkInInstructions = o.optJSONArray("checkInInstructions");
            if (checkInInstructions != null && checkInInstructions.length() > 0) {
                return checkInInstructions.toString();
            }
            return null;
        });
        fields.put("specialInstructions", o -> {
            JSONArray specialInstructions = o.optJSONArray("specialInstructions");
            if (specialInstructions != null && specialInstructions.length() > 0) {
                return specialInstructions.toString();
            }
            return null;
        });
        fields.put("knowBeforeYouGo", o -> {
            JSONArray knowBeforeYouGo = o.optJSONArray("knowBeforeYouGo");
            if (knowBeforeYouGo != null && knowBeforeYouGo.length() > 0) {
                return knowBeforeYouGo.toString();
            }
            return null;
        });
        fields.put("propertyFees", o -> {
            JSONObject paymentPolicy = o.optJSONObject("paymentPolicy");
            if (paymentPolicy != null) {
                JSONArray propertyFees = paymentPolicy.optJSONArray("propertyFees");
                if (propertyFees != null && propertyFees.length() > 0) {
                    return propertyFees.toString();
                }
            }
            return null;
        });
        fields.put("optionalExtras", o -> {
            JSONObject paymentPolicy = o.optJSONObject("paymentPolicy");
            if (paymentPolicy != null) {
                JSONArray optionalExtras = paymentPolicy.optJSONArray("optionalExtras");
                if (optionalExtras != null && optionalExtras.length() > 0) {
                    return optionalExtras.toString();
                }
            }
            return null;
        });
        readAndRecord(locales, "policies", fields, 3);
    }

    private void readAndRecord(String[] locales, String type, Map<String, Function<JSONObject, String>> fields, int threadSize) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        AtomicInteger c = new AtomicInteger(1);
        Map<String, Map<String, Integer>> localeFieldsCount = new ConcurrentHashMap<>();
        fields.forEach((k, v) -> localeFieldsCount.put(k, new ConcurrentHashMap<>()));
        for (String locale : locales) {
            String l = locale.split(",")[0];
            File directory = getFiles(type, l);
            if (directory == null || directory.listFiles() == null) {
                continue;
            }
            Map<String, Map<String, String>> localeFieldsCount2 = new ConcurrentHashMap<>();
            fields.forEach((k, v) -> localeFieldsCount2.put(k, new ConcurrentHashMap<>()));
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (File file : directory.listFiles()) {
                futures.add(CompletableFuture.runAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    System.out.printf("File %s read is starting...%n", file.getAbsolutePath());
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                        String line;
                        AtomicInteger cc = new AtomicInteger(0);
                        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
                        String name = Thread.currentThread().getName();
                        scheduledExecutorService.scheduleAtFixedRate(() -> {
                            System.out.printf("%s File %s read is reading(%d)...%n", name, file.getAbsolutePath(), cc.get());
                        }, 0, 5, TimeUnit.SECONDS);
                        while ((line = br.readLine()) != null) {
                            //ImagesModel model;
                            JSONObject model;
                            try {
                                model = new JSONObject(line);
                                //model = om.readValue(line, ImagesModel.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }
                            String id = model.optJSONObject("propertyId").getString("expedia");
                            for (Map.Entry<String, Function<JSONObject, String>> entry : fields.entrySet()) {
                                String value = entry.getValue().apply(model);
                                if (value != null) {
                                    localeFieldsCount.get(entry.getKey()).put(l, localeFieldsCount.get(entry.getKey()).getOrDefault(l, 0) + 1);
                                    localeFieldsCount2.get(entry.getKey()).put(id, l);
                                }
                            }
                            cc.incrementAndGet();
                        }
                        scheduledExecutorService.shutdown();
                        System.out.printf("%s File %s read is reading(%d)...%n", Thread.currentThread().getName(), file.getAbsolutePath(), cc.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.printf("Locale(%d/%d) File %s read is done. %d%n", c.get(), locales.length,
                            file.getAbsolutePath(), System.currentTimeMillis() - startTime);
                }, executorService));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            for (Map.Entry<String, Map<String, String>> entry : localeFieldsCount2.entrySet()) {
                try (BufferedWriter bw = RWFileUtils.getWriter("", String.format("E:\\SDP\\SDP-%s_%s-Locale-Detail-%s.csv", type, entry.getKey(), l))) {
                    bw.write("EcomId,Locales");
                    bw.newLine();
                    for (Map.Entry<String, String> e2 : entry.getValue().entrySet()) {
                        bw.write(String.format("%s,%s", e2.getKey(), e2.getValue()));
                        bw.newLine();
                    }
                    bw.flush();
                }
            }
            FileUtils.deleteDirectory(directory);
            c.incrementAndGet();
        }
        for (Map.Entry<String, Map<String, Integer>> entry : localeFieldsCount.entrySet()) {
            BigDecimal oneHundred = new BigDecimal("100");
            BigDecimal total = new BigDecimal(entry.getValue().get("en-US"));
            try (BufferedWriter bw = RWFileUtils.getWriter("", String.format("E:\\SDP\\SDP-%s_%s-Locale-Per.csv", type, entry.getKey()))) {
                bw.write("EcomId,Number,Percentage");
                bw.newLine();
                for (String locale : locales) {
                    String l = locale.split(",")[0];
                    Integer v = entry.getValue().getOrDefault(l, 0);
                    BigDecimal per = new BigDecimal(String.valueOf(v)).multiply(oneHundred)
                            .divide(total, 2, RoundingMode.HALF_UP);
                    bw.write(String.format("%s,%d,%s", l, v, per + "%"));
                    bw.newLine();
                }
                bw.flush();
            }
        }
        for (String field : fields.keySet()) {
            Map<String, String> localesMap = new HashMap<>();
            for (String locale : locales) {
                String l = locale.split(",")[0];
                File file = new File(String.format("E:\\SDP\\SDP-%s_%s-Locale-Detail-%s.csv", type, field, l));
                try (BufferedReader br = RWFileUtils.getReader("", file.getAbsolutePath())) {
                    String line = br.readLine();
                    while ((line = br.readLine()) != null) {
                        String id = line.split(",")[0];
                        if (localesMap.containsKey(id)) {
                            localesMap.put(id, localesMap.get(id) + ";" + l);
                        } else {
                            localesMap.put(id, l);
                        }
                    }
                }
                FileUtils.forceDelete(file);
            }
            try (BufferedWriter bw = RWFileUtils.getWriter("", String.format("E:\\SDP\\SDP-%s_%s-Locale-Detail.csv", type, field))) {
                bw.write("EcomId,Locales");
                bw.newLine();
                for (Map.Entry<String, String> entry : localesMap.entrySet()) {
                    bw.write(String.format("%s,%s", entry.getKey(), entry.getValue()));
                    bw.newLine();
                }
                bw.flush();
            }
        }
    }

    private File getFiles(String type, String locale) throws Exception {
        CloseableHttpClient client = getClient();

        String urlTmp = "https://apim.expedia.com/feed/downloadUrl?locale=%s&type=%s";

        String url = String.format(urlTmp, locale, type);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Key", "D4541739-756C-4627-9131-6BB802799866");
        httpGet.setHeader("Authorization", "Basic RDQ1NDE3MzktNzU2Qy00NjI3LTkxMzEtNkJCODAyNzk5ODY2OjhHc1J4RU1JTmN3RlNWZFI=");
        String downloadUrl = getDownloadUrl(httpGet, client);
        if (downloadUrl != null) {
            httpGet = new HttpGet(downloadUrl);
            String root = "E:\\SDP\\";
            String fileName = type + "-" +  locale;
            String suffix = ".zip";
            String filePath = root + fileName + suffix;
            downloadFile(filePath, httpGet, client);
            long startTime = System.currentTimeMillis();
            System.out.printf("Unzip file %s is starting.%n", filePath);
            ZipUtil.unZipFile(filePath);
            System.out.printf("Unzip file %s is done. %d%n", filePath, System.currentTimeMillis() - startTime);
            String zipFilePath = root + fileName;
            File directory = new File(zipFilePath);
            if (!directory.isDirectory() || directory.listFiles() == null) {
                return null;
            }
            FileUtils.deleteQuietly(new File(filePath));
            return directory;
        }
        return null;
    }

    private String getDownloadUrl(HttpGet httpGet, CloseableHttpClient client) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
        }
        return new JSONObject(sb.toString()).optString("downloadUrl", null);
    }

    private void downloadFile(String filePath, HttpGet httpGet, CloseableHttpClient client) throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.printf("Download file %s is starting...%n", filePath);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            try (InputStream inputStream = response.getEntity().getContent();
                 FileOutputStream outputStream = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[4096];
                int ch = 0;
                while ((ch = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, ch);
                }
                outputStream.flush();
            }
        }
        System.out.printf("Download file %s is done. %d%n", filePath, (System.currentTimeMillis() - startTime));
    }

    private CloseableHttpClient getClient() throws Exception {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager(registry);
        clientConnectionManager.setMaxTotal(100);

        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();

        return HttpClients.custom()
                .setConnectionManager(clientConnectionManager)
                .setDefaultRequestConfig(config)
                .build();
    }

}
