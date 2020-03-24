package com.jj.fst_disk_lru.utils;

import java.io.ByteArrayOutputStream;
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

    public static byte[] readStreamAsBytesArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];

            int len;
            while ((len = in.read(buffer)) > -1) {
                output.write(buffer, 0, len);
            }

            output.flush();
            return output.toByteArray();
        }
    }

}
