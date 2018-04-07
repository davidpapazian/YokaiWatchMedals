package com.davidpapazian.yokaiwatchmedals.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int marginLeft;
    private final int marginTop;
    private final int marginRight;
    private final int marginBottom;

    public SpaceItemDecoration(int marginLeft, int marginTop, int marginRight, int marginBottom) {
        this.marginLeft = marginLeft;
        this.marginTop = marginTop;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
    }

    public SpaceItemDecoration(int margin) {
        this.marginLeft = margin;
        this.marginTop = margin;
        this.marginRight = margin;
        this.marginBottom = margin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = marginLeft;
        outRect.top = marginTop;
        outRect.right = marginRight;
        outRect.bottom = marginBottom;
    }
}