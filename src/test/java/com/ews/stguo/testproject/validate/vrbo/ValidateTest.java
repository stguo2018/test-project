package com.ews.stguo.testproject.validate.vrbo;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.validate.vrbo.comparator.SummaryComparator;
import com.ews.stguo.testproject.validate.vrbo.comparator.VrboComparator;
import com.ews.stguo.testproject.validate.vrbo.generator.VrboIdMappingGenerator;
import com.ews.stguo.testproject.validate.vrbo.validator.VacationRentalValidator;
import com.ews.stguo.testproject.validate.vrbo.validator.VrboValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ews.stguo.testproject.utils.compress.CompressUtils;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ValidateTest {

    @Test
    public void test01_validate() throws Exception {
        Set<Integer> hotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(true, "E:/ews-29840/sddp-feed-control-file.csv");
        List<VrboValidator<?>> vrboValidators = new ArrayList<>();
//        vrboValidators.add(new SummaryValidator(hotelIds));
//        vrboValidators.add(new ListingsValidator(hotelIds));
//        vrboValidators.add(new LocationsValidator(hotelIds));
//        vrboValidators.add(new DescriptionsValidator(hotelIds));
//        vrboValidators.add(new AmenitiesValidator(hotelIds));
//        vrboValidators.add(new ImagesValidator(hotelIds));
//        vrboValidators.add(new PoliciesValidator(hotelIds));
//        vrboValidators.add(new GuestReviewsValidator(hotelIds));
        vrboValidators.add(new VacationRentalValidator(hotelIds));

        for (VrboValidator<?> vrboValidator : vrboValidators) {
            Assert.assertNotNull(vrboValidator);
            vrboValidator.start();
        }
    }

    // Done
    @Test
    public void test02_mapping() throws Exception {
        System.out.println("Loading control and mapping file...");
        Set<Integer> hotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(true, "E:/ews-29840/sddp-feed-control-file.csv");
        List<String> mappings = ControlFileRWUtils.loadHotelIdStrByPaths(true, "E:/ews-29840/vrboIdMapping.csv");
        System.out.println("Loaded.");
        Set<Integer> existsHotelIds = new HashSet<>();
        for (String mapping : mappings) {
            existsHotelIds.add(Integer.parseInt(mapping.split(",")[0]));
        }
        System.out.println("Before filter HotelIdsSize: " + hotelIds.size());
        System.out.println("ExistsHotelIdsSize: " + existsHotelIds.size());
        hotelIds.removeAll(existsHotelIds);
        System.out.println("After filter HotelIdsSize: " + hotelIds.size());
        VrboIdMappingGenerator vrboIdMappingGenerator = new VrboIdMappingGenerator();
        Assert.assertNotNull(vrboIdMappingGenerator);
        vrboIdMappingGenerator.generate(new ArrayList<>(hotelIds));
    }

    @Test
    public void test03_loadData() throws Exception {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxWaitMillis(10000);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379, 10000);
        Assert.assertNotNull(poolConfig);
        try (Jedis jedis = jedisPool.getResource()) {
            for (int i = 1; i <= 11; i++) {
                System.out.println("Loading is start(" + i + ")..");
                try (BufferedReader br = RWFileUtils.getReader("", "E:/ews-29840/edn-data-" + i + ".jsonl")) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] columns = line.split("&&&&&&&&&&&&&");
                        byte[] byteKey = columns[0].getBytes(StandardCharsets.UTF_8);
                        byte[] byteValue = CompressUtils.compress(columns[1].getBytes(StandardCharsets.UTF_8));
                        jedis.set(byteKey, byteValue);
                    }
                }
            }
        }
        System.out.println("Done");
    }

    @Test
    public void test_getdata() {
        String id = "64987936";
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxWaitMillis(10000);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379, 10000);
        Assert.assertNotNull(jedisPool);
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] byteKey = id.getBytes(StandardCharsets.UTF_8);
            byte[] byteValue = jedis.get(byteKey);
            System.out.println(new String(CompressUtils.decompress(byteValue), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_compare() throws Exception {
        String urlTemplate = "https://www.vrbo.com/%s?adultsCount=2&arrival=2021-10-02&departure=2021-10-06&unitId=%s";
        List<String> stringList = ControlFileRWUtils.loadHotelIdStrByPaths(true, "E:/ews-29840/vrboIdMapping4.csv");
        Assert.assertNotNull(stringList);
        Set<Integer> ids = stringList.stream().map(l -> l.split(",")[0]).map(Integer::parseInt).collect(Collectors.toSet());
        Map<String, Pair<String, String>> vrboWebLikeMapping = stringList.stream().map(s -> s.split(","))
                .collect(Collectors.toMap(ss -> ss[0], ss -> {
                    String vrboPropertyId = ss[2];
                    String[] columns = vrboPropertyId.split("\\.");
                    String listingId = columns[1];
                    String listingNum = columns[2];
                    String namespace = ss[3];
                    if ("trips".equals(namespace)) {
                        listingId = listingId + "ha";
                    } else if ("abritel".equals(namespace)) {
                        listingId = listingId + "a";
                    }
                    return Pair.of(vrboPropertyId, String.format(urlTemplate, listingId, listingNum));
                }));
        List<String> stringList1 = ControlFileRWUtils.loadHotelIdStrByPaths(true, "E:/ews-29840/InstantBookMapping.csv");
        Map<String, String> instantBookMapping = stringList1.stream().map(s -> s.split(","))
                .collect(Collectors.toMap(ss -> ss[0], ss -> ss[2]));
        List<VrboComparator<?>> comparators = new ArrayList<>();
        comparators.add(new SummaryComparator(ids, vrboWebLikeMapping, instantBookMapping));
//        comparators.add(new ListingsComparator(ids, vrboWebLikeMapping, instantBookMapping));
//        comparators.add(new LocationsComparator(ids));
//        comparators.add(new DescriptionsComparator(ids, vrboWebLikeMapping, instantBookMapping));
//        comparators.add(new AmenitiesComparator(ids, vrboWebLikeMapping, instantBookMapping));
//        comparators.add(new ImagesComparator(ids, vrboWebLikeMapping, instantBookMapping));
//        comparators.add(new PoliciesComparator(ids, vrboWebLikeMapping, instantBookMapping));
//        comparators.add(new GuestReviewsComparator(ids, vrboWebLikeMapping, instantBookMapping));
//        comparators.add(new VacationRentalComparator(ids, vrboWebLikeMapping, instantBookMapping));
        for (VrboComparator<?> comparator : comparators) {
            comparator.compare();
        }
    }

}

