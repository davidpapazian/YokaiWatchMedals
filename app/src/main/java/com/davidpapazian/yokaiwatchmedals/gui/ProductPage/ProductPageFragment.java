package com.davidpapazian.yokaiwatchmedals.gui.ProductPage;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.BasePageFragment;
import com.davidpapazian.yokaiwatchmedals.gui.MedalPage.MedalPageProductListAdapter;
import com.davidpapazian.yokaiwatchmedals.gui.ProductList.ProductListAdapter;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Product;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.ImageLoader;
import com.davidpapazian.yokaiwatchmedals.tools.SingleImageLoader;
import com.davidpapazian.yokaiwatchmedals.tools.Utils;
import com.davidpapazian.yokaiwatchmedals.views.RelativeLayoutWithScroll;
import com.davidpapazian.yokaiwatchmedals.views.SpaceItemDecoration;

public class ProductPageFragment extends BasePageFragment {

    private final static int SPACING = 3; //spacing in dp
    private final static int PADDING = 3; //padding in dp

    public ProductPageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_page, container, false);
        mItem = sMedalLibrary.getProductFromId(mId);
        mAdapter = new ProductPageMedalGridAdapter(this);
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
        Log.w("test", "filling from main");
        View view = inflater.inflate(R.layout.product_list_item, (RelativeLayout) mTopLayout);

        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        imageView.setLayoutParams(params);

        int width = Utils.dpToPx(80);
        ProductListAdapter adapter = new ProductListAdapter(null);
        adapter.setImageLoader(new SingleImageLoader(getActivity(), "product", width, width));
        adapter.fillView(view, MedalLibrary.getInstance().getProductFromId(mId), 0);
    }

    @Override
    protected void fillInitialViewFromSecondary(LayoutInflater inflater) {
        Log.w("test", "filling from secondaty");
        View view = inflater.inflate(R.layout.medal_page_product_list_item, (RelativeLayout) mTopLayout);

        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        imageView.setLayoutParams(params);

        int width = Utils.dpToPx(80);
        ProductListAdapter adapter = new ProductListAdapter(null);
        adapter.setImageLoader(new SingleImageLoader(getActivity(), "product", width, width));
        adapter.fillView(view, MedalLibrary.getInstance().getProductFromId(mId), 0);
    }

    @Override
    protected void onPageReady() {
        if (!landscape) {
            mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
                    mImageView.setPadding(0, -verticalOffset, 0, 0);
                }
            });
        }
        mGridView.getItemAnimator().setChangeDuration(0);
        int column = landscape ? 4 : 3;
        int width = (int) Math.floor( (Utils.getWidthPx() - Utils.dpToPx(2*PADDING + (column-1)*SPACING)) / column );
        mAdapter.setImageLoader(new ImageLoader(getActivity(), "medal", width, width));

        mLayoutManager = new GridLayoutManager(getActivity(), column);
        mGridView.setLayoutManager(mLayoutManager);
        mGridView.addItemDecoration(new SpaceItemDecoration(Utils.dpToPx(3)));
        mGridView.setAdapter(mAdapter);
        mAdapter.restoreList();

        mBottomLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void clear() {
        mBottomLayout.setVisibility(View.INVISIBLE);
        mGridView.setAdapter(null);
    }
}
