package com.davidpapazian.yokaiwatchmedals.gui;

import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.tools.ImageLoader;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseGridAdapter extends RecyclerView.Adapter<BaseGridAdapter.ItemViewHolder> {

    protected volatile ArrayList<? extends Item> mDataSet = new ArrayList<>();
    protected volatile ArrayList<? extends Item> mOriginalDataSet;
    protected BaseFragment fragment;
    protected final ExecutorService mUpdateExecutor = Executors.newSingleThreadExecutor();
    protected ItemComparator mComparator;
    protected FilterBySpinner mSpinnerFilter = new FilterBySpinner();
    protected ItemQueryFilter mQueryFilter = new ItemQueryFilter();
    protected ImageLoader mImageLoader;

    public BaseGridAdapter(BaseFragment fragment) {
        super();
        this.fragment = fragment;
    }

    @Override
    public abstract ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.fillView(mDataSet.get(position), position);
    }

    public FilterBySpinner getSpinnerFilter(){
        return mSpinnerFilter;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public Filter getFilter() {
        return mQueryFilter;
    }

    @MainThread
    public void update(final ArrayList<? extends Item> items, final boolean detectMoves) {
        mUpdateExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.w("test", "updating, new list : " + String.valueOf(items.size()));
                final ArrayList<? extends Item> newList = prepareNewList(items, detectMoves);
                final DiffUtil.DiffResult result = DiffUtil.calculateDiff(createDiffCallBack(newList), detectMoves);
                YWMApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataSet = newList;
                        result.dispatchUpdatesTo(BaseGridAdapter.this);
                        onListUpdated();
                    }
                });
            }
        });
    }

    public ArrayList<? extends Item> prepareNewList(ArrayList<? extends Item> items, boolean detectMoves) {
        Collections.sort(items, mComparator);
        return new ArrayList<>(items);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public View view;

        public ItemViewHolder(View view) {
            super(view);
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            Log.w("test", "click");
            int position = getLayoutPosition();
            if (position >= 0 && position < mDataSet.size())
                fragment.onItemClick(view, mDataSet.get(position), position);
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

        public void fillView(Item item, int position){
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            BaseGridAdapter.this.fillView(view, item, position);
        }
    }

    @MainThread
    public void restoreList() {
        Log.w("test", "restoring item list");
        if (mOriginalDataSet == null)
            setOriginalList();
        if (mOriginalDataSet != null) {
            Log.w("test", "in restoreList, about to update, size : " + String.valueOf(mOriginalDataSet.size()));
            update(new ArrayList<>(mOriginalDataSet), false);
            mOriginalDataSet = null;
        }
    }

    public void setOriginalList() {
        mOriginalDataSet = getOriginalList();
    }

    public abstract ArrayList<? extends Item> getOriginalList();

    public abstract void fillView(View view, Item item, int position);

    protected class ItemQueryFilter extends Filter {

        protected ArrayList<? extends Item> initData() {
            if (mOriginalDataSet == null)
                mOriginalDataSet = new ArrayList<>(mDataSet);
            return mOriginalDataSet;
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            final String[] queryStrings = charSequence.toString().trim().toLowerCase().split(" ");
            FilterResults results = new FilterResults();
            Set<Item> set = new HashSet<>();
            for (Item item : initData()) {
                for (String queryString : queryStrings) {
                    if (queryString.length() < 3)
                        continue;
                    if (item.getName() != null && item.getName().toLowerCase().contains(queryString)) {
                        set.add(item);
                        break;
                    }
                }
            }
            results.values = new ArrayList<>(set);
            results.count = set.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            Log.w("test", "in itemQueryFilter/publishResults, about to update, size : " + String.valueOf(((ArrayList<? extends Item>) filterResults.values).size()));
            update((ArrayList<? extends Item>) filterResults.values, false);
        }
    }

    protected abstract DiffUtil.Callback createDiffCallBack(final ArrayList<? extends Item> items);

    public void sortBy(int sortby) {
        mComparator.sortBy(sortby);
        Log.w("test", "in BaseGridAdapter/sortby, about to update, size : " + String.valueOf(mDataSet.size()));
        update(new ArrayList<>(mDataSet), true);
    }

    public class FilterBySpinner {

        private List<? extends Item> initData() {
            if (mOriginalDataSet == null)
                mOriginalDataSet = new ArrayList<>(mDataSet);
            return (ArrayList<? extends Item>) mOriginalDataSet;
        }

        public void filter(final Integer[] newState) {
            for (int i=0; i<newState.length; i++)
                Log.w("test", "states : " + String.valueOf(newState[i]));
            (new Runnable() {
                @Override
                public void run() {
                    publishResults(performFiltering(newState));
                }
            }).run();
        }

        private ArrayList<? extends Item> performFiltering(Integer[] newState) {
            ArrayList<Item> results = new ArrayList<>();
            for (Item item : initData())
                if (item.hasState(newState))
                    results.add(item);
            return results;
        }

        private void publishResults(ArrayList<? extends Item> filterResults) {
            Log.w("test", "in filterbyspinner/publishResults, about to update, size : " + String.valueOf(filterResults.size()));
            update(filterResults, false);
        }
    }

    private void onListUpdated() {
        Intent intent = new Intent();
        intent.setAction(MainActivity.LIST_UPDATED);
        YWMApplication.getInstance().sendBroadcast(intent);

        if (fragment instanceof BaseGridFragment)
            ((BaseGridFragment)fragment).onListUpdated();
    }

    public void setImageLoader(ImageLoader loader){
        mImageLoader = loader;
    }


}
