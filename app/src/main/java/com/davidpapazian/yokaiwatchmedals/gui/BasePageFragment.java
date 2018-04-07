package com.davidpapazian.yokaiwatchmedals.gui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.Utils;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;

public abstract class BasePageFragment extends BaseFragment {

    protected Item mItem;
    protected int mId;
    protected int[] mInitialItemParams;
    protected int[] mInitialImageParams;
    protected int[] mInitialNameParams;
    protected AppBarLayout mAppBarLayout;
    protected View mExpandingView;
    protected View mTopLayout;
    protected View mBottomLayout;
    protected ImageView mImageView;
    protected AutoFitTextView mNameView;
    protected AutoFitTextView mMainListTitle;
    protected boolean alreadyPopedUp = false;
    protected boolean mFromMain = true;

    public static final int ANIMATION_DURATION = 200;

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(false);
        sMedalLibrary = MedalLibrary.getInstance();
        if (bundle == null)
            bundle = getArguments();
        if (bundle != null) {
            mId = bundle.getInt("id", 0);
            mFromMain = bundle.getBoolean("fromMain", true);
            mInitialItemParams = bundle.getIntArray("initialItemParams");
            mInitialImageParams = bundle.getIntArray("initialImageParams");
            mInitialNameParams = bundle.getIntArray("initialNameParams");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mExpandingView = view.findViewById(R.id.expanding_layout);
        mGridView = (RecyclerView) view.findViewById(R.id.main_list);
        mBottomLayout = view.findViewById(R.id.bottom_layout);
        mNameView = (AutoFitTextView) view.findViewById(R.id.name);
        mMainListTitle = (AutoFitTextView) view.findViewById(R.id.main_list_title);
        mImageView = (ImageView) view.findViewById(R.id.image);

        if (alreadyPopedUp)
            expand();
        else
            popUpAndExpand();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.w("test", "on hidden : " + (hidden ? "yes" : "no"));
        if (!hidden) //((SecondaryActivity)getActivity()).getCurrentFragment() == null)
            ((SecondaryActivity)getActivity()).setCurrentFragment(this);
        super.onHiddenChanged(hidden);
    }

    public void popUpAndExpand() {
        //shape the whole expanding view
        final int fdx = Utils.getWidthPx();
        final int fdy = Utils.getHeightPx();
        Log.w("test", "fdy : " + String.valueOf(fdy));
        final int x = mInitialItemParams[0];
        final int y = mInitialItemParams[1];
        final int dx = mInitialItemParams[2];
        final int dy = mInitialItemParams[3];
        final RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(dx, dy);
        itemParams.leftMargin = x;
        itemParams.topMargin = y;
        mExpandingView.setLayoutParams(itemParams);

        final int fidx = landscape ? (int)Math.floor(Utils.getWidthPx()/landscapeRatio) : Utils.getWidthPx();
        final int ix = mInitialImageParams[0];
        final int idx = mInitialImageParams[2];
        final RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();

        final int fndx = landscape ? fidx : fdx;
        final int ndx = mInitialNameParams[2];
        final int nx = mInitialNameParams[0];
        final RelativeLayout.LayoutParams nameParams = (RelativeLayout.LayoutParams) mNameView.getLayoutParams();
        final int ny = nameParams.topMargin;
        mInitialNameParams[1] = ny; //the negative value is replaced by 0 by the recycler view, so lets update it;


        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float t, Transformation _) {
                t=t*t;

                itemParams.width = Math.round(dx + (fdx-dx)*t);
                itemParams.height = Math.round(dy + (fdy-dy)*t);
                itemParams.leftMargin = Math.round(x*(1-t));
                itemParams.topMargin = Math.round(y*(1-t));

                imageParams.width = Math.round(idx + (fidx-idx)*t);
                imageParams.height = imageParams.width;
                imageParams.leftMargin = Math.round(ix*(1-t));

                nameParams.width = Math.round(ndx + (fndx-ndx)*t);
                nameParams.leftMargin = Math.round(nx*(1-t));
                nameParams.topMargin = Math.round(ny*(1-t));

                mExpandingView.setLayoutParams(itemParams);
                mImageView.setLayoutParams(imageParams);
                mNameView.setLayoutParams(nameParams);
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(ANIMATION_DURATION);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.w("test", "mev height : " + String.valueOf(mExpandingView.getHeight()) + ", width : " + String.valueOf(mExpandingView.getWidth()));
                Log.w("test", "bot height : " + String.valueOf(mBottomLayout.getHeight()) + ", width : " + String.valueOf(mBottomLayout.getWidth()));
                Log.w("test", "top height : " + String.valueOf(mTopLayout.getHeight()) + ", width : " + String.valueOf(mTopLayout.getWidth()));
                Log.w("test", "img height : " + String.valueOf(mImageView.getHeight()) + ", width : " + String.valueOf(mImageView.getWidth()));
                onPageReady();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mExpandingView.startAnimation(a);
        alreadyPopedUp = true;
    }

    public void expand() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int fdx = metrics.widthPixels;
        final int fdy = metrics.heightPixels;

        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(fdx, fdy);
        params.leftMargin = 0;
        params.topMargin = 0;
        mExpandingView.setLayoutParams(params);
        //fillInitialViewFromMain(inflateExpandingView());
    }

    public void collapse() {

        clear();

        final int dx = Utils.getWidthPx();
        final int dy = Utils.getHeightPx();

        final RelativeLayout.LayoutParams itemParams = (RelativeLayout.LayoutParams) mExpandingView.getLayoutParams();
        final int fx = mInitialItemParams[0];
        final int fy = mInitialItemParams[1];
        final int fdx = mInitialItemParams[2];
        final int fdy = mInitialItemParams[3];

        final RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
        final int fix = mInitialImageParams[0];
        final int fidx = mInitialImageParams[2];
        final int idx = landscape ? (int)Math.floor(Utils.getWidthPx()/landscapeRatio) : Utils.getWidthPx();

        final RelativeLayout.LayoutParams nameParams = (RelativeLayout.LayoutParams) mNameView.getLayoutParams();
        final int fnx = mInitialNameParams[0];
        final int fny = mInitialNameParams[1];
        final int fndx = mInitialNameParams[2];
        final int ndx = landscape ? idx : dx;

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float t, Transformation _) {

                t=t*t;

                itemParams.width = Math.round(dx + (fdx-dx)*t);
                itemParams.height = Math.round(dy + (fdy-dy)*t);
                itemParams.leftMargin = Math.round(fx*t);
                itemParams.topMargin = Math.round(fy*t);

                imageParams.width = Math.round(idx + (fidx-idx)*t);
                imageParams.height = imageParams.width;
                imageParams.leftMargin = Math.round(fix*t);

                nameParams.width = Math.round(ndx + (fndx-ndx)*t);
                nameParams.leftMargin = Math.round(fnx*t);
                nameParams.topMargin = Math.round(fny*t);

                mExpandingView.setLayoutParams(itemParams);
                mImageView.setLayoutParams(imageParams);
                mNameView.setLayoutParams(nameParams);

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(ANIMATION_DURATION);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                ((SecondaryActivity)getActivity()).onCurrentFragmentReadyToClose();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mExpandingView.startAnimation(a);
    }

    protected abstract void fillInitialViewFromMain(LayoutInflater inflater);

    protected abstract void fillInitialViewFromSecondary(LayoutInflater inflater);

    protected abstract void onPageReady();

    protected void onBackPressed() {
        collapse();
    }

    public Item getItem() {
        return mItem;
    }

    protected abstract void clear();
}

