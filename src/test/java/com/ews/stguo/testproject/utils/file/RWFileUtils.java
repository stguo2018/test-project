package com.ews.stguo.testproject.utils.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class RWFileUtils {

    private RWFileUtils() {

    }

    public static final String BASE_PATH = "C:/Users/stguo/Downloads/";

    public static BufferedWriter getWriter(String path) throws IOException {
        return getWriter(BASE_PATH, path);
    }

    public static BufferedWriter getWriter(String path, boolean append) throws IOException {
        return getWriter(BASE_PATH, path, append);
    }

    public static BufferedWriter getWriter(String basePath, String path) throws IOException {
        return getWriter(basePath, path, false);
    }

    public static BufferedWriter getWriter(String basePath, String path, boolean append) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(basePath + path, append), StandardCharsets.UTF_8));
    }

    public static BufferedReader getReader(String path) throws IOException {
        return getReader(BASE_PATH, path);
    }

    public static BufferedReader getReader(String basePath, String path) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(basePath + path), StandardCharsets.UTF_8));
    }

    public static File getFile(String path) {
        return getFile(BASE_PATH, path);
    }

    public static File getFile(String basePath, String path) {
        return new File(basePath + path);
    }

}
