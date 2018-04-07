package com.davidpapazian.yokaiwatchmedals.gui.MedalPage;

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
import com.davidpapazian.yokaiwatchmedals.gui.BasePageFragment;
import com.davidpapazian.yokaiwatchmedals.gui.ProductList.ProductListAdapter;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Medal;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Product;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;
import com.davidpapazian.yokaiwatchmedals.tools.Utils;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;

import java.util.ArrayList;

public class MedalPageProductListAdapter extends BaseGridAdapter {

    public MedalPageProductListAdapter(MedalPageFragment fragment) {
        super(fragment);
        mComparator = new ItemComparator("product");
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medal_page_product_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void fillView(View view, Item item, int position) {
        Log.w("test", "filling view, id : " + String.valueOf(item.getId()));
        Product product = (Product) item;
        mImageLoader.loadBitmap(item, (ImageView) view.findViewById(R.id.image));
        ((TextView) view.findViewById(R.id.name)).setText(product.getName());

        //set the views that are not needed for expansion
        if (fragment != null) {
            ((AutoFitTextView) view.findViewById(R.id.code)).setText(((Medal) ((BasePageFragment) fragment).getItem()).getProductCodeList().get(item.getId()));
        }
    }

    @Override
    public ArrayList<? extends Item> getOriginalList() {
        return ((Medal) ((BasePageFragment)fragment).getItem()).getProductList();
    }

    @Override
    protected DiffUtil.Callback createDiffCallBack(ArrayList<? extends Item> items) {
        return new ProductListAdapter.ProductDiffCallback((ArrayList<Product>) mDataSet, (ArrayList<Product>) items);
    }
}
