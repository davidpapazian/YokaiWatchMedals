package com.davidpapazian.yokaiwatchmedals.gui.ProductList;

import android.media.Image;
import android.support.transition.TransitionManager;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.BaseGridAdapter;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Product;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;
import com.davidpapazian.yokaiwatchmedals.tools.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class ProductListAdapter extends BaseGridAdapter {

    private int mExpandedPosition = -1;

    public ProductListAdapter(ProductListFragment fragment) {
        super(fragment);
        mComparator = new ItemComparator("product");
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void fillView(View view, Item item, final int position) {
        Product product = (Product) item;
        mImageLoader.loadBitmap(item, (ImageView) view.findViewById(R.id.image));
        ((TextView) view.findViewById(R.id.name)).setText(product.getName());

        //set the views that are not needed for expansion
        if (fragment != null) {
            view.findViewById(R.id.date).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.date)).setText(product.getDate());
        }
    }

    @Override
    protected DiffUtil.Callback createDiffCallBack(ArrayList<? extends Item> items) {
        return new ProductDiffCallback((ArrayList<Product>) mDataSet, (ArrayList<Product>) items);
    }

    @Override
    public ArrayList<Product> getOriginalList() {
        return MedalLibrary.getInstance().getProductList();
    }

    public static class ProductDiffCallback extends DiffUtil.Callback {
        ArrayList<Product> oldList, newList;

        public ProductDiffCallback(ArrayList<Product> oldList, ArrayList<Product> newList) {
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
            Product oldItem = oldList.get(oldItemPosition);
            Product newItem = newList.get(newItemPosition);
            return oldItem != null && newItem != null && (oldItem.getId() == newItem.getId());
        }

        @Override   //check images and extras
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return true;
        }
    }
}
