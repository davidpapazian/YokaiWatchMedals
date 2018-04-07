package com.davidpapazian.yokaiwatchmedals.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.davidpapazian.yokaiwatchmedals.YWMApplication;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by David on 26/11/2017.
 */
public class DiskCacheHandler {

    public final static String CACHE_DIR = YWMApplication.CACHE_DIR;

    public static void addBitmapToDiskCache(String folder, Bitmap b, String fileName) {

        File file = new File(CACHE_DIR + "/" + folder, fileName);
        try {
            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        }
        catch (Exception e) {e.printStackTrace();}

    }

    public static Bitmap getBitmapFromDiskCache(String folder, String fileName) {
        Bitmap b = null;
        String path = CACHE_DIR + "/" + folder + "/" + fileName;
        File file = new File(path);
        if (file.exists()) {b = BitmapFactory.decodeFile(path);}
        return b;
    }

}
