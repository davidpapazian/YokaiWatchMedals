package com.davidpapazian.yokaiwatchmedals;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import com.davidpapazian.yokaiwatchmedals.tools.Utils;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class YWMApplication extends Application {

    public static final boolean isNougatOrLater = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    public static final boolean isMarshMallowOrLater = isNougatOrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    public static final boolean isLolliPopOrLater = isMarshMallowOrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final boolean isKitKatOrLater = isLolliPopOrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    public static final boolean isJellyBeanMR2OrLater = isKitKatOrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    public static final boolean isJellyBeanMR1OrLater = isJellyBeanMR2OrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    public static final boolean isJellyBeanOrLater = isJellyBeanMR1OrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean isICSOrLater = isJellyBeanOrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    public static final boolean isHoneycombMr2OrLater = isICSOrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    public static final boolean isHoneycombMr1OrLater = isHoneycombMr2OrLater || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    public static final boolean isHoneycombOrLater = isHoneycombMr1OrLater || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;

    public final static String APP_DIR = Environment.getExternalStorageDirectory().getPath() + "/Yo-kai Watch Medals";
    public final static String CACHE_DIR = APP_DIR + "/cache";
    public final static String COLLECTION_DIR = APP_DIR + "/collection";
    public final static String DATA_DIR = "/data/data/com.davidpapazian.yokaiwatchmedals";
    public final static String DB_DIR = DATA_DIR + "/databases";
    public final static String ROOT_URL = "http://manualis.yokaiwatchworld.net";


    private static YWMApplication instance;
    private final int maxThreads = Math.max(isJellyBeanMR1OrLater ? Runtime.getRuntime().availableProcessors() : 2, 1);

    public static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setPriority(Process.THREAD_PRIORITY_DEFAULT+Process.THREAD_PRIORITY_LESS_FAVORABLE);
            return thread;
        }
    };
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(Math.min(2, maxThreads), maxThreads, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), THREAD_FACTORY);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        instance = YWMApplication.this;

    }

    public static void runBackground(Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper())
            runnable.run();
        else
            instance.mThreadPool.execute(runnable);
    }

    public static void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper())
            runnable.run();
        else
            instance.mHandler.post(runnable);
    }

    public static YWMApplication getInstance() {
        return instance;
    }

    public void initialise() {
        createDir(APP_DIR);
        createDir(CACHE_DIR);
        createDir(COLLECTION_DIR);
        createDir(CACHE_DIR + "/medal_500");
        createDir(CACHE_DIR + "/medal_120");
        createDir(CACHE_DIR + "/yokai_500");
        createDir(CACHE_DIR + "/yokai_120");
        createDir(CACHE_DIR + "/product_500");
        createDir(CACHE_DIR + "/product_120");
    }

    public void createDir(String path) {
        File fPath = new File(path);
        if (!fPath.exists()) {
            fPath.mkdir();
        }
    }
}