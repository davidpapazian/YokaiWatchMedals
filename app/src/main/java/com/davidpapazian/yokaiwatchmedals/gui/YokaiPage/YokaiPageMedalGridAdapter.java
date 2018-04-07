package com.davidpapazian.yokaiwatchmedals.gui.YokaiPage;

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
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Yokai;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;

import java.util.ArrayList;

public class YokaiPageMedalGridAdapter extends BaseGridAdapter {

    public YokaiPageMedalGridAdapter(YokaiPageFragment fragment) {
        super(fragment);
        mComparator = new ItemComparator("medal");
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //the same as ProductPageMedalGridAdapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medal_grid_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void fillView(View view, Item item, int position) {
        //the same as ProductPageMedalGridAdapter
        Medal medal = (Medal) item;
        mImageLoader.loadBitmap(item, (ImageView) view.findViewById(R.id.image));
        ((AutoFitTextView) view.findViewById(R.id.name)).setText(medal.getName());

        //set the views that are not needed for expansion
        if (fragment != null) {
            ((AutoFitTextView) view.findViewById(R.id.name)).setText(medal.getName(), 20);
            ((AutoFitTextView) view.findViewById(R.id.serie)).setText(medal.getSerieName(), 16);
            ((AutoFitTextView) view.findViewById(R.id.code)).setText(((Yokai) ((BasePageFragment)fragment).getItem()).getMedalCodeList().get(item.getId()), 16);
            ((AutoFitTextView) view.findViewById(R.id.type)).setText(medal.getTypeName(), 16);
        }
    }

    @Override
    public ArrayList<? extends Item> getOriginalList() {
        return ((Yokai) ((BasePageFragment)fragment).getItem()).getMedalList();
    }

    @Override
    protected DiffUtil.Callback createDiffCallBack(ArrayList<? extends Item> items) {
        return new MedalGridAdapter.MedalDiffCallback((ArrayList<Medal>) mDataSet, (ArrayList<Medal>) items);
    }
}
