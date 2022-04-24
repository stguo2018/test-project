package com.ews.stguo.testproject.test.controlfile;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ControlFileCompare {

    @Test
    public void test01() throws Exception {
        String[] path = {
                "test/ControlFile-EDE-GOOGLE-BEX_new.tsv",
                "test/ControlFile-EDE-GOOGLE-BEX_old.tsv",
                "ControlFile-GOOGLE-VR-ECOM.tsv"
        };
        Set<Integer> newHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(path[0]);
        Set<Integer> oldHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(path[1]);
        Set<Integer> ecomVrHotelIds = ControlFileRWUtils.loadHotelIdsByPathsAsSet(path[2]);
        Assert.assertNotNull(newHotelIds);
        newHotelIds.removeAll(ecomVrHotelIds);
        oldHotelIds.removeAll(ecomVrHotelIds);
        System.out.println("new: " + newHotelIds.size());
        System.out.println("old: " + oldHotelIds.size());
        Collection<Integer> collection1 = CollectionUtils.removeAll(oldHotelIds, newHotelIds);
        Collection<Integer> collection2 = CollectionUtils.removeAll(newHotelIds, oldHotelIds);
        System.out.println("remove: " + collection1.size());
        System.out.println("add: " + collection2.size());
        try (BufferedWriter removeBw = RWFileUtils.getWriter("test/removed hotel id list.txt");
             BufferedWriter addBw = RWFileUtils.getWriter("test/add hotel id list.txt")) {
            for (Integer hotelId : new ArrayList<>(collection1)) {
                removeBw.write(String.valueOf(hotelId));
                removeBw.newLine();
            }
            removeBw.flush();
            for (Integer hotelId : new ArrayList<>(collection2)) {
                addBw.write(String.valueOf(hotelId));
                addBw.newLine();
            }
            addBw.flush();
        }
    }

    @Test
    public void test02() throws Exception {
        String[] paths = {
                "control-file (1).csv", "control-file-ewsapi (3).csv", "ews-hotel-static-ecomid-hcomid-mapping_1.csv"
        };
        // 564, 140
        // 625, 179

        //933948
        //874099

        // 933948
        // 929083

        List<String> mappingStrs = ControlFileRWUtils.loadHotelIdStrByPaths(paths[2]);
        Set<Integer> ecomHotelidsInMapping = new HashSet<>();
        for (String mappingStr : mappingStrs) {
            String[] columns = mappingStr.split(",");
            try {
                ecomHotelidsInMapping.add(Integer.parseInt(columns[0]));
            } catch (NumberFormatException e) {
                System.out.println("emmmmmm.");
            }
        }
        List<Integer> hotelIds1 = getHotelIds(ControlFileRWUtils.loadHotelIdStrByPaths(paths[0]), true, "24", ecomHotelidsInMapping);
        List<Integer> hotelIds2 = getHotelIds(ControlFileRWUtils.loadHotelIdStrByPaths(paths[1]), true, "24", ecomHotelidsInMapping);
        System.out.println(hotelIds1.size());
        System.out.println(hotelIds2.size());
        System.out.println(CollectionUtils.removeAll(hotelIds1, new HashSet(hotelIds2)).size());
        System.out.println(CollectionUtils.removeAll(hotelIds2, new HashSet(hotelIds1)).size());
    }

    private List<Integer> getHotelIds(List<String> hotelIdstr, boolean bookable, String tspIds, Set<Integer> ecomHotelIdsInMapping) {
        List<Integer> availableHotelIds = new ArrayList<>();
        for (String str : hotelIdstr) {
            String[] columns = str.split(",");
            if (columns.length == 3) {
                Integer id = Integer.parseInt(columns[0]);
                boolean bkb = Integer.parseInt(columns[1]) == 1;
                String[] tids = columns[2].split(";");
                boolean flag = false;
                if (StringUtils.isNotBlank(tspIds)) {
                    for (String tid : tids) {
                        if (tspIds.contains(tid)) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (bkb == bookable && (StringUtils.isBlank(tspIds) || flag) && ecomHotelIdsInMapping.contains(id)) {
                    availableHotelIds.add(id);
                }
            }
        }
        return availableHotelIds;
    }

}
