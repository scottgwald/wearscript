package com.dappervision.wearscript.record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AudioMerger {
    public static boolean merge(File file1, File file2, File output) {
        byte[] file1contents, file2contents;
        try {
            file1contents = readAllBytes(file1);
            file2contents = readAllBytes(file2);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        byte[] outputContents = new byte[file1contents.length + file2contents.length - AudioRecordThread.WAV_HEADER_LENGTH];
        System.arraycopy(file1contents, 0, outputContents, 0, file1contents.length);
        System.arraycopy(file2contents, AudioRecordThread.WAV_HEADER_LENGTH,
                outputContents, file1contents.length,
                file2contents.length - AudioRecordThread.WAV_HEADER_LENGTH);
        try {
            new FileOutputStream(output).write(outputContents);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static byte[] readAllBytes(File file) throws IOException {
        RandomAccessFile f = new RandomAccessFile(file, "r");
        byte[] b = new byte[(int)f.length()];
        f.read(b);
        return b;
    }
}
