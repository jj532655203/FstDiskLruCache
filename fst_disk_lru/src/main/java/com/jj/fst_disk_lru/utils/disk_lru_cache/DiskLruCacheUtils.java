package com.jj.fst_disk_lru.utils.disk_lru_cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.Utils;
import com.blankj.utilcode.util.ZipUtils;
import com.jj.fst_disk_lru.utils.FSTUtils;
import com.jj.fst_disk_lru.utils.IOUtils;
import com.jj.fst_disk_lru.utils.Md5Utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

;

/**
 * Jay
 * lru本地缓存
 */
public class DiskLruCacheUtils {

    private static final String TAG = "DiskLruCacheUtils";
    private static DiskLruCacheUtils instance;
    private DiskLruCache mDiskLruCache;

    private static long MAX_SIZE = 1024 * 1024 * 1024;

    public static void setMaxSize(int maxSize) {
        if (maxSize < 1024 * 1024 * 50) return;
        MAX_SIZE = maxSize;
    }

    private DiskLruCacheUtils() {
        try {
            mDiskLruCache = DiskLruCache.open(getDiskCacheDir(), 1, 1, MAX_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getDiskCacheDir() {
        String diskLruDirPath = Utils.getApp().getFilesDir().getAbsolutePath() + File.separator + "disk_lru_cache";
        File diskDir = new File(diskLruDirPath);
        if (!diskDir.exists()) {
            diskDir.mkdirs();
        }
        return diskDir;
    }

    public static DiskLruCacheUtils getInstance() {
        if (instance == null) {
            synchronized (DiskLruCacheUtils.class) {
                if (instance == null) {
                    instance = new DiskLruCacheUtils();
                }
            }
        }
        return instance;
    }

    /**
     * this function will create bitmap ,don't call concurrently!
     */
    public Bitmap getBitmap(String ossImgPath) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.e(TAG, "不能在主线程操作DiskLruCache");
            return null;
        }

        String key = Md5Utils.encode(ossImgPath);

        Bitmap bitmap = null;
        FileInputStream inputStream = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                inputStream = (FileInputStream) snapshot.getInputStream(0);
                FileDescriptor fd = inputStream.getFD();
                bitmap = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.safeClose(inputStream);
        }

        return bitmap;
    }

    /**
     * this function will create bitmap ,don't call concurrently!
     */
    public void put(String ossImgPath, Bitmap bitmap) {

        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.e(TAG, "不能在主线程操作DiskLruCache");
            return;
        }

        String key = Md5Utils.encode(ossImgPath);

