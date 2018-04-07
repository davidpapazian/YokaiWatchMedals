package com.davidpapazian.yokaiwatchmedals.gui.YokaiGrid;

import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.BaseGridAdapter;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Product;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Yokai;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;

import java.util.ArrayList;

public class YokaiGridAdapter extends BaseGridAdapter {

    public YokaiGridAdapter(YokaiGridFragment fragment) {
        super(fragment);
        mComparator = new ItemComparator("yokai");
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.yokai_grid_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void fillView(View view, Item item, int position) {
        Yokai yokai = (Yokai) item;
        mImageLoader.loadBitmap(item, (ImageView) view.findViewById(R.id.image));
        ((AutoFitTextView) view.findViewById(R.id.name)).setText(yokai.getName());

        //set the views that are not needed for expansion
        if (fragment != null) {
            view.findViewById(R.id.top_details).setVisibility(View.VISIBLE);
            ((AutoFitTextView) view.findViewById(R.id.tribe)).setText(yokai.getTribeName(), 16);
            ((AutoFitTextView) view.findViewById(R.id.type)).setText(yokai.getTypeName(), 16);
        }
    }

    @Override
    protected DiffUtil.Callback createDiffCallBack(ArrayList<? extends Item> items) {
        return new YokaiDiffCallback((ArrayList<Yokai>) mDataSet, (ArrayList<Yokai>) items);
    }

    @Override
    public ArrayList<Yokai> getOriginalList() {
        return MedalLibrary.getInstance().getYokaiList();
    }

    public static class YokaiDiffCallback extends DiffUtil.Callback {
        ArrayList<Yokai> oldList, newList;

        public YokaiDiffCallback(ArrayList<Yokai> oldList, ArrayList<Yokai> newList) {
            this.oldList = new ArrayList<>(oldList);
            this.newList = new ArrayList<>(newList);
        }

        @Override
        public int getOldListSize() {
            return oldList == null ? 0 : oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList == null ? 0 : newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Yokai oldItem = oldList.get(oldItemPosition);
            Yokai newItem = newList.get(newItemPosition);
            return oldItem != null && newItem != null && (oldItem.getId() == newItem.getId());
        }

        @Override   //check images and extras
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return true;
        }

    }
}
