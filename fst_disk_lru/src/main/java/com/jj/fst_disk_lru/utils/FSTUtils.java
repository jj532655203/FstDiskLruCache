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
     *
     * @param stream    该输出流携带的数据必须是对象转byte数组,不能是jsonString
     * @param possibles 可不填;可以加速序列化2倍!object的类名,如果是集合则把集合的类名也传进来
     */
    public void writeObject2Stream(OutputStream stream, Object object, Class... possibles) throws IOException {
        Log.d(TAG, "writeObject2Stream");

        FSTObjectOutput out = confInstance.getObjectOutput(stream);
        out.writeObject(object, possibles);
// DON'T out.close() when using factory method;
        out.flush();
        stream.close();
    }

}
