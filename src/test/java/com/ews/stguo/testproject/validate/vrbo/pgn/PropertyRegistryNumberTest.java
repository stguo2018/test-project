package com.ews.stguo.testproject.validate.vrbo.pgn;

import com.ews.stguo.testproject.utils.client.ClientCreator;
import com.ews.stguo.testproject.utils.client.LcsClient;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.Attribute;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.PropertyContentLoc;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.PropertyContentRoot;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.Section;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.SectionGroup;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.BooleanUtils;
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
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@RunWith(SpringRunner.class)
public class PropertyRegistryNumberTest {

    @Test
    public void test1() throws Exception {
        List<String> idlist = ControlFileRWUtils.loadHotelIdStrByPaths("VRProperties.csv").stream()
                .map(s -> s.split(",")[0])
                .collect(Collectors.toList());

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
        String tempUrl = "https://localhost:5402/properties/contents?excludeInactiveRooms=false&includeBedTypeOccupancy=true&excludeAddresses=false&langIds=1033&mediaSizeTypes=3,14,15&excludeMediaList=true&excludeRatePlans=true&propertyIds=%s&sectionTypes=1,21,22,226,367,11,6,5,556,3,418,197,199,169,471,233&excludeRoomTypes=false&includeSpaces=true";
        RateLimiter rateLimiter = RateLimiter.create(15);
        List<List<String>> pts = Lists.partition(idlist, Math.min(100, idlist.size()));
        int count = 1;
        for (List<String> ids : pts) {
            System.out.printf("%s(%d/%d)%n", ZonedDateTime.now(), count++, pts.size());
            rateLimiter.acquire();
            String url = String.format(tempUrl, String.join(",", ids));
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User", "EWSLPP");
            httpGet.setHeader("Accept-Encoding", "gzip");
            httpGet.setHeader("request-id", UUID.randomUUID().toString());
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    boolean c = handle(response.getEntity().getContent());
                    if (!c) {
                        //count++;
                    }
                    if (count == 20) {
                        //break;
                    }
                } else {
                    System.out.println(statusCode + ":" + response.getStatusLine().getReasonPhrase());
                }
            }
        }
    }

    private boolean handle(InputStream inputStream) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONArray cs = new JSONArray(sb.toString());
            if (cs != null && cs.length() > 0) {
                for (int i = 0; i < cs.length(); i++) {
                    boolean c = handleStep2(cs.optJSONObject(i));
                    if (!c) {
                        System.out.println(cs.optJSONObject(i));
                        return c;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean handleStep2(JSONObject property) throws Exception {
        Integer id = property.optInt("id");
        JSONArray contents = property.optJSONArray("contents");
        if (contents != null && contents.length() > 0) {
            JSONObject content = contents.getJSONObject(0);
            JSONArray sectionGroups = content.optJSONArray("sectionGroups");
            JSONObject policy = findSectionById(11, sectionGroups);
            JSONObject vl = findSectionById(471, sectionGroups);
            JSONObject s233 = findSectionById(233, sectionGroups);

            String registryNumber = null;
            if (policy != null) {
                registryNumber = findAttributeValueById(5109, policy);
            } else if (vl != null) {
                registryNumber = findParagraphsTextByRank(1, vl);
            }

            if (s233 != null) {
                registryNumber = findParagraphsTextByRank(1, s233);
                if (registryNumber != null) {
                    if (registryNumber.startsWith("Property Registration Number ")) {
                        try (BufferedWriter bw = RWFileUtils.getWriter("RegistryNumber233-F1.csv", true)) {
                            bw.write(id + "," + registryNumber);
                            bw.newLine();
                            bw.flush();
                        }
                    } else {
                        try (BufferedWriter bw = RWFileUtils.getWriter("RegistryNumber233-Other.csv", true)) {
                            bw.write(id + "," + registryNumber);
                            bw.newLine();
                            bw.flush();
                        }
                    }
                }
            }

//            if (policy != null) {
//                String registryNumber = findAttributeValueById(5109, policy);
//                if (registryNumber != null) {
//                    try (BufferedWriter bw = RWFileUtils.getWriter("RegistryNumber5109.csv", true)) {
//                        bw.write(id + "," + registryNumber);
//                        bw.newLine();
//                        bw.flush();
//                    }
//                }
//            }
//            if (vl != null) {
//                String registryNumber = findParagraphsTextByRank(1, vl);
//                if (registryNumber != null) {
//                    if (registryNumber.startsWith("Property Registration Number ")) {
//                        String str = registryNumber.replace("Property Registration Number ", "");
//                        try (BufferedWriter bw = RWFileUtils.getWriter("RegistryNumber471-Matched.csv", true)) {
//                            bw.write(id + "," + str);
//                            bw.newLine();
//                            bw.flush();
//                        }
//                    } else {
//                        try (BufferedWriter bw = RWFileUtils.getWriter("RegistryNumber471-UnMatched.csv", true)) {
//                            bw.write(id + "," + registryNumber);
//                            bw.newLine();
//                            bw.flush();
//                        }
//                    }
//                }
//            }
        }
        return true;
    }

    private String findParagraphsTextByRank(Integer rank, JSONObject section) {
        JSONArray paragraphs = section.optJSONArray("paragraphs");
        if (paragraphs == null || paragraphs.length() <= 0) {
            return null;
        }
        for (int i = 0; i < paragraphs.length(); i++) {
            JSONObject paragraph = paragraphs.optJSONObject(i);
            Integer aid = paragraph.optInt("rank");
            if (Objects.equals(rank, aid)) {
                return paragraph.optString("text");
            }
        }
        return null;
    }

    private String findAttributeValueById(Integer id, JSONObject section) {
        JSONArray attributes = section.optJSONArray("attributes");
        if (attributes == null || attributes.length() <= 0) {
            return null;
        }
        for (int i = 0; i < attributes.length(); i++) {
            JSONObject attribute = attributes.optJSONObject(i);
            Integer aid = attribute.optInt("id");
            if (Objects.equals(id, aid)) {
                return attribute.optString("value");
            }
        }
        return null;
    }

    private JSONObject findSectionById(Integer sectionId, JSONArray sgs) {
        if (sgs == null || sgs.length() <= 0) {
            return null;
        }
        for (int i = 0; i < sgs.length(); i++) {
            JSONObject sg = sgs.optJSONObject(i);
            if (sg != null) {
                JSONArray sections = sg.optJSONArray("sections");
                if (sections != null && sections.length() > 0) {
                    for (int j = 0; j < sections.length(); j++) {
                        JSONObject section = sections.optJSONObject(i);
                        if (section != null) {
                            Integer id = section.optInt("id");
                            if (Objects.equals(sectionId, id)) {
                                return section;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Test
    public void test2() throws Exception {
        String name = "expedia-lodging-%d-all.jsonl";
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            try (BufferedReader br = RWFileUtils.getReader(String.format(name, i))) {
                String line;
                while ((line = br.readLine()) != null) {
                    JSONObject vr = new JSONObject(line);
                    JSONObject propertyId = vr.optJSONObject("propertyId");

                    String id = propertyId.optString("expedia");
                    if (StringUtils.isNotBlank(id)) {
                        ids.add(id);
                    }
                }
            }
        }

        try (BufferedWriter bw = RWFileUtils.getWriter("VRProperties.csv")) {
            for (String id : ids) {
                bw.write(id);
                bw.newLine();
            }
            bw.flush();
        }

    }

    @Test
    public void test3() throws Exception {
        Set<String> vrIds = new HashSet<>(Arrays.asList("7","9","10","11","14","16","17","18","22","23","24","33"));
        List<String> idlist = ControlFileRWUtils.loadHotelIdStrByPaths("VRProperties.csv").stream()
                .map(s -> s.split(",")[0])
                .collect(Collectors.toList());
        System.out.println(idlist.size());

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
        String tempUrl = "https://localhost:5403/properties/shells?excludedPropertyTypeIds=false&propertyIds=%s";
        RateLimiter rateLimiter = RateLimiter.create(30);
        List<List<String>> pts = Lists.partition(idlist, Math.min(100, idlist.size()));
        int count = 1;
        List<String> vrboActiveIdList = new ArrayList<>();
        List<String> ecomLocalVRActiveIdList = new ArrayList<>();
        List<String> otherVRActiveIdList = new ArrayList<>();
        for (List<String> ids : pts) {
            System.out.printf("%s(%d/%d)%n", ZonedDateTime.now(), count++, pts.size());
            rateLimiter.acquire();
            String url = String.format(tempUrl, String.join(",", ids));
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User", "EWSLPP");
            httpGet.setHeader("Accept-Encoding", "gzip");
            httpGet.setHeader("request-id", UUID.randomUUID().toString());
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    boolean c = handleLDS(response.getEntity().getContent(), vrboActiveIdList,
                            ecomLocalVRActiveIdList, otherVRActiveIdList,vrIds);
                    if (!c) {
                        //count++;
                    }
                    if (count == 20) {
                        //break;
                    }
                } else {
                    System.out.println(statusCode + ":" + response.getStatusLine().getReasonPhrase());
                }
            }
        }
        try (BufferedWriter bw1 = RWFileUtils.getWriter("Vrbo Active Id List.csv")) {
            bw1.write("HotelId,TSPIds,StructuTypeId");
            bw1.newLine();
            for (String s : vrboActiveIdList) {
                bw1.write(s);
                bw1.newLine();
            }
        }
        try (BufferedWriter bw1 = RWFileUtils.getWriter("Ecom Local VR Active Id List.csv")) {
            bw1.write("HotelId,TSPIds,StructuTypeId");
            bw1.newLine();
            for (String s : ecomLocalVRActiveIdList) {
                bw1.write(s);
                bw1.newLine();
            }
        }
        try (BufferedWriter bw1 = RWFileUtils.getWriter("Other Vrbo Active Id List.csv")) {
            bw1.write("HotelId,TSPIds,StructuTypeId");
            bw1.newLine();
            for (String s : otherVRActiveIdList) {
                bw1.write(s);
                bw1.newLine();
            }
        }
    }

    private boolean handleLDS(InputStream inputStream, List<String> vrboActiveIdList,
                              List<String> ecomLocalVRActiveIdList,
            List<String> otherVRActiveIdList
            , Set<String> vrIds) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONArray cs = new JSONArray(sb.toString());
            if (cs != null && cs.length() > 0) {
                for (int i = 0; i < cs.length(); i++) {
                    JSONObject content = cs.optJSONObject(i);
                    if (content == null) {
                        continue;
                    }
                    String propertyId = content.optString("propertyID");
                    if (StringUtils.isBlank(propertyId)) {
                        continue;
                    }
                    JSONArray providers = content.optJSONArray("providers");
                    Map<String, Boolean> activeStatus = new HashMap<>();
                    if (providers == null || providers.length() <= 0) {
                        continue;
                    }
                    for (int j = 0; j < providers.length(); j++) {
                        JSONObject provider = providers.optJSONObject(j);
                        if (provider == null) {
                            continue;
                        }
                        String id = provider.optString("id");
                        Boolean active = provider.optBoolean("active");
                        activeStatus.put(id, active);
                    }
                    String structTypeId = content.optString("structureTypeID");
                    boolean is83 = Optional.ofNullable(activeStatus.get("83")).orElse(false);
                    boolean is103 = Optional.ofNullable(activeStatus.get("103")).orElse(false);
                    boolean is24 = Optional.ofNullable(activeStatus.get("24")).orElse(false);
                    if (is24 && !is83 && !is103 && vrIds.contains(structTypeId)) {
                        ecomLocalVRActiveIdList.add(String.format("%s,%s,%s", propertyId,
                                String.join(";", activeStatus.keySet()), structTypeId));
                    } else if (is83 || is103) {
                        vrboActiveIdList.add(String.format("%s,%s,%s", propertyId,
                                String.join(";", activeStatus.keySet()), structTypeId));
                    } else {
                        otherVRActiveIdList.add(String.format("%s,%s,%s", propertyId,
                                String.join(";", activeStatus.keySet()), structTypeId));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Test
    public void test4() throws Exception {
        List<String> idlist = ControlFileRWUtils.loadHotelIdStrByPaths("VRProperties.csv").stream()
                .map(s -> s.split(",")[0])
                .collect(Collectors.toList());
        List<List<String>> pts = Lists.partition(idlist, Math.min(100, idlist.size()));
        RateLimiter rateLimiter = RateLimiter.create(10);
        String tempUrl = "https://localhost:5402/properties/contents?excludeInactiveRooms=false&includeBedTypeOccupancy=true&excludeAddresses=false&langIds=1033&mediaSizeTypes=3,14,15&excludeMediaList=true&excludeRatePlans=true&propertyIds=%s&sectionTypes=1,21,22,226,367,11,6,5,556,3,418,197,199,169,471,233&excludeRoomTypes=false&includeSpaces=true";
        int count = 1;
        int count2 = 0;
        int count3 = 0;
        CloseableHttpClient httpClient = ClientCreator.createHttpClient();
        for (List<String> ids : pts) {
            if (count2 >= 20 && count3 >= 20) {
                break;
            }
            System.out.printf("%s(%d/%d)%n", ZonedDateTime.now(), count++, pts.size());
            rateLimiter.acquire();
            List<PropertyContentRoot> propertyContentRoots = LcsClient.callLcs(tempUrl, ids, httpClient);
            for (PropertyContentRoot propertyContentRoot : propertyContentRoots) {
                Integer hotelId = propertyContentRoot.getId();
                for (PropertyContentLoc content : propertyContentRoot.getContents()) {
                    List<SectionGroup> sectionGroups = content.getSectionGroups();
                    List<Section> sections = sectionGroups.stream().map(SectionGroup::getSections)
                            .flatMap(Collection::stream).collect(Collectors.toList());
                    Section policy = sections.stream().filter(Objects::nonNull).filter(s -> s.getId() == 11)
                            .findFirst().orElse(null);
                    Attribute propertyRegistryNumber = Optional.ofNullable(policy)
                            .map(Section::getAttributes)
                            .flatMap(attributes -> attributes.stream().filter(a -> Objects.nonNull(a) && a.getId() == 5109).findFirst())
                            .orElse(null);
                    Section vrRegulation = sections.stream().filter(Objects::nonNull).filter(s -> s.getId() == 471)
                            .findFirst().orElse(null);
                    if (propertyRegistryNumber == null && vrRegulation != null && count2 < 20) {
                        System.out.println("No5109,471:" + hotelId);
                        count2++;
                    }
                    if (propertyRegistryNumber != null && vrRegulation != null && count3 < 20) {
                        System.out.println("5109,471:" + hotelId);
                        count3++;
                    }
                }
            }
        }
    }

    @Test
    public void test5() throws Exception {
        String name = "expedia-lodging-%d-all.jsonl";
        int count1 = 0;
        int count2 = 0;
        for (int i = 1; i <= 3; i++) {
            try (BufferedReader br = RWFileUtils.getReader(String.format(name, i))) {
                String line;
                while ((line = br.readLine()) != null) {
                    JSONObject vr = new JSONObject(line);
                    JSONObject propertyId = vr.optJSONObject("propertyId");

                    String id = propertyId.optString("expedia");
                    if (StringUtils.isNotBlank(id)) {
                        if (id.equals("37749583")) {
                            System.out.println(line);
                        }
                        count1++;
//                        JSONObject rp = vr.optJSONObject("referencePrice");
//                        JSONObject rp = vr.optJSONObject("propertyType");
                        String rp = vr.optString("maxOccupancy");
                        if (rp != null) {
//                            String value = rp.optString("value");
//                            String value = rp.optString("id");
                            String value = rp;
                            if (rp.equals("0")) {
                                System.out.println(line);
                            }
                            if (StringUtils.isNotBlank(value) && !StringUtils.equalsIgnoreCase(value, "0")) {
                                count2++;
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("%d/%d%n", count2, count1);
    }

    @Test
    public void test6() throws Exception {
        List<String> strings = ControlFileRWUtils.loadHotelIdStrByPaths("sddp-feed-control-file (3).csv");
        List<String> idlist = new ArrayList<>();
        for (String string : strings) {
            String[] cs = string.split(",");
            idlist.add(cs[0]);
        }
        CloseableHttpClient httpClient = ClientCreator.createHttpClient();
        String tempUrl = "https://localhost:5403/properties/shells?excludedPropertyTypeIds=false&propertyIds=%s";
        RateLimiter rateLimiter = RateLimiter.create(30);
        List<List<String>> pts = Lists.partition(idlist, Math.min(100, idlist.size()));
        int count = 0;
        for (List<String> ids : pts) {
            rateLimiter.acquire();
            String url = String.format(tempUrl, String.join(",", ids));
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User", "EWSLPP");
            httpGet.setHeader("Accept-Encoding", "gzip");
            httpGet.setHeader("request-id", UUID.randomUUID().toString());
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        JSONArray cs = new JSONArray(sb.toString());
                        if (cs != null && cs.length() > 0) {
                            for (int i = 0; i < cs.length(); i++) {
                                JSONObject content = cs.optJSONObject(i);
                                if (content == null) {
                                    continue;
                                }
                                String propertyId = content.optString("propertyID");
                                if (StringUtils.isBlank(propertyId)) {
                                    continue;
                                }
                                JSONArray providers = content.optJSONArray("providers");
                                if (providers == null || providers.length() <= 0) {
                                    continue;
                                }
                                if (providers.length() == 1) {
                                    JSONObject provider = providers.optJSONObject(0);
                                    String id = provider.optString("id");
                                    Boolean active = provider.optBoolean("active");
                                    if (id.equals("103") && BooleanUtils.isTrue(active)) {
                                        count++;
                                        System.out.println(propertyId);
                                    }
                                    if (count == 100) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    System.out.println(statusCode + ":" + response.getStatusLine().getReasonPhrase());
                }
            }
        }
    }

}
