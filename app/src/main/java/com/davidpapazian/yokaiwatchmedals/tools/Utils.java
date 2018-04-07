package com.davidpapazian.yokaiwatchmedals.tools;

import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;


public class Utils {


    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getHeightPx() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getWidthPx() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }


}
