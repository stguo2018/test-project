package com.ews.stguo.testproject.utils.file.filereader;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@Slf4j
@Data
public class FileReaderBuilder {

    private String readerName;
    private String filePath;
    private FileReaderType fileReaderType;
    private AbstractFileReader fileReader;

    public FileReaderBuilder(String filePath) {
        this.filePath = filePath;
        this.fileReaderType = getFileReaderType(filePath);
        initFileResultSet();
    }

    public FileReaderBuilder(String readerName, String filePath) {
        this.readerName = readerName;
        this.filePath = filePath;
        this.fileReaderType = getFileReaderType(filePath);
        initFileResultSet();
    }

    private FileReaderType getFileReaderType(String filePath) {
        FileReaderType type;
        if (filePath.charAt(filePath.length() - 1) == '/' || filePath.charAt(filePath.length() - 1) == '\\') {
            type = FileReaderType.DIRECTORY;
        } else {
            String extensionName = filePath.substring(filePath.lastIndexOf('.')).toLowerCase();
            switch (extensionName) {
                case ".zip":
                    type = FileReaderType.ZIP;
                    break;
                case ".gz":
                    type = FileReaderType.GZ;
                    break;
                default:
                    type = FileReaderType.NORMAL;
                    break;
            }
        }
        return type;
    }

    private void initFileResultSet() {
        switch (this.fileReaderType) {
            case DIRECTORY:
                this.fileReader = new DirectoryFileReader();
                break;
            case ZIP:
                this.fileReader = new ZipFileReader();
                break;
            case GZ:
                this.fileReader = new GzFileReader();
                break;
            default:
                this.fileReader = new NormalFileReader();
                break;
        }
        this.fileReader.setReaderName(readerName == null ? filePath : readerName);
        this.fileReader.setFilePath(filePath);
        this.fileReader.setFileReaderType(fileReaderType);
    }

    public FileReader build() throws IOException {
        this.fileReader.init();
        return this.fileReader;
    }

    public FileReader buildChildFileReader(FileReader fileReader) throws IOException {
        this.fileReader.setLevel(fileReader.getLevel() + 1);
        this.fileReader.init();
        return this.fileReader;
    }

}
