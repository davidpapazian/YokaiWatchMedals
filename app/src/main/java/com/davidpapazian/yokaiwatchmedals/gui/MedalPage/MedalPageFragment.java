package com.davidpapazian.yokaiwatchmedals.gui.MedalPage;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.BaseGridAdapter;
import com.davidpapazian.yokaiwatchmedals.gui.BasePageFragment;
import com.davidpapazian.yokaiwatchmedals.gui.MedalGrid.MedalGridAdapter;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.ImageLoader;
import com.davidpapazian.yokaiwatchmedals.tools.SingleImageLoader;
import com.davidpapazian.yokaiwatchmedals.tools.Utils;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;
import com.davidpapazian.yokaiwatchmedals.views.SpaceItemDecoration;

public class MedalPageFragment extends BasePageFragment {

    private BaseGridAdapter mSecondAdapter;
    private RecyclerView mSecondGridView;
    private LinearLayoutManager mSecondLayoutManager;
    private AutoFitTextView mSecondListTitle;

    public MedalPageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medal_page, container, false);
        mItem = sMedalLibrary.getMedalFromId(mId);
        mAdapter = new MedalPageProductListAdapter(this);
        mSecondAdapter = new MedalPageYokaiGridAdapter(this);
        mTopLayout = view.findViewById(R.id.top_layout);
        mAppBarLayout = (AppBarLayout) view.findViewById(R.id.app_bar_layout);
        if (mFromMain)
            fillInitialViewFromMain(inflater);
        else
            fillInitialViewFromSecondary(inflater);
        return view;
    }

    @Override
    protected void fillInitialViewFromMain(LayoutInflater inflater) {
        Log.w("test", "MedalPageFragment fillInitialViewFromMain");
        View view = inflater.inflate(R.layout.medal_grid_item, (RelativeLayout) mTopLayout);
        MedalGridAdapter adapter = new MedalGridAdapter(null);
        int medalWidth;
        Log.w("test", "fill initial, Utils.getWidthPx() : " + String.valueOf(Utils.getWidthPx()));
        medalWidth = landscape ? (int)Math.floor(Utils.getWidthPx()/landscapeRatio) : Utils.getWidthPx();
        adapter.setImageLoader(new SingleImageLoader(getActivity(), "medal", medalWidth, medalWidth));
        adapter.fillView(view, MedalLibrary.getInstance().getMedalFromId(mId), 0);
    }

    @Override
    protected void fillInitialViewFromSecondary(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.medal_grid_item, (RelativeLayout) mTopLayout);
        MedalGridAdapter adapter = new MedalGridAdapter(null);
        int medalWidth = Utils.getWidthPx();
        adapter.setImageLoader(new SingleImageLoader(getActivity(), "medal", medalWidth, medalWidth));
        adapter.fillView(view, MedalLibrary.getInstance().getMedalFromId(mId), 0);
    }

    @Override
    protected void onPageReady() {
        Log.w("test", "onPageReady");

        if (!landscape) {
            mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
                    mImageView.setPadding(0, -verticalOffset, 0, 0);
                }
            });
        }

        //main list title
        mMainListTitle.setText(getResources().getString(R.string.medal_page_product_list_title));

        //deal with main as usual
        mGridView.getItemAnimator().setChangeDuration(0);
        mLayoutManager = new LinearLayoutManager(getActivity());
        int width = Utils.dpToPx(80);
        mAdapter.setImageLoader(new ImageLoader(getActivity(), "product", width, width));
        mGridView.setLayoutManager(mLayoutManager);
        mGridView.addItemDecoration(new SpaceItemDecoration(Utils.dpToPx(3)));
        mGridView.setAdapter(mAdapter);
        mAdapter.restoreList();

        //secondary list title
        mSecondListTitle = (AutoFitTextView) mExpandingView.findViewById(R.id.secondary_list_title);
        mSecondListTitle.setText(getResources().getString(R.string.medal_page_yokai_list_title));

        //deal with secondary initialization
        mSecondGridView = (RecyclerView) mExpandingView.findViewById(R.id.secondary_list);
        mSecondGridView.getItemAnimator().setChangeDuration(0);
        int column = 5; //landscape ? 5 : 5;
        mSecondLayoutManager = new GridLayoutManager(getActivity(), column);
        int swidth = Utils.getWidthPx()/6;
        mSecondAdapter.setImageLoader(new ImageLoader(getActivity(), "yokai", swidth, swidth));
        mSecondGridView.setLayoutManager(mSecondLayoutManager);
        mSecondGridView.addItemDecoration(new SpaceItemDecoration(Utils.dpToPx(3)));
        mSecondGridView.setAdapter(mSecondAdapter);
        mSecondAdapter.restoreList();

        mBottomLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void clear() {
        mBottomLayout.setVisibility(View.INVISIBLE);
        mGridView.setAdapter(null);
        mSecondGridView.setAdapter(null);
    }
}
