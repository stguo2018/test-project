package com.ews.stguo.testproject.validate.vrbo.pdq;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.validate.vrbo.HotelEntry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class PDQDateTest {

    @Test
    public void test01() throws Exception {
        Set<String> ecomIdVrboMapping = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("ews-hotel-static-ecomid-vrbo-mapping.csv");
             BufferedWriter bw = RWFileUtils.getWriter("ecomid-vrbo-mapping-after-distinct-mapping.csv")) {
            bw.write("hotelId,listingTriad");
            bw.newLine();
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                if (ecomIdVrboMapping.add(ecomId)) {
                    bw.write(String.format("%s,%s", ecomId, columns[1]));
                    bw.newLine();
                }
            }
            bw.flush();
        }
    }

    @Test
    public void test02() throws Exception {
        Set<String> tspId24 = new HashSet<>();
        Set<String> tspId83 = new HashSet<>();
        Set<String> tspId103 = new HashSet<>();
        Set<String> tspId24and83 = new HashSet<>();
        Set<String> tspId24and103 = new HashSet<>();
        Set<String> tspId83and103 = new HashSet<>();
        Set<String> tspId24and83and103 = new HashSet<>();
        Set<String> tspIdOthers = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("sddp-feed-control-file.csv")) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                Set<String> tspIds = new HashSet<>(Arrays.asList(columns[2].split(";")));
                if (tspIds.contains("24") && !tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId83.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24and83.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and103.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId83and103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and83and103.add(ecomId + "," + columns[2]);
                } else {
                    tspIdOthers.add(ecomId + "," + columns[2]);
                }
            }
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        if (CollectionUtils.isNotEmpty(tspIdOthers)) {
            try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-otherTspIds.csv")) {
                bw.write("hotelId,tspIds");
                bw.newLine();
                for (String s : tspIdOthers) {
                    bw.write(s);
                    bw.newLine();
                }
                bw.flush();
            }
        }

    }

    @Test
    public void test03() throws Exception {
        Set<String> sddpIds = new HashSet<>();
        Map<String, String> sddpContents = new HashMap<>();
        try (BufferedReader br = RWFileUtils.getReader("sddp-feed-control-file.csv")) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                sddpIds.add(ecomId);
                sddpContents.put(ecomId, columns[2]);
            }
        }

        Set<String> vrboMapping = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("ecomid-vrbo-mapping-after-distinct-mapping.csv")) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                vrboMapping.add(ecomId);
            }
        }

        Set<String> missInSddpIds = new HashSet(CollectionUtils.removeAll(new ArrayList(sddpIds), vrboMapping));
        Set<String> missInVrobMapping = new HashSet(CollectionUtils.removeAll(new ArrayList(vrboMapping), sddpIds));
        Set<String> retainAll = new HashSet(CollectionUtils.retainAll(new ArrayList(sddpIds), vrboMapping));
        System.out.println(missInSddpIds.size());
        System.out.println(missInVrobMapping.size());
        System.out.println(retainAll.size());
        try (BufferedReader br = RWFileUtils.getReader("ecomid-vrbo-mapping-after-distinct-mapping.csv");
             BufferedWriter bw = RWFileUtils.getWriter("PDQ/exclude-sddp-vrbo-mapping.csv");
             BufferedWriter bw2 = RWFileUtils.getWriter("PDQ/include-sddp-vrbo-mapping.csv");) {
            bw.write("hotelId,listingTriad,tspIds");
            bw.newLine();
            bw2.write("hotelId,listingTriad,tspIds");
            bw2.newLine();
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                if (missInVrobMapping.contains(ecomId)) {
                    bw.write(line + "," + sddpContents.get(ecomId));
                    bw.newLine();
                } else {
                    bw2.write(line + "," + sddpContents.get(ecomId));
                    bw2.newLine();
                }
            }
            bw.flush();
            bw2.flush();
        }
    }

    @Test
    public void test04() throws Exception {
        Set<String> tspId24 = new HashSet<>();
        Set<String> tspId83 = new HashSet<>();
        Set<String> tspId103 = new HashSet<>();
        Set<String> tspId24and83 = new HashSet<>();
        Set<String> tspId24and103 = new HashSet<>();
        Set<String> tspId83and103 = new HashSet<>();
        Set<String> tspId24and83and103 = new HashSet<>();
        Set<String> tspIdOthers = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("PDQ/included-sddp-vrbo-mapping.csv")) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                Set<String> tspIds = new HashSet<>(Arrays.asList(columns[2].split(";")));
                if (tspIds.contains("24") && !tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId83.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24and83.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and103.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId83and103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and83and103.add(ecomId + "," + columns[2]);
                } else {
                    tspIdOthers.add(ecomId + "," + columns[2]);
                }
            }
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        if (CollectionUtils.isNotEmpty(tspIdOthers)) {
            try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-otherTspIds.csv")) {
                bw.write("hotelId,tspIds");
                bw.newLine();
                for (String s : tspIdOthers) {
                    bw.write(s);
                    bw.newLine();
                }
                bw.flush();
            }
        }

    }

    @Test
    public void test05() throws Exception {
        Set<String> pdq = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("ecomid-vrbo-mapping-after-distinct-mapping.csv")) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                pdq.add(line.split(",")[0]);
            }
        }
        Set<String> tspId24 = new HashSet<>();
        Set<String> tspId83 = new HashSet<>();
        Set<String> tspId103 = new HashSet<>();
        Set<String> tspId24and83 = new HashSet<>();
        Set<String> tspId24and103 = new HashSet<>();
        Set<String> tspId83and103 = new HashSet<>();
        Set<String> tspId24and83and103 = new HashSet<>();
        Set<String> tspIdOthers = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("sddp-feed-control-file.csv")) {
            int count = 0;
            String line;// = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                if (pdq.contains(ecomId)) {
                    continue;
                }
                count++;
                Set<String> tspIds = new HashSet<>(Arrays.asList(columns[2].split(";")));
                if (tspIds.contains("24") && !tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId83.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24and83.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and103.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId83and103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and83and103.add(ecomId + "," + columns[2]);
                } else {
                    tspIdOthers.add(ecomId + "," + columns[2]);
                }
            }
            System.out.println(count);
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        if (CollectionUtils.isNotEmpty(tspIdOthers)) {
            try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-otherTspIds.csv")) {
                bw.write("hotelId,tspIds");
                bw.newLine();
                for (String s : tspIdOthers) {
                    bw.write(s);
                    bw.newLine();
                }
                bw.flush();
            }
        }
    }

    @Test
    public void test06() throws Exception {
        Set<String> pdq = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("ecomid-vrbo-mapping-after-distinct-mapping.csv")) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                pdq.add(line.split(",")[0]);
            }
        }
        Set<String> withoutPdq = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("sddp-feed-control-file.csv")) {
            int count = 0;
            String line;// = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                if (pdq.contains(ecomId)) {
                    continue;
                }
                count++;
                withoutPdq.add(ecomId + "," + columns[2]);
            }
            System.out.println(count);
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("without-pdq-mapping.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : withoutPdq) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
    }

    @Test
    public void test07() throws Exception {
        Map<String, String> pdq = new HashMap<>();
        try (BufferedReader br = RWFileUtils.getReader("ecomid-vrbo-mapping-after-distinct-mapping.csv")) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                pdq.put(line.split(",")[0], line);
            }
        }
        Map<String, String> ableToGetVrboPropertyIdFromLCS = new HashMap<>();
        try (BufferedReader br = RWFileUtils.getReader("Can get VrboPropertyId from lcs ids.csv")) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                ableToGetVrboPropertyIdFromLCS.put(line.split(",")[0], line);
            }
        }

        Collection<String> intersectionIds = CollectionUtils.retainAll(new ArrayList(pdq.keySet()), ableToGetVrboPropertyIdFromLCS.keySet());
        Collection<String> withoutPDQ = CollectionUtils.removeAll(new ArrayList(pdq.keySet()), ableToGetVrboPropertyIdFromLCS.keySet());
        Collection<String> withoutSDDP = CollectionUtils.removeAll(new ArrayList(ableToGetVrboPropertyIdFromLCS.keySet()), pdq.keySet());
        Collection<String> union = CollectionUtils.union(new ArrayList(ableToGetVrboPropertyIdFromLCS.keySet()), pdq.keySet());
        System.out.println(pdq.size());
        System.out.println(ableToGetVrboPropertyIdFromLCS.size());
        System.out.println(intersectionIds.size());
        System.out.println(withoutPDQ.size());
        System.out.println(withoutSDDP.size());
        System.out.println(union.size());
        try (BufferedWriter bw = RWFileUtils.getWriter("IntersectionIds.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : intersectionIds) {
                String line = pdq.get(s);
                if (line == null) {
                    line = ableToGetVrboPropertyIdFromLCS.get(s);
                }
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("WithoutForPDQ.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : withoutPDQ) {
                String line = pdq.get(s);
                if (line == null) {
                    line = ableToGetVrboPropertyIdFromLCS.get(s);
                }
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("WithoutForSDDP.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : withoutSDDP) {
                String line = pdq.get(s);
                if (line == null) {
                    line = ableToGetVrboPropertyIdFromLCS.get(s);
                }
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("UnionForPDQ&SQDDP.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : union) {
                String line = pdq.get(s);
                if (line == null) {
                    line = ableToGetVrboPropertyIdFromLCS.get(s);
                }
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        }
    }

    @Test
    public void test08() throws Exception {
        Set<String> able = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("Able to get VrboPropertyId from the LCS.csv")) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                able.add(line.split(",")[0]);
            }
        }
        Set<String> able2 = new HashSet<>();
        Set<String> tspId24 = new HashSet<>();
        Set<String> tspId83 = new HashSet<>();
        Set<String> tspId103 = new HashSet<>();
        Set<String> tspId24and83 = new HashSet<>();
        Set<String> tspId24and103 = new HashSet<>();
        Set<String> tspId83and103 = new HashSet<>();
        Set<String> tspId24and83and103 = new HashSet<>();
        Set<String> tspIdOthers = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("sddp-feed-control-file.csv")) {
            int count = 0;
            String line;// = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                if (!able.contains(ecomId)) {
                    continue;
                }
                able2.add(ecomId + "," + columns[2]);
                count++;
                Set<String> tspIds = new HashSet<>(Arrays.asList(columns[2].split(";")));
                if (tspIds.contains("24") && !tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId83.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24and83.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and103.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId83and103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and83and103.add(ecomId + "," + columns[2]);
                } else {
                    tspIdOthers.add(ecomId + "," + columns[2]);
                }
            }
            System.out.println(count);
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        if (CollectionUtils.isNotEmpty(tspIdOthers)) {
            try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-otherTspIds.csv")) {
                bw.write("hotelId,tspIds");
                bw.newLine();
                for (String s : tspIdOthers) {
                    bw.write(s);
                    bw.newLine();
                }
                bw.flush();
            }
        }
        if (CollectionUtils.isNotEmpty(able2)) {
            try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-has-vrboId-from-lcs.csv")) {
                bw.write("hotelId,tspIds");
                bw.newLine();
                for (String s : able2) {
                    bw.write(s);
                    bw.newLine();
                }
                bw.flush();
            }
        }
    }

    @Test
    public void test09() throws Exception {
        List<String> c1 = ControlFileRWUtils.loadHotelIdStrByPaths("control-file (1).csv");
        System.out.println(c1.size());
        Map<String, String> map = new HashMap<>();
        for (String s : c1) {
            String[] columns = s.split(",");
            map.put(columns[0], s);
        }
        List<String> c2 = ControlFileRWUtils.loadHotelIdStrByPaths("control-file (2).csv");
        System.out.println(c2.size());
        for (String s : c2) {
            String[] columns = s.split(",");
            map.put(columns[0], s);
        }
        System.out.println(c1.size() + c2.size());
        System.out.println(map.size());
        try (BufferedWriter bw = RWFileUtils.getWriter("full-control-file.csv")) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                bw.write(entry.getValue());
                bw.newLine();
            }
            bw.flush();
        }
    }

    @Test
    public void test10() throws Exception {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxWaitMillis(10000);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        ObjectMapper om = new ObjectMapper();
        JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 57379, 10000);
