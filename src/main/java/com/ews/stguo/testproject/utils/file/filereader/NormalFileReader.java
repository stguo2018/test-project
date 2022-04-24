package com.ews.stguo.testproject.utils.file.filereader;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@Slf4j
public class NormalFileReader extends AbstractFileReader {

    private BufferedReader br;
    private DataInputStream dataInputStream;

    @Override
    public void init() throws IOException {
        log.info("Normal file reader initialization.");
    }

    @Override
    public String readLine() throws IOException {
        try {
            if (br == null) {
                br = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                        new FileInputStream(getFilePath())), StandardCharsets.UTF_8));
            }
            return br.readLine();
        } catch (IOException e) {
            log.error("File with file type Normal failed to read line.", e);
            throw e;
        }
    }

    @Override
    public String readAllLine() throws IOException {
        try (BufferedReader brAll = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                new FileInputStream(getFilePath())), StandardCharsets.UTF_8))) {
            return FileComman.bufferRead(brAll);
        } catch (IOException e) {
            log.error("File with file type Normal failed to read all line.", e);
            throw e;
        }
    }

    @Override
    public byte[] readData(byte[] bytes, int offset, int length) throws IOException {
        try {
            if (dataInputStream == null) {
                dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(getFilePath())));
            }
            if (dataInputStream.read(bytes, offset, length) != -1) {
                return bytes;
            }
            return new byte[0];
        } catch (IOException e) {
            log.error("File with file type Normal failed to read data.", e);
            throw e;
        }
    }

    @Override
    public byte[] readAllData() throws IOException {
        try (DataInputStream dStream = new DataInputStream(new BufferedInputStream(new FileInputStream(getFilePath())))) {
            return FileComman.dataInputStreaRead(dStream);
        } catch (IOException e) {
            log.error("File with file type Normal failed to read all data.", e);
            throw e;
        }
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
        if (br != null) {
            br.close();
        }
        if (dataInputStream != null) {
            dataInputStream.close();
        }
    }

}
