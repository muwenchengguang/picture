/**
 * 
 * @author PengPeng
 * @date 2016-08-08
 * 
 */

package com.peng.jpegviewer.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {
    private static final ALog.Category TAG = ALog.Category.Utils;

    public static String getExternalAppPath(String appName) {
        return Environment.getExternalStorageDirectory().getPath() + "/" + appName;
    }

    public static boolean isExist(String path) {
        if (path == null) {
            return false;
        }

        File f = new File(path);
        return f.exists();
    }

    public static File[] enumFiles(String path) {
        if (path == null) {
            return null;
        }

        File f = new File(path);
        if (!f.exists()) {
            return null;
        }

        if (f.isFile()) {
            return null;
        }
        return f.listFiles();
    }

    public static boolean isFile(String path) {
        if (path == null) {
            return false;
        }

        File f = new File(path);
        if (!f.exists()) {
            return false;
        }

        return f.isFile();
    }

    public static boolean isDirectory(String path) {
        if (path == null) {
            return false;
        }

        File f = new File(path);
        if (!f.exists()) {
            return false;
        }

        return f.isDirectory();
    }

    public static File open(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            return null;
        }
        return f;
    }

    public static boolean createFile(String path, boolean createFolder) {
        File f = new File(path);
        if (f.exists()) {
            return false;
        }
        if (createFolder) {
            return f.mkdirs();
        } else {
            try {
                return f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                ALog.e(TAG, "create file failed with:" + e);
                return false;
            }
        }
    }

    public static boolean deleteFile(String path) {
        return deleteFile(new File(path));
    }

    public static boolean deleteFile(File f) {
        if (f == null || !f.exists()) {
            return false;
        }
        if (f.isDirectory()) {
            File[] subs = f.listFiles();
            if (subs != null) {
                for (File sub : subs) {
                    deleteFile(sub);
                }
            }
        }
        boolean ret = f.delete();
        ALog.i(TAG, f.getAbsolutePath() + " delete ret = " + ret);
        return ret;
    }

    public static boolean renameFile(String path, String newName) {
        File f = new File(path);
        if (!f.exists()) {
            return false;
        }
        return f.renameTo(new File(f.getParent(), newName));
    }

    public static void save2File(File file, String dstFilename) {
        try {
            InputStream inputStream = new FileInputStream(file);
            save2File(inputStream, dstFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ALog.e(TAG, "save2File failed with:" + e);
        } catch (IOException e) {
            e.printStackTrace();
            ALog.e(TAG, "save2File failed with:" + e);
        }

    }

    public static void save2File(InputStream is, String filename) throws IOException {
        final int cacheCapacity = 1024*1024;
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        } else {
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);

        int pos = 0;
        int available = is.available();
        while (pos < available) {
            int leftCache = available - pos;
            int cacheSize = (leftCache > cacheCapacity?cacheCapacity:leftCache);
            byte[] cache = new byte[cacheSize];
            is.read(cache, 0, cacheSize);
            fos.write(cache);
            pos += cacheSize;
        }
        fos.flush();
        fos.close();
    }

    public static void save2File(byte[] data, String filename) throws IOException {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }

        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    public static byte[] openFile(String filename, int readLength) throws IOException {
        File f = new File(filename);
        if (!f.exists()) {
            return null;
        }
        IOException exception = null;
        try {
            FileInputStream fis = new FileInputStream(f);
            int sz = fis.available();
            if (sz <= 0) {
                fis.close();
                return null;
            }
            int length = (readLength < fis.available()?readLength:fis.available());
            byte[] data = new byte[length];
            fis.read(data);
            fis.close();
            return data;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            exception = e;
        }  catch (IOException e) {
            // TODO Auto-generated catch block
            exception = e;
        }
        ALog.e(TAG, "openFile failed with:" + exception);
        throw exception;
    }

    public static byte[] openFile(File f, int readLength) {
        if (f == null || !f.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(f);
            int sz = fis.available();
            if (sz <= 0) {
                fis.close();
                return null;
            }
            int length = (readLength < fis.available()?readLength:fis.available());
            byte[] data = new byte[length];
            fis.read(data);
            fis.close();
            return data;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ALog.e(TAG, "openFile failed with:" + e);
            return null;
        }  catch (IOException e) {
            // TODO Auto-generated catch block
            ALog.e(TAG, "openFile failed with:" + e);
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream openFile(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            return null;
        }
        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ALog.e(TAG, "openFile failed with:" + e);
        }
        return null;
    }

    public static byte[] retrieveFileContent(String filePathName) {
        File f = new File(filePathName);
        if (!f.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(f);
            int sz = fis.available();
            if (sz <= 0) {
                fis.close();
                return null;
            }
            int length = fis.available();
            byte[] data = new byte[length];
            fis.read(data);
            fis.close();
            return data;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            ALog.e(TAG, "retrieveFileContent failed with:" + e);
        }  catch (IOException e) {
            // TODO Auto-generated catch block
            ALog.e(TAG, "retrieveFileContent failed with:" + e);
        }
        return null;
    }

    public static boolean isAssetFileExist(Context context, String filePathName) {
        InputStream is = null;
        try {
            is = context.getAssets().open(filePathName);
            is.close();
            return true;
        } catch (IOException e) {
            ALog.e(TAG, "retrieveAssetFileContent failed with:" + e);
            return false;
        }
    }

    public static byte[] retrieveAssetFileContent(Context context, String filePathName) {
        InputStream is = null;
        try {
            is = context.getAssets().open(filePathName);
            int len = is.available();
            if (len <= 0) return null;
            byte[] data = new byte[len];
            int reads = is.read(data);
            if (reads != len) {
                return null;
            }
            is.close();
            return data;
        } catch (IOException e) {
            ALog.e(TAG, "retrieveAssetFileContent failed with:" + e);
            return null;
        }
    }

    public static InputStream openAsset(Context context, String filePathName) {
        InputStream is = null;
        try {
            return context.getAssets().open(filePathName);
        } catch (IOException e) {
            ALog.e(TAG, "retrieveAssetFileContent failed with:" + e);
            return null;
        }
    }

    public static String[] assetList(Context context, String filePathName) {
        try {
            return context.getAssets().list(filePathName);
        } catch (IOException e) {
            ALog.e(TAG, "assetList failed with:" + e);
            return null;
        }
    }

    public static boolean copyFilesUnderFolder2Dst(String srcPath, String dstPath) {
        boolean ret = true;
        if (!FileUtils.isExist(srcPath)) {
            ALog.e(TAG, "copyFilesUnderFolder2Dst failed with:" + srcPath + " doesn't exist");
            return false;
        }
        File srcF = new File(srcPath);
        if (!srcF.isDirectory()) {
            ALog.e(TAG, "copyFilesUnderFolder2Dst failed with:" + srcPath + " is not directory");
            return false;
        }
        if (!FileUtils.isExist(dstPath)) {
            ALog.e(TAG, "copyFilesUnderFolder2Dst failed with:" + dstPath + " doesn't exist");
            return false;
        }
        File[] subs = srcF.listFiles();
        for (File sub : subs) {
            String dstSubPath = dstPath + "/" + sub.getName();
            if (sub.isFile()) {
                FileUtils.save2File(sub, dstSubPath);
            } else if (sub.isDirectory()) {
                FileUtils.createFile(dstSubPath, true);
                ret = copyFilesUnderFolder2Dst(sub.getAbsolutePath(), dstSubPath);
            }
        }
        return ret;
    }

    public static File[] list(String path) {
        if (!isExist(path)) return null;
        File f = new File(path);
        return f.listFiles();
    }

    public static void unzip(String destPath, String path) {
        if (!FileUtils.isExist(path)) return;
        ZipInputStream zis;
        try {
            InputStream is = new FileInputStream(path);
            BufferedInputStream bis = new BufferedInputStream(is);
            zis = new ZipInputStream(bis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        byte[] cache = new byte[1024*10];
        try {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File file = new File(destPath + "/" + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (!parent.exists()){
                        parent.mkdirs();
                    }
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fileOut = new FileOutputStream(file);
                    int reads;
                    while ((reads = zis.read(cache)) > 0) {
                        if (reads == cache.length) {
                            fileOut.write(cache);
                        } else {
                            fileOut.write(cache, 0, reads);
                        }
                    }
                    fileOut.close();
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