//        JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 16379, 10000);
        List<String> c1 = ControlFileRWUtils.loadHotelIdStrByPaths("control-file-full.csv");
        System.out.println(c1.size());

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

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(clientConnectionManager)
                .setDefaultRequestConfig(config)
                .build();
        String tempUrl = "https://localhost:44572/properties/shells?excludedPropertyTypeIds=false&propertyIds={ids}";
        AtomicInteger ct = new AtomicInteger(0);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> System.out.println(LocalDateTime.now().toString() + "---" + ct.get() + "/" + c1.size()), 0, 5, TimeUnit.SECONDS);

        RateLimiter rateLimiter = RateLimiter.create(10);
        Map<String, String> kv = new ConcurrentHashMap<>();
        List<String> list = new ArrayList<>();
        for (String s : c1) {
            String[] columns = s.split(",");
            String id = columns[0];
            list.add(id);
            if (list.size() == 100) {
                rateLimiter.acquire();
                List<HotelEntry> hotelEntries = getHotelEntries(tempUrl, list, httpClient);
                list = new ArrayList<>();
                for (HotelEntry he : hotelEntries) {
                    String key = he.getHotelID() + ":lds";
                    String value = null;
                    try {
                        value = om.writeValueAsString(he);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (he.getHotelID() != null && value != null) {
                        kv.put(key, value);
                    }
                }
            }
            if (kv.size() >= 1000) {
                try (Jedis jedis = jedisPool.getResource()) {
                    Pipeline pipeline = jedis.pipelined();
                    for (Map.Entry<String, String> e : kv.entrySet()) {
                        pipeline.set(e.getKey(), e.getValue());
                        ct.incrementAndGet();
                    }
                    kv = new HashMap<>();
                    pipeline.sync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (list.size() > 0) {
            List<HotelEntry> hotelEntries = getHotelEntries(tempUrl, list, httpClient);
            for (HotelEntry he : hotelEntries) {
                String key = he.getHotelID() + ":lds";
                String value = null;
                try {
                    value = om.writeValueAsString(he);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (he.getHotelID() != null && value != null) {
                    kv.put(key, value);
                }
            }
        }
        if (kv.size() > 0) {
            try (Jedis jedis = jedisPool.getResource()) {
                Pipeline pipeline = jedis.pipelined();
                for (Map.Entry<String, String> e : kv.entrySet()) {
                    pipeline.set(e.getKey(), e.getValue());
                    ct.incrementAndGet();
                }
                pipeline.sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(ct.get() + "/" + c1.size());
        scheduledExecutorService.shutdown();
    }

    private List<HotelEntry> getHotelEntries(String tempUrl, List<String> list, CloseableHttpClient httpClient) {
        String url = tempUrl.replace("{ids}", String.join(",", list));
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User", "EWSLPP");
        httpGet.setHeader("Accept-Encoding", "gzip");
        httpGet.setHeader("request-id", UUID.randomUUID().toString());
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String line;
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    return buildHotelEntries(new JSONArray(sb.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(statusCode + ":" + response.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<HotelEntry> buildHotelEntries(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return new ArrayList<>();
        }

        List<HotelEntry> hotelEntries = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject == null) {
                continue;
            }
            HotelEntry he = new HotelEntry();
            Long propertyId = jsonObject.optLong("propertyID");
            he.setHotelID(propertyId);
            Boolean active = jsonObject.optBoolean("active");
            he.setActive(active);
            JSONArray providers = jsonObject.optJSONArray("providers");
            List<Long> tspIds = new ArrayList<>();
            if (providers != null && providers.length() > 0) {
                for (int j = 0; j < providers.length(); j++) {
                    JSONObject provider = providers.optJSONObject(j);
                    Long tspId = provider.optLong("id");
                    tspIds.add(tspId);
                }
                he.setProvider(tspIds);
            }

            hotelEntries.add(he);
        }

        return hotelEntries;
    }

    @Test
    public void test11() throws Exception {
        Set<String> fullKeys = new HashSet<>();
        Map<String, String> old = new HashMap<>();
        try (BufferedReader br = RWFileUtils.getReader("control-file-old.csv")) {
            String line;
            while ((line = br.readLine()) != null) {
                String ecomId = line.split(",")[0];
                fullKeys.add(ecomId);
                old.put(ecomId, line);
            }
        }
        System.out.println(old.size());
        int matchCount = 0;
        int newSize = 0;
        Map<String, String> added = new HashMap<>();
        Map<String, String> unmatched = new HashMap<>();
        try (BufferedReader br = RWFileUtils.getReader("control-file-new.csv")) {
            String line;
            while ((line = br.readLine()) != null) {
                ++newSize;
                String ecomid = line.split(",")[0];
                fullKeys.add(ecomid);
                if (old.containsKey(ecomid) && StringUtils.equals(line, old.get(ecomid))) {
                    ++matchCount;
                } else if (!old.containsKey(ecomid)) {
                    added.put(ecomid, line);
                } else {
                    unmatched.put(ecomid, old.get(ecomid) + ":" + line);
                }
            }
        }
        System.out.println(newSize);
        System.out.printf("(%d/%d)%.2f%%%n", matchCount, fullKeys.size(), new BigDecimal(matchCount * 100)
                .divide(new BigDecimal(fullKeys.size()), 2, RoundingMode.HALF_UP)
                .floatValue());
        System.out.println(added.size());
        System.out.println(unmatched.size());
        unmatched.forEach((k, v) -> System.out.println(v));
    }

    @Test
    public void test12() throws Exception {
        Set<String> able = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("control-file-incomplete.csv")) {
            String line;
            while ((line = br.readLine()) != null) {
                able.add(line.split(",")[0]);
            }
        }
        Set<String> tspId24 = new HashSet<>();
        Set<String> tspId83 = new HashSet<>();
        Set<String> tspId103 = new HashSet<>();
        Set<String> tspId24and83 = new HashSet<>();
        Set<String> tspId24and103 = new HashSet<>();
        Set<String> tspId83and103 = new HashSet<>();
        Set<String> tspId24and83and103 = new HashSet<>();
        Set<String> tspIdOthers = new HashSet<>();
        try (BufferedReader br = RWFileUtils.getReader("control-file-new.csv")) {
            int count = 0;
            String line;// = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                String ecomId = columns[0];
                try {
                    if (able.contains(ecomId) || columns.length < 3 || StringUtils.isBlank(columns[2])) {
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println(line);
                }
                count++;
                Set<String> tspIds = new HashSet<>(Arrays.asList(columns[2].split(";")));
                if (tspIds.contains("24") && !tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId83.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && !tspIds.contains("103")) {
                    tspId24and83.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && !tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and103.add(ecomId + "," + columns[2]);
                } else if (!tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId83and103.add(ecomId + "," + columns[2]);
                } else if (tspIds.contains("24") && tspIds.contains("83") && tspIds.contains("103")) {
                    tspId24and83and103.add(ecomId + "," + columns[2]);
                } else {
                    tspIdOthers.add(ecomId + "," + columns[2]);
                }
            }
            System.out.println(count);
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-24,83,103.csv")) {
            bw.write("hotelId,tspIds");
            bw.newLine();
            for (String s : tspId24and83and103) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        }
        if (CollectionUtils.isNotEmpty(tspIdOthers)) {
            try (BufferedWriter bw = RWFileUtils.getWriter("sddp-feed-control-file-tspId-otherTspIds.csv")) {
                bw.write("hotelId,tspIds");
                bw.newLine();
                for (String s : tspIdOthers) {
                    bw.write(s);
                    bw.newLine();
                }
                bw.flush();
            }
        }
    }

    @Test
    public void test13() throws Exception {
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                if (j % 2 == 0 || (i + j) < 10) {
                    continue;
                }
                int k = i + j - 10;
                int t = 2 * k + 1 - 10;
                int t2 = 2 * k + 1;
                if ((j != t && j != t2) || k <= 0) {
                    continue;
                }
                int t3 = i + j - 10;
                if (j == t) {
                    t3+=1;
                }
                int t4 = (i + j) / 10;
                if (t3 == k && t4 == k) {
                    System.out.printf("i:%d, j:%d, k:%d%n", i, j, k);
                }

            }
        }
        System.out.println("Done");
    }

    @Test
    public void test14() throws Exception {
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

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(clientConnectionManager)
                .setDefaultRequestConfig(config)
                .build();
        HttpGet httpGet = new HttpGet("https://apim.expedia.com/flights/listings?" +
                "segment1.origin=YYZ&segment1.destination=YYC&segment1.departureDate=2022-01-12&" +
                "adult=1&segment2.origin=YYC&segment2.destination=YYZ&segment2.departureDate=2022-01-19&" +
                "cabinClass=economy&filterBasicEconomy=true");
        httpGet.setHeader("Authorization", "Basic OTFCRTU4OTctRTAyQy00MTdGLTlFQjgtRUQ5NTYxRTg3NjRGOjFHQ3ZmbU95QjRieEdzV3E=");
        httpGet.setHeader("Accept", "application/vnd.exp-flight.v3+xml");
        httpGet.setHeader("Key", "91BE5897-E02C-417F-9EB8-ED9561E8764F");
        httpGet.setHeader("Partner-Transaction-Id", "RateGain");
        httpGet.setHeader("Accept-Encoding", "gzip,deflate");
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("Done!");
            } else {
                System.out.println(response.getStatusLine().getReasonPhrase());
            }
        }
    }

    @Test
    public void test15() throws Exception {
        List<String> oldList = ControlFileRWUtils
                .loadHotelIdStrByPaths("sddp-feed-control-file-2021-11-09.csv")
                .stream()
                .map(s -> s.split(",")[0])
                .collect(Collectors.toList());
        List<String> newList = ControlFileRWUtils
                .loadHotelIdStrByPaths("sddp-feed-control-file-2022-01-09.csv")
                .stream()
                .map(s -> s.split(",")[0])
                .collect(Collectors.toList());
        List<String> delList = ListUtils.removeAll(oldList, new HashSet<>(newList));
        List<String> addList = ListUtils.removeAll(newList, new HashSet<>(oldList));
        try (BufferedWriter bw = RWFileUtils.getWriter("Removed Hotel Id List.csv");
             BufferedWriter bw2 = RWFileUtils.getWriter("Added Hotel Id List.csv")) {
            for (String s : delList) {
                bw.write(s);
                bw.newLine();
            }
            for (String s : addList) {
                bw2.write(s);
                bw2.newLine();
            }
        }
    }

    @Test
    public void test16() throws Exception {

        ObjectMapper objMapper = new ObjectMapper();
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        String str = "{\"a\": \"aaa\", \"b\": \"bbb\"}";
        Data d = objMapper.readValue(str, Data.class);
        System.out.println(d.getA());
    }

    @Test
    public void test17() throws Exception {
        class Data {
            private String startDate;
            private String endDate;

            public String getStartDate() {
                return startDate;
            }

            public void setStartDate(String startDate) {
                this.startDate = startDate;
            }

            public String getEndDate() {
                return endDate;
            }

            public void setEndDate(String endDate) {
                this.endDate = endDate;
            }
        }
        List<Data> mockList = new ArrayList<>();
        List<Data> list = mockList.stream().map(data -> {
            Data data1 = new Data();
            data1.setStartDate(data.getStartDate());
            // ... more fields...
            Data data2 = new Data();
            data2.setEndDate(data.getEndDate());
            // ... more fields...
            return Arrays.asList(data1, data2);
        }).flatMap(Collection::stream).sorted((data1, data2) -> {
            String date1 = data1.getStartDate() != null ? data1.getStartDate() : data1.getEndDate();
            String date2 = data2.getStartDate() != null ? data2.getStartDate() : data2.getEndDate();
            LocalDateTime d1 = LocalDateTime.parse(date1);
            LocalDateTime d2 = LocalDateTime.parse(date2);
            return d1.isAfter(d2) ? 1 : -1;
        }).collect(Collectors.toList());
    }

    @Test
    public void test18() throws Exception {
        Map<String, String> newFeed = new HashMap<>();
        String head;
        int count = 0;
        try (BufferedReader br = RWFileUtils.getReader("hotels-local-1.tsv")) {
            String line = br.readLine();
            head = line;
            while ((line = br.readLine()) != null) {
                if (count++ % 10000 == 0) {
                    String hotelId = line.split("\t")[6];
                    newFeed.put(hotelId, line);
                }
            }
        }

        Map<String, String> oldFeed = new HashMap<>();
        String head2;
        count = 0;
        try (BufferedReader br = RWFileUtils.getReader("hotels-local-1_1.tsv")) {
            String line = br.readLine();
            head2 = line;
            String allLine = "";
            try {
                while ((line = br.readLine()) != "") {
                    count++;
                    String hotelId = (allLine + line).split("\t")[6];
                    if (newFeed.containsKey(hotelId)) {
                        oldFeed.put(hotelId, line);
                    }
                    allLine = "";
                }
            } catch (Exception e) {
                System.out.println(count + "---" + line);
            }
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("compare.txt")) {
            bw.write(head2);
            bw.newLine();
            bw.write(head);
            bw.newLine();
            bw.newLine();
            for (Map.Entry<String, String> entry : oldFeed.entrySet()) {
                bw.write(entry.getValue());
                bw.newLine();
                bw.write(newFeed.get(entry.getKey()));
                bw.newLine();
                bw.newLine();
                newFeed.remove(entry.getKey());
            }
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("remaining.txt")) {
            bw.write(head);
            bw.newLine();
            for (String value : newFeed.values()) {
                bw.write(value);
                bw.newLine();
            }
        }

    }

    @Test
    public void test19() throws Exception {
        Set<String> controlFileIds = ControlFileRWUtils
                .loadHotelIdStrByPaths("sddp-feed-control-file (2).csv")
                .stream().map(s -> s.split(",")[0]).collect(Collectors.toSet());
        System.out.println("ControlFileSize:" + controlFileIds.size());

        Set<String> sdpIds = new HashSet<>();
        Set<String> missIds = new HashSet<>();
        Set<String> contents = new HashSet<>();
        int geoLocationCount = 0;
        Random random = new Random();
        for (int i = 1; i <= 3; i++) {
            try (BufferedReader br = RWFileUtils.getReader(String.format("expedia-lodging-%d-all.jsonl", i))) {
                String line;
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
                        if (o.optJSONObject("geoLocation") != null) {
                            geoLocationCount++;
                        }
                    }

                }
            }
        }
        List<String> lossIds = ListUtils.removeAll(controlFileIds, sdpIds);
        System.out.println("SDPIds:" + sdpIds.size());
        System.out.println("MissIds:" + missIds.size());
        System.out.println("LostIds:" + lossIds.size());
        System.out.println("Contents:" + contents.size());
        System.out.println("geoLocationCount:" + geoLocationCount);
        try (BufferedWriter bw = RWFileUtils.getWriter("MissIds.csv")) {
            for (String sdpId : missIds) {
                bw.write(sdpId);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("LostIds.csv")) {
            for (String sdpId : lossIds) {
                bw.write(sdpId);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("HotelIdsInSDP.csv")) {
            for (String sdpId : sdpIds) {
                bw.write(sdpId);
                bw.newLine();
            }
            bw.flush();
        }
        try (BufferedWriter bw = RWFileUtils.getWriter("RandomContents.jsonl")) {
            for (String sdpId : contents) {
                bw.write(sdpId);
                bw.newLine();
            }
            bw.flush();
        }
    }

}

class Data {
    private String a;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }
}
