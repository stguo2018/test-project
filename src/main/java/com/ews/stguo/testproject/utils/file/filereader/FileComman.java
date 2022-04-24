package com.ews.stguo.testproject.utils.file.filereader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class FileComman {

    private FileComman() {}

    public static String bufferRead(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static byte[] dataInputStreaRead(DataInputStream dataInputStream) throws IOException {
        byte[] bytes = new byte[2];
        byte[] readBytes = new byte[1];
        long count = 0;
        while (dataInputStream.read(readBytes, 0, readBytes.length) != -1) {
            if (count == bytes.length) {
                byte[] newBytes = new byte[(bytes.length << 1)];
                System.arraycopy(bytes, 0, readBytes, 0, bytes.length);
                bytes = newBytes;
            }
            System.arraycopy(readBytes, 0, bytes, (int) count, readBytes.length);
            count ++;
        }
        return bytes;
    }

}
