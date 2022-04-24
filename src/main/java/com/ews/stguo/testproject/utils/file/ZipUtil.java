package com.ews.stguo.testproject.utils.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.*;
import java.util.Enumeration;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@Slf4j
public class ZipUtil {

    private ZipUtil() {
    }

    public static void unZipFile(String zipFilePath) throws IOException {
        unZipFile(zipFilePath, null, false);
    }

    public static void unZipFile(String zipFilePath, String fileSavePath, Boolean isDelete) throws IOException {

        String defaultFileSavePath = zipFilePath.substring(0, zipFilePath.lastIndexOf('.')) + File.separator;
        String saveDirectoryPath = fileSavePath == null || fileSavePath.isEmpty() ? defaultFileSavePath : fileSavePath;
        createDirectory(saveDirectoryPath);

        File zipFile = createFile(new File(zipFilePath));

        try (ZipFile zf = new ZipFile(zipFile, "UTF-8")) {
            Enumeration<ZipEntry> entries = zf.getEntries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String fileName = zipEntry.getName();
                boolean isMkdir = fileName.lastIndexOf('/') != -1;
                fileName = saveDirectoryPath + fileName;
                if (zipEntry.isDirectory() && new File(fileName).mkdirs()) {
                    continue;
                }
                File file = new File(fileName);
                if (isMkdir) {
                    validationDirectory(fileName.substring(0, fileName.lastIndexOf('/') + 1));
                }
                if (!file.createNewFile()) {
                    IOException e = new IOException(String.format("%s create failed.", file.getName()));
                    log.error(String.format("%s create failed.", file.getName()), e);
                    throw e;
                }
                try (InputStream in = zf.getInputStream(zipEntry);
                     BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                    byte[] bytes = new byte[1024];
                    int count;
                    while ((count = in.read(bytes)) != -1) {
                        bos.write(bytes, 0, count);
                    }
                    bos.flush();
                }
            }
        }

        if (isDelete != null && isDelete) {
            deleteFile(zipFile);
        }

    }

    private static void createDirectory(String directoryPath) throws IOException {
        validationDirectory(directoryPath);
        clearDirectory(directoryPath);
        File dFile = new File(directoryPath);
        if (!dFile.exists() && !dFile.mkdirs()) {
            IOException e = new IOException(String.format("%s mkdirs failed.", directoryPath));
            log.error(String.format("%s mkdirs failed.", directoryPath), e);
            throw e;
        }
    }

    private static void clearDirectory(String dicrectoryPath) throws IOException {
        File dFile = new File(dicrectoryPath);
        if (dFile.exists() && dFile.isDirectory()) {
            File[] delFiles = dFile.listFiles();
            if (delFiles != null) {
                for (File delFile : delFiles) {
                    if (delFile.isDirectory()) {
                        clearDirectory(delFile.getAbsolutePath());
                    } else {
                        deleteFile(delFile);
                    }
                }
            }
            deleteFile(dFile);
        }
    }

    private static void deleteFile(File file) throws IOException {
        if (!file.delete()) {
            IOException e = new IOException(String.format("%s delete failed.", file.getAbsolutePath()));
            log.error(String.format("%s delete failed.", file.getAbsolutePath()), e);
            throw e;
        }
    }

    private static void validationDirectory(String directoryPath) throws IOException {
        if (directoryPath.charAt(directoryPath.length() - 1) != '/' && directoryPath.charAt(directoryPath.length() - 1) != '\\') {
            IOException e = new IOException(String.format("%s is not directory.", directoryPath));
            log.error(String.format("%s is not directory.", directoryPath), e);
            throw e;
        }
    }

    private static File createFile(File file) throws IOException {
        if (!file.exists() && file.length() <= 0) {
            IOException e = new IOException(String.format("not find %s!", file.getAbsolutePath()));
            log.error(String.format("not find %s!", file.getAbsolutePath()), e);
            throw e;
        }

        return file;
    }

}
