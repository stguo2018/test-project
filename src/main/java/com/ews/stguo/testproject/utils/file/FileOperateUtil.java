package com.ews.stguo.testproject.utils.file;

import com.ews.stguo.testproject.utils.file.filereader.FileReader;
import com.ews.stguo.testproject.utils.file.filereader.FileReaderBuilder;
import com.ews.stguo.testproject.utils.file.filereader.FileReaderType;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class FileOperateUtil {

    private FileOperateUtil() {
    }

    public static FileReader readFileWithSingleton(String filePath) throws IOException {
        FileReader fileReader = null;
        for (FileReader reader : arrangeFileReader(new FileReaderBuilder(filePath).build())) {
            if (!Objects.equals(reader.getFileReaderType(), FileReaderType.ZIP) && !Objects.equals(reader.getFileReaderType(), FileReaderType.DIRECTORY)) {
                fileReader = reader;
                break;
            }
        }
        return fileReader;
    }

    public static List<FileReader> readFile(String filePath) throws IOException {
        return arrangeFileReader(new FileReaderBuilder(filePath).build());
    }

    private static List<FileReader> arrangeFileReader(FileReader fileReader) {
        List<FileReader> arrangeFileReaders = new ArrayList<>();
        arrangeFileReader(fileReader, arrangeFileReaders);
        return arrangeFileReaders;
    }

    private static void arrangeFileReader(FileReader fileReader, List<FileReader> arrangeFileReaders) {
        if (!CollectionUtils.isEmpty(fileReader.getChildFileReaders())) {
            for (FileReader fr : fileReader.getChildFileReaders()) {
                arrangeFileReader(fr, arrangeFileReaders);
            }
        } else if (!Objects.equals(fileReader.getFileReaderType(), FileReaderType.DIRECTORY)) {
            arrangeFileReaders.add(fileReader);
        }
    }

//    private static <T> FileReader<T> transformFileResultSet(FileReader fileReader, String className) {
//        FileResultSetAfterAdvice fileResultSetAfterAdvice = new FileResultSetAfterAdvice(className);
//        ProxyFactory factory = new ProxyFactory();
//        factory.setTarget(fileReader);
//        factory.addAdvice(fileResultSetAfterAdvice);
//        return (FileReader<T>) factory.getProxy();
//    }

}
