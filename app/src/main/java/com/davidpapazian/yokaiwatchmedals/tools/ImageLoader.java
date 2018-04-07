package com.davidpapazian.yokaiwatchmedals.tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageLoader {

    public static List<AsyncTask> asyncList = new ArrayList<>();
    protected static LruCache<String, Bitmap> mMemoryCache;
    protected String itemType;
    protected String defaultImageName;
    protected Bitmap mPlaceHolderBitmap;
    protected Context context;
    protected int width;
    protected int height;
    public final static String ROOT_URL = YWMApplication.ROOT_URL;

    public ImageLoader(Context context, String itemType, int width, int height) {
        this.context = context;
        this.itemType = itemType;
        this.height = height;
        this.width = width;
        this.defaultImageName = "medal_unknown"; //itemType + "_unknown";
        getDefaultImage();

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use part of the available memory for this memory cache.
        final int cacheSize = maxMemory / 10;

        Log.w("test", "maxMemory : " + String.valueOf(maxMemory) + " and cacheSize : " + String.valueOf(cacheSize));

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void getDefaultImage() {
        mPlaceHolderBitmap = decodeSampledBitmapFromResource(context.getResources(), context.getResources().getIdentifier("com.davidpapazian.yokaiwatchmedals:drawable/" + defaultImageName, null, null), width, height);
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public Bitmap decodeSampledBitmapFromInternet(String imageName, int reqWidth, int reqHeight) {
        Bitmap b = null;
        try {
            URL url = new URL(ROOT_URL + "/" + itemType + "_500/" + imageName + ".png");
            InputStream is = null;
            is = new BufferedInputStream(url.openStream());
            Bitmap rawBitmap = BitmapFactory.decodeStream(is);  //new
            //Log.w("test", rawBitmap == null ? "rawBitmap is null" : "rawBitmap is not null");
            if (rawBitmap != null) {
                addBitmapToMemoryCache(imageName, rawBitmap);    //new
                DiskCacheHandler.addBitmapToDiskCache(itemType + "_500", rawBitmap, imageName);   //new
                b = Bitmap.createScaledBitmap(rawBitmap, reqWidth, reqHeight, false);
                //Log.w("test", "used cache : " + String.valueOf(mMemoryCache.size()) + " / " + String.valueOf(Runtime.getRuntime().maxMemory() / 1024));
            }
        }
        catch (IOException e) {Log.w("test", "file not found in decodeSampledBitmapFromInternet");
        }
        return b;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask medalWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(medalWorkerTask);
        }
        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public void loadBitmap(Item item, ImageView imageView) {
        String imageName = item.getImageName();
        if (cancelPotentialWork(imageName, imageView)) {
            final BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(imageName, imageView);
            asyncList.add(bitmapWorkerTask);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, bitmapWorkerTask);
            imageView.setImageDrawable(asyncDrawable);
            bitmapWorkerTask.execute();
        }
    }

    public boolean cancelPotentialWork(String imageName, ImageView imageView) {

        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapImageName = bitmapWorkerTask.imageName;
            if (!bitmapImageName.equals(imageName)) {

                // Cancel previous task
                bitmapWorkerTask.cancelTimer();
                bitmapWorkerTask.cancel(true);

                //dataWorkerTask.cancel(true);
                asyncList.remove(bitmapWorkerTask);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {

        private final WeakReference<View> viewReference;
        Handler handler = new Handler();
        TaskCanceler taskCanceler = new TaskCanceler(this);
        String imageName;
        boolean internetSlow = false;

        public BitmapWorkerTask(String imageName, ImageView imaageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            this.imageName = imageName;
            viewReference = new WeakReference<View>(imaageView);
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        }

        public void cancelTimer() {
            handler.removeCallbacks(taskCanceler);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Object... params) {
            handler.postDelayed(taskCanceler, 800);  //cancels task after 5 seconds
            taskCanceler.imageName = imageName;

            Bitmap rawBitmap = getBitmapFromMemCache(imageName);
            if (rawBitmap == null && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(itemType + "_500_enabled", false))
                rawBitmap = DiskCacheHandler.getBitmapFromDiskCache(itemType + "_500", imageName);
            if (rawBitmap == null)
                rawBitmap = DiskCacheHandler.getBitmapFromDiskCache(itemType + "_120", imageName);

            Bitmap b = null;
            if (rawBitmap != null)
                b = Bitmap.createScaledBitmap(rawBitmap, width, height, false);

            //cancels the cancellation
            if (taskCanceler != null && handler != null)
                handler.removeCallbacks(taskCanceler);
            return b;
        }

        public void onSlowInternet() {
            onPostExecute(null);
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {

            //Log.w("test", "task for " + imageName + " is onPostExecute");
            if (bitmap == null) {
                //Log.w("test", "task for " + imageName + " has null bitmap, gets from resources");
                int resId = context.getResources().getIdentifier("com.davidpapazian.yokaiwatchmedals:drawable/" + imageName, null, null);
                if (resId == 0) {resId = context.getResources().getIdentifier("com.davidpapazian.yokaiwatchmedals:drawable/" + defaultImageName, null, null);}
                bitmap = decodeSampledBitmapFromResource(context.getResources(), resId, width, height);
            }

            if (viewReference != null && bitmap != null) {
                //Log.w("test", "task for " + imageName + " has bitmap and should be displayed");
                final View view = viewReference.get();
                if (view != null) {
                    final ImageView imageView = (ImageView) view.findViewById(R.id.image);
                    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                    if (this == bitmapWorkerTask) {
                        imageView.setImageBitmap(bitmap);
                        asyncList.remove(this);
                    }
                }
            }
        }
    }

    public class TaskCanceler implements Runnable{
        private AsyncTask task;
        private String imageName = "???";

        public TaskCanceler(AsyncTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (task.getStatus() == AsyncTask.Status.RUNNING ) {
                //((BitmapWorkerTask) task).internetSlow = true;
                //Log.w("test", "task for code " + imageName + " is killed because it's slow");
                task.cancel(true);
                ((BitmapWorkerTask) task).onSlowInternet();
            }
        }
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
