package com.davidpapazian.yokaiwatchmedals.gui.ProductPage;

import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.BaseGridAdapter;
import com.davidpapazian.yokaiwatchmedals.gui.BasePageFragment;
import com.davidpapazian.yokaiwatchmedals.gui.MedalGrid.MedalGridAdapter;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Medal;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Product;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;

import java.util.ArrayList;

public class ProductPageMedalGridAdapter extends BaseGridAdapter {

    public ProductPageMedalGridAdapter(ProductPageFragment fragment) {
        super(fragment);
        mComparator = new ItemComparator("medal");
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medal_grid_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void fillView(View view, Item item, int position) {
        Medal medal = (Medal) item;
        mImageLoader.loadBitmap(item, (ImageView) view.findViewById(R.id.image));
        ((AutoFitTextView) view.findViewById(R.id.name)).setText(medal.getName(), 20);

        //set the views that are not needed for expansion
        if (fragment != null) {
            //should never be the case
            view.findViewById(R.id.top_details).setVisibility(View.VISIBLE);
            ((AutoFitTextView) view.findViewById(R.id.serie)).setText(medal.getSerieName(), 16);
            ((AutoFitTextView) view.findViewById(R.id.type)).setText(medal.getTypeName(), 16);
            ((AutoFitTextView) view.findViewById(R.id.code)).setText(((Product) ((BasePageFragment) fragment).getItem()).getMedalCodeList().get(item.getId()), 16);
        }
    }

    @Override
    public ArrayList<? extends Item> getOriginalList() {
        return ((Product) ((BasePageFragment)fragment).getItem()).getMedalList();
    }

    @Override
    protected DiffUtil.Callback createDiffCallBack(ArrayList<? extends Item> items) {
        return new MedalGridAdapter.MedalDiffCallback((ArrayList<Medal>) mDataSet, (ArrayList<Medal>) items);
    }
}
