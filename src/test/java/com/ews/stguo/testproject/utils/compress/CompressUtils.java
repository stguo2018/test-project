package com.ews.stguo.testproject.utils.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressUtils {

    private CompressUtils() {}

    public static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            deflater.finish();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            deflater.end();
            return outputStream.toByteArray();
        }
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            inflater.end();
            return outputStream.toByteArray();
        }
    }

}
