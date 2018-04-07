package com.davidpapazian.yokaiwatchmedals.gui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;

public abstract class BaseFragment extends Fragment {

    protected BaseGridAdapter mAdapter;
    protected RecyclerView mGridView;
    protected LinearLayoutManager mLayoutManager;
    protected MedalLibrary sMedalLibrary;
    protected boolean landscape;
    public static final double landscapeRatio = 2.5;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    protected void onItemClick(View view, Item item, int position) {

        //get view params
        int[] itemLocation = new int[2];
        view.getLocationOnScreen(itemLocation);
        int dx = view.getWidth();
        int dy = view.getHeight();
        int x = itemLocation[0];
        int y = itemLocation[1];
        int[] initialItemParams = new int[]{x, y, dx, dy};

        //get image params
        View imageView = view.findViewById(R.id.image);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        int idx = imageView.getWidth();
        int idy = imageView.getHeight();
        int[] initialImageParams = new int[]{params.leftMargin, params.topMargin, idx, idy};

        //getname params
        View nameView = view.findViewById(R.id.name);
        int[] nameLocation = new int[2];
        nameView.getLocationOnScreen(nameLocation);
        int ndx = nameView.getWidth();
        int ndy = nameView.getHeight();
        int nx = nameLocation[0];
        int ny = nameLocation[1];
        //save relative position, not absolute
        int[] initialNameParams = new int[]{nx-x, ny-y, ndx, ndy};

        Log.w("test", "                   " + String.valueOf(y));
        Log.w("test", "itemLocation : " + String.valueOf(x) + "      " + String.valueOf(x + dx));
        Log.w("test", "                   " + String.valueOf(y + dy));

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            Log.w("test", "item click, from main, type : " + String.valueOf(item.getType()) + ", and id : " + String.valueOf(item.getId()));
            Intent intent = new Intent(activity, SecondaryActivity.class);
            intent.putExtra("type", item.getType());
            intent.putExtra("id", item.getId());
            intent.putExtra("initialItemParams", initialItemParams);
            intent.putExtra("initialImageParams", initialImageParams);
            intent.putExtra("initialNameParams", initialNameParams);
            startActivity(intent);
        } else if (activity instanceof SecondaryActivity) {
            Log.w("test", "item click, from secondary, type : " + String.valueOf(item.getType()) + ", and id : " + String.valueOf(item.getId()));
            ((SecondaryActivity) activity).showPageFragment(item.getType(), item.getId(), initialItemParams, initialImageParams, initialNameParams);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        super.onViewCreated(view, savedInstanceState);
    }
}
