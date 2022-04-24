package com.ews.stguo.testproject.utils.file.filereader;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public enum FileReaderType {

    NORMAL("Normal"),
    DIRECTORY("Directory"),
    ZIP("Zip"),
    GZ("Gz");

    private FileReaderType(String readerType) {
        this.readerType = readerType;
    }

    private String readerType;

    public String getReaderType() {
        return readerType;
    }

}
