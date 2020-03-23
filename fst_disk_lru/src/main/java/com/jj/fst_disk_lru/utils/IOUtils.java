package com.jj.fst_disk_lru.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {


    public static void safeClose(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public static void safeClose(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
    }
}
