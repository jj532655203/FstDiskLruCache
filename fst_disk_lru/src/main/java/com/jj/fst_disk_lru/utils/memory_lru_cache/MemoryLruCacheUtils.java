package com.jj.fst_disk_lru.utils.memory_lru_cache;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.google.gson.Gson;


public class MemoryLruCacheUtils {

    private static final String TAG = "MemoryLruCacheUtils";
    private static LruCache<String, Object> lruCache;
    private static MemoryLruCacheUtils instance;
    private static Gson gson;
    private static int MAX_SIZE = getMemoryCacheSize();

    public static void setMaxSize(int maxSize) {
        if (maxSize < 1024 * 1024 * 50) return;
        MAX_SIZE = maxSize;
    }

    private MemoryLruCacheUtils() {
        gson = new Gson();
        try {
            lruCache = new LruCache<String, Object>(MAX_SIZE) {
                protected int sizeOf(@NonNull String paramString, @NonNull Object serializable) {

                    return gson.toJson(serializable).length() / 1024;
                }
            };
        } catch (Exception e) {
            Log.e(TAG, "getInstance new LruCache 异常" + Log.getStackTraceString(e));
        }
    }

    public static MemoryLruCacheUtils getInstance() {
        if (instance == null) {
            synchronized (MemoryLruCacheUtils.class) {
                if (instance == null) instance = new MemoryLruCacheUtils();
            }
        }
        return instance;
    }

    private static int getMemoryCacheSize() {
        int memorySize = (int) (Runtime.getRuntime().maxMemory() / 1024L / 16L);
        Log.d(TAG, "getMemoryCacheSize memorySize=" + memorySize);
        return memorySize;
    }

    public void put(String key, Object serializable) {
        if (TextUtils.isEmpty(key)) return;

        if (serializable == null) {
            lruCache.remove(key);
            return;
        }

        lruCache.put(key, serializable);
    }

    public Object get(String key) {
        if (TextUtils.isEmpty(key)) return null;
        return lruCache.get(key);
    }

    public void remove(String key) {
        if (TextUtils.isEmpty(key)) return;
        lruCache.remove(key);
    }
}
