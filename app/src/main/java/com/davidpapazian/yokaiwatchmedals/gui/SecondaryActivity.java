package com.davidpapazian.yokaiwatchmedals.gui;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.MedalPage.MedalPageFragment;
import com.davidpapazian.yokaiwatchmedals.gui.ProductPage.ProductPageFragment;
import com.davidpapazian.yokaiwatchmedals.gui.YokaiPage.YokaiPageFragment;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;

public class SecondaryActivity extends AppCompatActivity {

    BasePageFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_secondary);

        Log.w("test", "secondary, onCreate");
        if (mCurrentFragment == null) {
            Log.w("test", "mCurrentFragment is null");
            Intent intent = getIntent();
            showPageFragment(intent.getIntExtra("type", 0),
                    intent.getIntExtra("id", 0),
                    intent.getIntArrayExtra("initialItemParams"),
                    intent.getIntArrayExtra("initialImageParams"),
                    intent.getIntArrayExtra("initialNameParams"));
        } else {
            Log.w("test", "mCurrentFragment not null");
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
        {
            public void onBackStackChanged()
            {
                // Your logic here
            }
        });
    }

    public void showPageFragment(int type, int id, int[] initialItemParams, int[] initialImageParams, int[] initialNameParams) {
        Log.w("test", "showPageFragment, type : " + String.valueOf(type) + ", and id : " + String.valueOf(id));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BasePageFragment fragment;
        switch (type) {
            case Item.TYPE_MEDAL:
                fragment = new MedalPageFragment();
                break;
            case Item.TYPE_PRODUCT:
                fragment = new ProductPageFragment();
                break;
            default:
                fragment = new YokaiPageFragment();
                break;
        }

        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putIntArray("initialItemParams", initialItemParams);
        bundle.putIntArray("initialImageParams", initialImageParams);
        bundle.putIntArray("initialNameParams", initialNameParams);
        bundle.putBoolean("fromMain", mCurrentFragment == null);
        fragment.setArguments(bundle);

        String tag = String.valueOf(type) + "_" + String.valueOf(id);
        ft.add(R.id.fragment_placeholder, fragment, tag);
        //if (mCurrentFragment != null)
            ft.addToBackStack(tag);
        ft.commit();
        mCurrentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        Log.w("test", "onBackPressed in activity");
        mCurrentFragment.onBackPressed();
    }

    public void onCurrentFragmentReadyToClose() {
        super.onBackPressed();
        Log.w("test", "onCurrentFragmentReadyToClose in activity");
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            mCurrentFragment = (BasePageFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
        else
            super.onBackPressed();
    }

    public BasePageFragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public void setCurrentFragment(BasePageFragment fragment) {
        mCurrentFragment = fragment;
    }

}
