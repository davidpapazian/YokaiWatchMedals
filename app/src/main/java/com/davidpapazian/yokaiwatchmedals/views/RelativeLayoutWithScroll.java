package com.davidpapazian.yokaiwatchmedals.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.davidpapazian.yokaiwatchmedals.R;

public class RelativeLayoutWithScroll extends RelativeLayout {

    private RecyclerView mGridView;
    private View mTopView;
    private View mImageView;
    private int max;
    private int min;
    private float mYTouch;

    public RelativeLayoutWithScroll(Context context) {
        super(context);
    }

    public RelativeLayoutWithScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutWithScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(final int min, final int max) {
        this.min = min;
        this.max = max;
        mTopView = findViewById(R.id.top_layout);
        mImageView = findViewById(R.id.image);
        mGridView = (RecyclerView) findViewById(R.id.main_list);
        mGridView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                final int action = ev.getAction();
                Log.w("test", "action : " + String.valueOf(action));
                if (action == MotionEvent.ACTION_DOWN) {
                    mYTouch = ev.getRawY();
                } else if (action == MotionEvent.ACTION_MOVE) {
                    final float deltaY = ev.getRawY()-mYTouch;
                    mYTouch = ev.getRawY();
                    if (isRecyclerViewAtTheTop() &&
                            ( (deltaY>0 && mImageView.getHeight()<max) || (deltaY < 0 && mImageView.getHeight()>min) )) {
                        updateHeight(deltaY);
                        return true;
                    }
                }
                return true;
            }
        });
        /*
        mTopView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                int action = ev.getAction();
                Log.w("test", "onTouch, action : " + String.valueOf(action) + ", scroll : " + String.valueOf(mGridView.getScrollY()) + ", img height : " + String.valueOf(mImageView.getHeight()));
                if (action == MotionEvent.ACTION_MOVE) {
                    final float deltaY = ev.getRawY()-mYTouch;
                    mYTouch = ev.getRawY();
                    Log.w("test", "move, delta : " + String.valueOf(deltaY));
                    if (mGridView.computeVerticalScrollOffset() == 0 && mImageView.getHeight() >= min) {
                        updateHeight(deltaY);
                    }
                }
                return true;
            }
        });
        */
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mGridView.dispatchTouchEvent(ev);
    }

    private void updateHeight(float deltaY) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
        if ((deltaY > 0 && mImageView.getHeight() <= max) || (deltaY < 0 && mImageView.getHeight() >= min)) {
            params.height = Math.round(Math.max(Math.min(params.height+deltaY, max), min));
            mImageView.setLayoutParams(params);
        }
    }

    private boolean isRecyclerViewAtTheTop() {
        return ((LinearLayoutManager)mGridView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 0;
    }
}
