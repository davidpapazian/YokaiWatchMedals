package com.davidpapazian.yokaiwatchmedals.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.davidpapazian.yokaiwatchmedals.R;

public class RelativeLayoutWithCollapsingImage extends RelativeLayout {

    private View mImageView;
    private int max;
    private int min;
    private float mYTouch;

    public RelativeLayoutWithCollapsingImage(Context context) {
        super(context);
    }

    public RelativeLayoutWithCollapsingImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutWithCollapsingImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize() {
        mImageView = findViewById(R.id.image);
        max = mImageView.getLayoutParams().height;
        min = max/4;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        Log.w("test", "action : " + String.valueOf(action));
        if (action == MotionEvent.ACTION_DOWN) {
            mYTouch = ev.getRawY();
        } else if (action == MotionEvent.ACTION_MOVE) {
            final float deltaY = ev.getRawY()-mYTouch;
            mYTouch = ev.getRawY();
            updateHeight(deltaY);
            //return true;
        }
        return super.onTouchEvent(ev);
    }

    private void updateHeight(float deltaY) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
        params.height = Math.round(Math.max(Math.min(params.height+deltaY, max), min));
        mImageView.setLayoutParams(params);
    }

    public boolean isMin() {
        return mImageView.getHeight() <= min;
    }

    public boolean isMax() {
        return mImageView.getHeight() >= max;
    }
}