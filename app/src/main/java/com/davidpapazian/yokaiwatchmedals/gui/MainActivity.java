package com.davidpapazian.yokaiwatchmedals.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.tools.UpdateHandler;
import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.gui.MedalGrid.MedalGridFragment;
import com.davidpapazian.yokaiwatchmedals.gui.Preferences.PreferencesActivity;
import com.davidpapazian.yokaiwatchmedals.gui.ProductList.ProductListFragment;
import com.davidpapazian.yokaiwatchmedals.gui.YokaiGrid.YokaiGridFragment;
import com.davidpapazian.yokaiwatchmedals.interfaces.IFilterable;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.DatabaseHelper;
import com.davidpapazian.yokaiwatchmedals.tools.LocaleHelper;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener  {

    protected static final String ID_MEDALGRID = "medal_grid";
    protected static final String ID_PRODUCTLIST = "product_list";
    protected static final String ID_YOKAIGRID = "yokai_grid";
    protected static final String ID_SEARCH = "search";
    protected static final String ID_SETTINGS = "settings";
    protected static final String ID_ABOUT = "about";

    private static final int ACTIVITY_RESULT_PREFERENCES = 1;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurrentFragmentId;
    private Fragment mCurrentFragment;
    protected AppBarLayout mAppBarLayout;
    private final SimpleArrayMap<String, WeakReference<Fragment>> mFragmentsStack = new SimpleArrayMap<>();
    protected SharedPreferences mSettings;
    protected Toolbar mToolbar;
    private boolean waitingForFragments = false;
    private Menu mMenu;
    private SearchView mSearchView;
    private DatabaseHelper mHelper;
    private MedalLibrary mMedalLibrary;
    private MyBroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        YWMApplication.getInstance().initialise();

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mAppBarLayout.setExpanded(true);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.root_container);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);

        if (savedInstanceState != null) {
            for (Fragment fragment : getSupportFragmentManager().getFragments())
                mFragmentsStack.put(fragment.getTag(), new WeakReference<>(fragment));

            mCurrentFragmentId = savedInstanceState.getInt("current");
            Log.w("test", "mCurrentFragent got from savedInstanceState : " + String.valueOf(mCurrentFragmentId));
            if (mCurrentFragmentId > 0) {
                mNavigationView.setCheckedItem(mCurrentFragmentId);
                String tag = getTag(mCurrentFragmentId);
                if (mFragmentsStack.containsKey(tag)) {
                    mCurrentFragment = mFragmentsStack.get(tag).get();
                    Log.w("test", "fragment " + mCurrentFragment.getClass() + " displayed from stack");
                }
            }
        } else {
            mCurrentFragmentId = mSettings.getInt("fragment_id", R.id.nav_medal_grid);
        }



        //mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MEDAL_LIBRARY_READY);
        filter.addAction(DATABASE_UPDATE_FINISHED);
        filter.addAction(LIST_UPDATED);
        this.registerReceiver(receiver, filter);

        mHelper = DatabaseHelper.getInstance();
        mMedalLibrary = MedalLibrary.getInstance();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleHelper.onAttach(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("test", "in activity result");
        if (requestCode == ACTIVITY_RESULT_PREFERENCES) {
            Log.w("test", "in activity result, result from pref");
            if (resultCode == RESULT_OK) {
                Log.w("test", "got data prof pref intent, langChnaged is : " + data.getBooleanExtra("langageUpdateNeeded", false)
                            + " and fetchUpd is " + String.valueOf(data.getBooleanExtra("updateFetchingNeeded", false)));
                if (data.getBooleanExtra("langageUpdateNeeded", false)) { //recreate activity is language is changed
                    //recreate();   //the navigation menu looses its listener for some reason
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                if (data.getBooleanExtra("updateFetchingNeeded", false))  //fetch update is downloaded content pref changed
                    UpdateHandler.fetchDataUpdate(this);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateHandler.updateFetched = false;
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbar.setBackgroundColor(getResources().getColor(R.color.nice_blue));
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (getCurrentFragment() instanceof IFilterable) {
            MenuItem searchItem = menu.findItem(R.id.search_button);
            mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            mSearchView.setQueryHint("type search");
            mSearchView.setOnQueryTextListener(this);
            MenuItemCompat.setOnActionExpandListener(searchItem, this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return mDrawerToggle.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {

        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mNavigationView.setNavigationItemSelectedListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("test", "onResume, mHelper is" + (mHelper.isReady() ? " " : " not ") + "Ready" + " mMedalLibrary is" + (mMedalLibrary.isInitiated() ? " " : " not ") + "isInitiated");
        //if (!mHelper.isReady()) {

        //}

        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(mCurrentFragmentId);
    }

    @Override
    protected void onResumeFragments() {
        Log.w("test", "onResumeFragments, mCurrentFragment is " + (mCurrentFragment == null ? "" : "not ") + "null");
        super.onResumeFragments();
        if (mCurrentFragment == null)
            showGridFragment(mCurrentFragmentId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNavigationView.setNavigationItemSelectedListener(null);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putInt("fragment_id", mCurrentFragmentId);
        editor.apply();
    }


    protected void onSaveInstanceState(Bundle outState) {
        Log.w("test", "onSaveInstanceState in : " + this.getClass().toString());
        outState.putInt("current", mCurrentFragmentId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        reloadPreferences();
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }
        finish();
    }

    private void reloadPreferences() {
        mCurrentFragmentId = mSettings.getInt("fragment_id", R.id.nav_medal_grid);
    }

    public void restoreCurrentList() {
        Fragment current = getCurrentFragment();
        if (current instanceof IFilterable) {
            ((IFilterable) current).restoreList();
        }
    }

    @NonNull
    private Fragment getNewFragment(int id) {
        switch (id) {
            case R.id.nav_medal_grid:
                return new MedalGridFragment();
            case R.id.nav_product_list:
                return new ProductListFragment();
            case R.id.nav_yokai_grid:
                return new YokaiGridFragment();
            default:
                return new MedalGridFragment();
        }
    }

    @Nullable
    @Override
    public ActionMode startSupportActionMode(@NonNull ActionMode.Callback callback) {
        mAppBarLayout.setExpanded(true);
        return super.startSupportActionMode(callback);
    }

    public void showGridFragment(int id) {
        mNavigationView.setCheckedItem(id);
        Log.w("test", "showGridFragment");
        FragmentManager fm = getSupportFragmentManager();

        while (fm.popBackStackImmediate()); // Clear backstack
        String tag = getTag(id);

        Fragment fragment = null;
        boolean add = false;
        WeakReference<Fragment> wr = mFragmentsStack.get(tag);
        if (wr != null)
            fragment = wr.get();
        if (fragment == null) {
            fragment = getNewFragment(id);
            mFragmentsStack.put(tag, new WeakReference<>(fragment));
            add = true;
        }
        if (mCurrentFragment != null)
            fm.beginTransaction().hide(mCurrentFragment).commit();
        FragmentTransaction ft = fm.beginTransaction();
        if (add)
            ft.add(R.id.fragment_placeholder, fragment, tag);
        else
            ft.show(fragment);
        ft.commit();
        mCurrentFragment = fragment;
        mCurrentFragmentId = id;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if(item == null)
            return false;

        int id = item.getItemId();
        Fragment current = getCurrentFragment();

        if (current == null) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return false;
        }

        if(mCurrentFragmentId == id) { //Already selected
            mDrawerLayout.closeDrawer(mNavigationView);
            return false;
        }

        switch (id){
            case R.id.nav_about:
                mDrawerLayout.closeDrawer(mNavigationView);
                return false;
            case R.id.nav_settings:
                startActivityForResult(new Intent(this, PreferencesActivity.class), ACTIVITY_RESULT_PREFERENCES);
                break;
            default:
                showGridFragment(id);
        }
        mDrawerLayout.closeDrawer(mNavigationView);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String filterQueryString) {
        Log.w("test", "in onQueryTextChange");
        if (filterQueryString.length() < 3)
            return false;
        Fragment current = getCurrentFragment();
        if (current instanceof IFilterable) {
            Log.w("test", "gonna filter");
            ((IFilterable) current).getFilter().filter(filterQueryString);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        //setSearchVisibility(true);
        setTopPanelVisibility(false);
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        //setSearchVisibility(false);
        setTopPanelVisibility(true);
        restoreCurrentList();
        return true;
    }

    private void setTopPanelVisibility(boolean visible){
        Fragment current = getCurrentFragment();
        if (current instanceof IFilterable)
            ((IFilterable) current).setTopPanelVisibility(visible);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private String getTag(int id){
        switch (id){
            case R.id.nav_about:
                return ID_ABOUT;
            case R.id.nav_settings:
                return ID_SETTINGS;
            case R.id.nav_medal_grid:
                return ID_MEDALGRID;
            case R.id.nav_product_list:
                return ID_PRODUCTLIST;
            case R.id.nav_yokai_grid:
                return ID_YOKAIGRID;
            default:
                return ID_MEDALGRID;
        }
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment; //getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
    }

    public static final String MEDAL_LIBRARY_READY = "MEDAL_LIBRARY_READY";
    public static final String DATABASE_UPDATE_FINISHED = "DATABASE_UPDATE_FINISHED";
    public static final String LIST_UPDATED = "LIST_UPDATED";
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("test", "main activity, receiving message");
            String action = intent.getAction();
            switch (action) {
                case MEDAL_LIBRARY_READY:
                    if (getCurrentFragment() instanceof BaseGridFragment) {
                        Log.w("test", "current is MedalGrid so...");
                        ((BaseGridFragment) getCurrentFragment()).onMedalLibraryReady();
                    }
                    break;
                case DATABASE_UPDATE_FINISHED:
                    if (getCurrentFragment() instanceof BaseGridFragment)
                        ((BaseGridFragment) getCurrentFragment()).onDatabaseUpdateFinished();
                    break;
                case LIST_UPDATED:
                    UpdateHandler.fetchDataUpdate(context);
                    break;
            }

        }
    }
}
