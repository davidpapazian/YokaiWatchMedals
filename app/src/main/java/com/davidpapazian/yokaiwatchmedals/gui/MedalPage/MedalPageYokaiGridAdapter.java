package com.davidpapazian.yokaiwatchmedals.gui.MedalPage;

import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.BaseGridAdapter;
import com.davidpapazian.yokaiwatchmedals.gui.BasePageFragment;
import com.davidpapazian.yokaiwatchmedals.gui.YokaiGrid.YokaiGridAdapter;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Medal;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Yokai;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;

import java.util.ArrayList;

public class MedalPageYokaiGridAdapter extends BaseGridAdapter {

    public MedalPageYokaiGridAdapter(MedalPageFragment fragment) {
        super(fragment);
        mComparator = new ItemComparator("yokai");
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medal_page_yokai_grid_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void fillView(View view, Item item, int position) {
        Log.w("test", "filling view, id : " + String.valueOf(item.getId()));
        Yokai yokai = (Yokai) item;
        mImageLoader.loadBitmap(item, (ImageView) view.findViewById(R.id.image));
        ((AutoFitTextView) view.findViewById(R.id.name)).setText(yokai.getName());
    }

    @Override
    public ArrayList<? extends Item> getOriginalList() {
        return ((Medal) ((BasePageFragment)fragment).getItem()).getYokaiList();
    }

    @Override
    protected DiffUtil.Callback createDiffCallBack(ArrayList<? extends Item> items) {
        return new YokaiGridAdapter.YokaiDiffCallback((ArrayList<Yokai>) mDataSet, (ArrayList<Yokai>) items);
    }
}