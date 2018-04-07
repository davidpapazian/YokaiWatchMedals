package com.davidpapazian.yokaiwatchmedals.gui.MedalGrid;

import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.davidpapazian.yokaiwatchmedals.gui.BaseFragment;
import com.davidpapazian.yokaiwatchmedals.gui.BaseGridAdapter;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Medal;
import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.ImageLoader;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;

import java.util.ArrayList;

public class MedalGridAdapter extends BaseGridAdapter {

    public MedalGridAdapter(MedalGridFragment fragment) {
        super(fragment);
        mComparator = new ItemComparator("medal");
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medal_grid_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void fillView(View view, Item item, int position){
        Medal medal = (Medal) item;
        mImageLoader.loadBitmap(item, (ImageView) view.findViewById(R.id.image));
        ((AutoFitTextView) view.findViewById(R.id.name)).setText(medal.getName(), 20);

        //set the views that are not needed for expansion
        if (fragment != null) {
            view.findViewById(R.id.top_details).setVisibility(View.VISIBLE);
            ((AutoFitTextView) view.findViewById(R.id.serie)).setText(medal.getSerieName(), 16);
            ((AutoFitTextView) view.findViewById(R.id.code)).setText(medal.getCode(), 16);
            ((AutoFitTextView) view.findViewById(R.id.type)).setText(medal.getTypeName(), 16);
        }
    }

    @Override
    public DiffUtil.Callback createDiffCallBack(final ArrayList<? extends Item> items) {
        return new MedalDiffCallback((ArrayList<Medal>) mDataSet, (ArrayList<Medal>) items);
    }

    @Override
    public ArrayList<Medal> getOriginalList() {
        return MedalLibrary.getInstance().getMedalList();
    }

    public static class MedalDiffCallback extends DiffUtil.Callback {
        ArrayList<Medal> oldList, newList;

        public MedalDiffCallback(ArrayList<Medal> oldList, ArrayList<Medal> newList) {
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
            Medal oldItem = oldList.get(oldItemPosition);
            Medal newItem = newList.get(newItemPosition);
            return oldItem != null && newItem != null && (oldItem.getId() == newItem.getId());
        }

        @Override   //check images and extras
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            //Medal oldItem = oldList.get(oldItemPosition);
            //Medal newItem = newList.get(newItemPosition);
            return true;
        }

        /*
        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            Medal oldItem = oldList.get(oldItemPosition);
            Medal newItem = newList.get(newItemPosition);

            if (oldItem.getQuantity() != newItem.getQuantity())
                return UPDATE_QUANTITY;
            else
                return UPDATE_IMAGE;

            return null;
        }
        */
    }
}