package com.ews.stguo.testproject.utils.file.filereader;

import com.ews.stguo.testproject.utils.file.ZipUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@Slf4j
public class ZipFileReader extends AbstractFileReader {

    @Override
    public void init() throws IOException {
        log.info("Zip file reader initialization.");
        // un zip.
        try {
            ZipUtil.unZipFile(getFilePath());
            // build file reader.
            FileReader fileReader = new FileReaderBuilder(getFilePath().substring(0, getFilePath().lastIndexOf('.')) + File.separator)
                    .buildChildFileReader(this);
            getChildFileReaders().add(fileReader);
        } catch (IOException e) {
            log.error("File with file type ZIP initialization failed.", e);
            throw e;
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
