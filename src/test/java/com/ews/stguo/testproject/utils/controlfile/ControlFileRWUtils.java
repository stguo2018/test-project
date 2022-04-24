package com.ews.stguo.testproject.utils.controlfile;

import com.ews.stguo.testproject.utils.file.RWFileUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ControlFileRWUtils {

    private ControlFileRWUtils() {

    }

    public static Set<Integer> loadHotelIdsByPathsAsSet(boolean useAbsolutePath, String... paths) throws Exception {
        return new HashSet<>(loadHotelIdsByPaths(useAbsolutePath, paths));
    }

    public static Set<Integer> loadHotelIdsByPathsAsSet(String... paths) throws Exception {
        return new HashSet<>(loadHotelIdsByPaths(false, paths));
    }

    public static List<Integer> loadHotelIdsByPaths(boolean useAbsolutePath, String... paths) throws Exception {
        List<Integer> hotelIds = new ArrayList<>();
        if (paths == null || paths.length == 0) {
            return hotelIds;
        }
        for (String path : paths) {
            try (BufferedReader br = useAbsolutePath ? RWFileUtils.getReader("", path) : RWFileUtils.getReader(path)) {
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        hotelIds.add(Integer.parseInt(line.trim().split(",")[0]));
                    } catch (NumberFormatException e) {

                    }
                }
            }
        }
        return hotelIds;
    }

    public static List<String> loadHotelIdStrByPaths(String... paths) throws Exception {
        return loadHotelIdStrByPaths(false, paths);
    }

    public static List<String> loadHotelIdStrByPaths(boolean useAbsolutePath, String... paths) throws Exception {
        List<String> hotelIds = new ArrayList<>();
        if (paths == null || paths.length == 0) {
            return hotelIds;
        }
        for (String path : paths) {
            try (BufferedReader br = useAbsolutePath ? RWFileUtils.getReader("", path) : RWFileUtils.getReader(path)) {
                String line;
                while ((line = br.readLine()) != null) {
                    hotelIds.add(line.trim());
                }
            }
        }
        return hotelIds;
    }

}
