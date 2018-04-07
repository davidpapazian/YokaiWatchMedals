package com.davidpapazian.yokaiwatchmedals.views;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.davidpapazian.yokaiwatchmedals.R;

/**
 * Created by David on 19/11/2017.
 */
public class AutoScalingImageBehaviour extends CoordinatorLayout.Behavior<RelativeLayout> {

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        Log.w("test", "((AppBarLayout)dependency).getTotalScrollRange() : " + String.valueOf(((AppBarLayout)dependency).getTotalScrollRange()));
        Log.w("test", "((AppBarLayout)dependency).getScrollY() : " + String.valueOf(((AppBarLayout)dependency).getScrollY()));
        Log.w("test", "((AppBarLayout)dependency).getMeasuredHeight() : " + String.valueOf(((AppBarLayout)dependency).getMeasuredHeight()));
        //child.findViewById(R.id.image).setScaleY( ((AppBarLayout)dependency).getTotalScrollRange()  );

        return super.onDependentViewChanged(parent, child, dependency);
    }
}
