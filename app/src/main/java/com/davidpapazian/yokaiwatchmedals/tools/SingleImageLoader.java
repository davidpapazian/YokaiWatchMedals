package com.davidpapazian.yokaiwatchmedals.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;

public class SingleImageLoader extends ImageLoader {

    public SingleImageLoader(Context context, String itemType, int width, int height) {
        super(context, itemType, width, height);
    }

    @Override
    public void loadBitmap(Item item, ImageView imageView) {
        String imageName = item.getImageName();

        Bitmap rawBitmap = getBitmapFromMemCache(imageName);
        if (rawBitmap == null && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(itemType + "_500_enabled", false))
            rawBitmap = DiskCacheHandler.getBitmapFromDiskCache(itemType + "_500", imageName);
        if (rawBitmap == null)
            rawBitmap = DiskCacheHandler.getBitmapFromDiskCache(itemType + "_120", imageName);

        Bitmap b = null;
        if (rawBitmap != null)
            b = rawBitmap; //Bitmap.createScaledBitmap(rawBitmap, width, height, false);
        else
            b = decodeSampledBitmapFromResource(context.getResources(),
                    context.getResources().getIdentifier("com.davidpapazian.yokaiwatchmedals:drawable/" + defaultImageName, null, null),
                    width,
                    height);
        imageView.setImageBitmap(b);
    }
}
