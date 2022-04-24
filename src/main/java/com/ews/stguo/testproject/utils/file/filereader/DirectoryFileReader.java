package com.ews.stguo.testproject.utils.file.filereader;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@Slf4j
public class DirectoryFileReader extends AbstractFileReader {

    @Override
    public void init() throws IOException {
        log.info("Directory file reader initialization.");
        File directory = new File(getFilePath());
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String path = file.isDirectory() ? file.getAbsolutePath() + File.separator : file.getAbsolutePath();
                FileReader fileReader = new FileReaderBuilder(path).buildChildFileReader(this);
                getChildFileReaders().add(fileReader);
            }
        }
    }

    @Override
    public String readLine() throws IOException {
        return null;
    }

    @Override
    public String readAllLine() throws IOException {
        return null;
    }

    @Override
    public byte[] readData(byte[] bytes, int offset, int length) throws IOException {
        return new byte[0];
    }

    @Override
    public byte[] readAllData() throws IOException {
        return new byte[0];
    }

    @Override
    public String readLineAsJson() {
        return null;
    }

    @Override
    public String readAllLineAsJson() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
