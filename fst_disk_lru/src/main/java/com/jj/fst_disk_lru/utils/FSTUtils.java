package com.jj.fst_disk_lru.utils;

import android.util.Log;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FSTUtils {
    private static final String TAG = "FSTUitls";

    private static FSTUtils instance;

    // ! reuse this Object, it caches metadata. Performance degrades massively
// if you create a new Configuration Object with each serialization !
    private final FSTConfiguration confInstance;

    public static FSTUtils getInstance() {
        if (instance == null) {
            synchronized (FSTUtils.class) {
                if (instance == null) {
                    instance = new FSTUtils();
                }
            }
        }
        return instance;
    }

    private FSTUtils() {
        confInstance = FSTConfiguration.createDefaultConfiguration();
    }

    /**
     * 反序列化
     */
    public <T> T readObjectFromStream(InputStream stream, Class<T> clazz) throws IOException {
        if (stream == null) {
            Log.e(TAG, "readObjectFromStream stream 为空");
            return null;
        }

        Log.d(TAG, "readObjectFromStream");

        FSTObjectInput in = confInstance.getObjectInput(stream);
        T result = null;
        try {
            result = (T) in.readObject(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

// DON'T: in.close(); here prevents reuse and will result in an exception .close();
        stream.close();
        return result;
    }

    /**
     * 序列化
     */
    public <T> void writeObject2Stream(OutputStream stream, Object object, Class<T> clazz) throws IOException {
        if (!object.getClass().isAssignableFrom(clazz)) {
            Log.e(TAG, "writeObject2Stream object 非法!");
            return;
        }

        Log.d(TAG, "writeObject2Stream");

        FSTObjectOutput out = confInstance.getObjectOutput(stream);
        out.writeObject(object, clazz);
// DON'T out.close() when using factory method;
        out.flush();
        stream.close();
    }

}
