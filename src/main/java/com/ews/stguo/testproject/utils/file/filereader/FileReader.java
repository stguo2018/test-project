package com.ews.stguo.testproject.utils.file.filereader;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public interface FileReader extends Closeable {

    void init() throws IOException;

    String readLine() throws IOException;

    String readAllLine() throws IOException;

    byte[] readData(byte[] bytes, int offset, int length) throws IOException;

    byte[] readAllData() throws IOException;

    String readLineAsJson();

    String readAllLineAsJson();

    String getReaderName();

    FileReaderType getFileReaderType();

    String getFilePath();

    List<FileReader> getChildFileReaders();

    Integer getLevel();

}