        Bitmap ossBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        OutputStream outputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            DiskLruCache.Editor edit = mDiskLruCache.edit(key);
            if (edit != null) {
                outputStream = edit.newOutputStream(0);
                baos = new ByteArrayOutputStream();
                ossBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                outputStream.write(baos.toByteArray());
                outputStream.flush();
                edit.commit();
                mDiskLruCache.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.safeClose(outputStream);
            IOUtils.safeClose(baos);
        }
    }

    /**
     * 直接将oss的输入流获取bitmap,写到diskLru
     * this function will create bitmap ,don't call concurrently!
     */
    public void put(String ossImgPath, InputStream inputStream) {

        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.e(TAG, "不能在主线程操作DiskLruCache");
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (bitmap == null || bitmap.isRecycled()) return;

        String key = Md5Utils.encode(ossImgPath);

        OutputStream outputStream = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

            DiskLruCache.Editor edit = mDiskLruCache.edit(key);
            if (edit != null) {
                outputStream = edit.newOutputStream(0);
                outputStream.write(baos.toByteArray());
                baos.flush();
                outputStream.flush();
                edit.commit();
                mDiskLruCache.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            IOUtils.safeClose(outputStream);
            IOUtils.safeClose(inputStream);
        }
    }

    public boolean contains(String ossUrl) {
        Log.d(TAG, "contains ossUrl=" + ossUrl);

        String key = Md5Utils.encode(ossUrl);

        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskLruCache.get(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean contain = snapshot != null;
        if (snapshot != null) {
            snapshot.close();
        }
        return contain;
    }

    public <T> T getObject(String ossImgPath, Class<T> clazz) {

        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.e(TAG, "不能在主线程操作DiskLruCache");
            return null;
        }

        String key = Md5Utils.encode(ossImgPath);

        T result = null;
        FileInputStream inputStream = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                inputStream = (FileInputStream) snapshot.getInputStream(0);
//                FileDescriptor fd = inputStream.getFD();
//                FileReader fileReader = new FileReader(fd);
//                BufferedReader bufferedReader = new BufferedReader(fileReader);
//                drawPathJson = bufferedReader.readLine();
                result = FSTUtils.getInstance().readObjectFromStream(inputStream, clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.safeClose(inputStream);
        }

        return result;
    }

    /**
     * @param possibles 可不填;可以加速序列化2倍!object的类名,如果是集合则把集合的类名也传进来
     */
    public void put(String ossImgPath, Object object, Class... possibles) {

        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.e(TAG, "不能在主线程操作DiskLruCache");
            return;
        }

        String key = Md5Utils.encode(ossImgPath);

        OutputStream outputStream = null;
        try {
            DiskLruCache.Editor edit = mDiskLruCache.edit(key);
            if (edit != null) {
                outputStream = edit.newOutputStream(0);
//                outputStream.write(object.getBytes());
//                outputStream.flush();
                FSTUtils.getInstance().writeObject2Stream(outputStream, object, possibles);
                edit.commit();
                mDiskLruCache.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.safeClose(outputStream);
        }
    }

    public void remove(String ossImgPath) {

        String key = Md5Utils.encode(ossImgPath);

        if (TextUtils.isEmpty(key)) return;
        try {
            mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将zipFile文件解压,并将解压文件都磁盘缓存
     */
    public synchronized boolean diskCacheAllFilesInZip(String zipFileUrl, InputStream inputStream) throws RuntimeException {
        Log.d(TAG, "diskCacheBookZip zipFileUrl=" + zipFileUrl);

        int lastIndexOfl = zipFileUrl.lastIndexOf("/");
        if (lastIndexOfl <= 0) {
            Log.e(TAG, "ZipUrl 异常");
            return false;
        }

        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new RuntimeException("不能在主线程执行IO操作");
        }

        List<File> unzipFileList;
        String destDirPath = getDiskCacheDir().getAbsolutePath();
        try {
            //缓存bookZip包,耗时所在eg:<<英语测试卷>>40s
            DiskLruCacheUtils.getInstance().writeFile(zipFileUrl, inputStream);
            Log.d(TAG, "diskCacheAllFilesInZip Succeed ");

            //将bookZip包 解压到 Constants.DISK_LRU_CACHE_DIR
            String zipFileUrlKey = Md5Utils.encode(zipFileUrl);
            String zipFilePath = destDirPath + File.separator + zipFileUrlKey + ".0";
            unzipFileList = ZipUtils.unzipFile(zipFilePath, destDirPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //以下都不耗时,如不失败,很快就成功了
        Log.d(TAG, "diskCacheBookZip unzipFile Succeed ");

        if (ObjectUtils.isEmpty(unzipFileList)) {
            Log.w(TAG, "Zip包内无文件");
            return true;
        }

        //将解压出来的文件重命名成 "key".0格式
        String zipFileOssDir = zipFileUrl.substring(0, lastIndexOfl);
        Log.d(TAG, "lastIndexOfl=" + lastIndexOfl + "? zipFileOssDir=" + zipFileOssDir);

        File journalFile = new File(destDirPath, "journal");
        Writer journalWriter = null;
        try {

            journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(journalFile, true), Charset.forName("US-ASCII")));
            for (File unzipFile : unzipFileList) {
                String unzipFileName = unzipFile.getName();
                Log.d(TAG, "unzipFileName=" + unzipFileName);
                String encodedKey = Md5Utils.encode(zipFileOssDir + "/" + unzipFileName);
                File destFile = new File(destDirPath + "/" + encodedKey + ".0");
                boolean renameTo = unzipFile.renameTo(destFile);
                Log.d(TAG, "encodedKey=" + encodedKey + "? renameTo=" + renameTo);

                //写journal
                journalWriter.write("DIRTY" + ' ' + encodedKey + '\n');
                journalWriter.flush();
                File cleanFile = new File(destDirPath, encodedKey + "." + 0);
                journalWriter.write("CLEAN" + ' ' + encodedKey + ' ' + cleanFile.length() + '\n');
                journalWriter.flush();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "DiskLruCache尚未初始化 call getInstance() first!");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (journalWriter != null) {
                try {
                    journalWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //手动关闭,等下次重新实例化,以上变更生效
        DiskLruCacheUtils.getInstance().close();

        return true;
    }

    private void writeFile(String url, InputStream inputStream) throws IOException {
        Log.d(TAG, "writeFile url=" + url);

        String key = Md5Utils.encode(url);

        OutputStream outputStream = null;
        try {

            DiskLruCache.Editor edit = mDiskLruCache.edit(key);
            if (edit != null) {
                outputStream = edit.newOutputStream(0);
                byte[] bytes = new byte[1024];
                int len;
                while ((len = inputStream.read(bytes)) > 0) {
                    outputStream.write(bytes, 0, len);
                }
                outputStream.flush();
                edit.commit();
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            IOUtils.safeClose(outputStream);
            IOUtils.safeClose(inputStream);
        }

    }

    public void close() {
        try {
            mDiskLruCache.close();
            instance = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
