package com.ews.stguo.testproject.validate.vrbo.verify;

import com.ews.stguo.testproject.utils.client.ClientCreator;
import com.ews.stguo.testproject.utils.client.ContentProviderClient;
import com.ews.stguo.testproject.utils.client.DatabaseClient;
import com.ews.stguo.testproject.utils.client.LcsClient;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.Attribute;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.Paragraph;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.PropertyContentLoc;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.PropertyContentRoot;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.Section;
import com.expedia.e3.shopsvc.shared.thirdparty.lcs.SectionGroup;
import com.expedia.www.ews.models.propertyinfo.v2.response.ContentType;
import com.expedia.www.ews.models.propertyinfo.v2.response.PropertyIdType;
import com.expedia.www.ews.models.propertyinfo.v2.response.PropertyInfoResponseType;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class CommonVerify {

    @Test
    public void test01() throws Exception {
        List<String> filtersIds = ControlFileRWUtils.loadHotelIdStrByPaths("failed-hotel-ids.csv");
        List<String> controlFileIds = CollectionUtils.isNotEmpty(filtersIds) ? filtersIds :  new ArrayList<>(ControlFileRWUtils
                .loadHotelIdStrByPaths("sddp-feed-control-file (2).csv")
                .stream().map(s -> s.split(",")[0]).collect(Collectors.toSet()));
        System.out.println("ControlFileSize:" + controlFileIds.size());
        RateLimiter rateLimiter = RateLimiter.create(10);
        List<List<String>> partitions = Lists.partition(controlFileIds, Math.min(200, controlFileIds.size()));
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        AtomicInteger count = new AtomicInteger(0);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.printf("(%d/%d)%n", count.get(), partitions.size());
        }, 5, 5, TimeUnit.SECONDS);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CloseableHttpClient httpClient = ClientCreator.createHttpClient();
        String url = "https://localhost:4411/query/v2/propertyInfos";
        DataSource dataSource = DatabaseClient.getDataSource("jdbc:mysql://localhost:3306/sdp?useUnicode=true&characterEncoding=utf8");
        Set<String> failedHotelIds = new HashSet<>();
        for (List<String> partition : partitions) {
            futures.add(CompletableFuture.runAsync(() -> {
                rateLimiter.acquire();
                try (Connection conn = dataSource.getConnection()) {
                    PropertyInfoResponseType response = ContentProviderClient.getPropertyInfoV2(url, partition, httpClient);
                    dataStorage(conn, response);
                } catch (Exception e) {
                    System.out.println(String.join(",", partition));
                    failedHotelIds.addAll(partition);
                    e.printStackTrace();
                }
                count.incrementAndGet();
            }, executorService));
            if (futures.size() >= 100) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).get();
                futures = new ArrayList<>();
            }
        }
        if (CollectionUtils.isNotEmpty(futures)) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).get();
        }
        if (CollectionUtils.isNotEmpty(failedHotelIds)) {
            try (BufferedWriter bw = RWFileUtils.getWriter("failed-hotel-ids.csv")) {
                for (String failedHotelId : failedHotelIds) {
                    bw.write(failedHotelId);
                    bw.newLine();
                }
                bw.flush();
            }
        }
    }

    private void dataStorage(Connection conn, PropertyInfoResponseType response) throws Exception {
        if (response == null || CollectionUtils.isEmpty(response.getContents())) {
            return;
        }
        for (ContentType content : response.getContents()) {
            String queryTemp = "insert into %s(%s) values(%s)";
            StringBuilder columns = new StringBuilder();
            List<Object> params = new ArrayList<>();

            PropertyIdType propertyId = content.getPropertyId();
            columns.append("ecom_id");
            params.add(propertyId.getExpedia());
            Optional.ofNullable(propertyId.getHcom()).ifPresent(v -> {
                columns.append(",hcom_id");
                params.add(v);
            });
            Optional.ofNullable(propertyId.getVrbo()).ifPresent(v -> {
                columns.append(",vrbo_id");
                params.add(v);
            });
            Optional.ofNullable(content.getName()).ifPresent(v -> {
                columns.append(",property_name");
                params.add(v);
            });
            Optional.ofNullable(content.getBookable()).ifPresent(o -> {
                Optional.ofNullable(o.getExpedia()).ifPresent(v -> {
                    columns.append(",expedia_bookable");
                    params.add(v);
                });
                Optional.ofNullable(o.getHcom()).ifPresent(v -> {
                    columns.append(",hcom_bookable");
                    params.add(v);
                });
                Optional.ofNullable(o.getVrbo()).ifPresent(v -> {
                    columns.append(",vrbo_bookable");
                    params.add(v);
                });
            });
            Optional.ofNullable(content.getInstantBook()).ifPresent(o -> {
                columns.append(",instant_book");
                params.add(o);
            });
            Optional.ofNullable(content.getPropertyType()).ifPresent(o -> {
                Optional.ofNullable(o.getId()).ifPresent(v -> {
                    columns.append(",structure_type_id");
                    params.add(v);
                });
            });
            String values = params.stream().map(a -> "?").collect(Collectors.joining(","));
            String query = String.format(queryTemp, "common", columns, values);
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                for (int i = 0; i < params.size(); i++) {
                    int index = i + 1;
                    preparedStatement.setObject(index, params.get(i));
                }
                preparedStatement.executeUpdate();
            }
            Optional.ofNullable(content.getInventorySources()).ifPresent(os -> {
                Set<String> providerIds = new HashSet<>();
                os.forEach(o -> {
                    StringBuilder columns2 = new StringBuilder();
                    List<String> params2 = new ArrayList<>();
                    columns2.append("ecom_id");
                    params2.add(propertyId.getExpedia());
                    Optional.ofNullable(o.getId()).ifPresent(v -> {
                        columns2.append(",provider_id");
                        params2.add(v);
                    });
                    if (providerIds.add(params2.get(1))) {
                        String values2 = params2.stream().map(a -> "?").collect(Collectors.joining(","));
                        String query2 = String.format(queryTemp, "inventory_source", columns2, values2);
                        try (PreparedStatement preparedStatement = conn.prepareStatement(query2)) {
                            for (int i = 0; i < params2.size(); i++) {
                                int index = i + 1;
                                preparedStatement.setString(index, params2.get(i));
                            }
                            preparedStatement.executeUpdate();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            });
        }
    }

    @Test
    public void test02() throws Exception {
        List<String> controlFileIds = ControlFileRWUtils
                .loadHotelIdStrByPaths("sddp-feed-control-file (2).csv")
                .stream().map(s -> s.split(",")[0]).distinct().collect(Collectors.toList());
        System.out.println("ControlFileSize:" + controlFileIds.size());
        RateLimiter rateLimiter = RateLimiter.create(10);
        List<List<String>> partitions = Lists.partition(controlFileIds, Math.min(100, controlFileIds.size()));
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        AtomicInteger count = new AtomicInteger(0);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.printf("(%d/%d)%n", count.get(), partitions.size());
        }, 5, 5, TimeUnit.SECONDS);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CloseableHttpClient httpClient = ClientCreator.createHttpClient();
        String url = "https://localhost:5402/properties/contents?excludeInactiveRooms=false&includeBedTypeOccupancy=true&excludeAddresses=false&langIds=1033&mediaSizeTypes=3,14,15&excludeMediaList=true&excludeRatePlans=true&propertyIds=%s&sectionTypes=1,21,22,226,367,11,6,5,556,3,418,197,199,169,471,23,29,310,318&excludeRoomTypes=false&includeSpaces=true";
        DataSource dataSource = DatabaseClient.getDataSource("jdbc:mysql://localhost:3306/sdp?useUnicode=true&characterEncoding=utf8");
        for (List<String> partition : partitions) {
            futures.add(CompletableFuture.runAsync(() -> {
                rateLimiter.acquire();
                try (Connection conn = dataSource.getConnection()) {
                    List<PropertyContentRoot> response = LcsClient.callLcs(url, partition, httpClient);
                    dataStorage(conn, response);
                } catch (Exception e) {
                    System.out.println(String.join(",", partition));
                    e.printStackTrace();
                }
                count.incrementAndGet();
            }, executorService));
            if (futures.size() >= 100) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).get();
                futures = new ArrayList<>();
            }
        }
        if (CollectionUtils.isNotEmpty(futures)) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).get();
        }
    }

    private void dataStorage(Connection conn, List<PropertyContentRoot> response) throws Exception {
        if (response == null || CollectionUtils.isEmpty(response)) {
            return;
        }
        for (PropertyContentRoot propertyContentRoot : response) {
            if (propertyContentRoot == null || CollectionUtils.isEmpty(propertyContentRoot.getContents())) {
                continue;
            }
            String ecomId = String.valueOf(propertyContentRoot.getId());
            PropertyContentLoc propertyContentLoc = propertyContentRoot.getContents().get(0);
            String queryTemp = "insert into %s(%s) values(%s)";
            StringBuilder columns = new StringBuilder();
            List<Object> params = new ArrayList<>();
            columns.append("ecom_id");
            params.add(ecomId);
            List<Section> sections = Optional.ofNullable(propertyContentLoc.getSectionGroups())
                    .map(sgs -> sgs.stream().map(SectionGroup::getSections).flatMap(Collection::stream).collect(Collectors.toList()))
                    .orElse(new ArrayList<>());
            sections.stream().filter(sc -> Objects.equals(sc.getId(), 11))
                    .map(Section::getAttributes)
                    .flatMap(Collection::stream)
                    .filter(attr -> Objects.equals(attr.getId(), 5109))
                    .map(Attribute::getValue)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresent(v -> {
                        columns.append(",from_5109");
                        params.add(v);
                    });
            sections.stream().filter(sc -> Objects.equals(sc.getId(), 471))
                    .map(Section::getParagraphs)
                    .flatMap(Collection::stream)
                    .filter(p -> Objects.equals(p.getRank(), 1))
                    .map(Paragraph::getText)
                    .filter(StringUtils::isNotBlank)
                    .findFirst()
                    .ifPresent(v -> {
                        columns.append(",from_471");
                        params.add(v);
                    });
            String values = params.stream().map(a -> "?").collect(Collectors.joining(","));
            String query = String.format(queryTemp, "registry_number", columns, values);
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                for (int i = 0; i < params.size(); i++) {
                    int index = i + 1;
                    preparedStatement.setObject(index, params.get(i));
                }
                preparedStatement.executeUpdate();
            }
        }
    }

}
