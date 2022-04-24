package com.ews.stguo.testproject.utils.file.filereader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public abstract class AbstractFileReader implements FileReader {

    private String readerName;
    private String filePath;
    private FileReaderType fileReaderType;
    private List<FileReader> childFileReaders = new ArrayList<>();
    private Integer level = 0;

    @Override
    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    @Override
    public FileReaderType getFileReaderType() {
        return this.fileReaderType;
    }

    public void setFileReaderType(FileReaderType fileReaderType) {
        this.fileReaderType = fileReaderType;
    }

    @Override
    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<FileReader> getChildFileReaders() {
        return this.childFileReaders;
    }

    public void setChildFileReaders(List<FileReader> childFileReaders) {
        this.childFileReaders = childFileReaders;
    }

    @Override
    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

}
