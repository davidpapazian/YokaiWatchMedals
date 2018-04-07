package com.davidpapazian.yokaiwatchmedals.gui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filter;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.interfaces.IFilterable;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.DatabaseHelper;
import com.davidpapazian.yokaiwatchmedals.tools.ItemComparator;
import com.davidpapazian.yokaiwatchmedals.tools.MultipleSpinnerHandler;

import java.util.ArrayList;
import java.util.Arrays;


public abstract class BaseGridFragment extends BaseFragment implements IFilterable {

    protected MenuItem mSearchButtonView;
    protected View mTopPanelView;
    protected MultipleSpinnerHandler mSpinnerHandler;
    protected Integer[] initialState;
    public int initialPosition = 0;

    public BaseGridFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sMedalLibrary = MedalLibrary.getInstance();
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.w("test", "onActivityCreated for " + this.getClass().toString());

        if (savedInstanceState != null) {
            initialPosition = savedInstanceState.getInt("firstVisiblePosition", -1);
            ArrayList<Integer> initialStateList = savedInstanceState.getIntegerArrayList("currentState");
            if (initialStateList != null) {
                initialState = initialStateList.toArray(new Integer[initialStateList.size()]);
                Log.w("test2", "setting savedstate, tatoeba : " + String.valueOf(initialState[0]));
                mSpinnerHandler.setState(initialState);
            }
        }
        if (MedalLibrary.getInstance().isInitiated()) {
            Log.w("test", "onActivityCreated, library ready for restoring");
            mAdapter.setOriginalList(); //restoreList();
            mSpinnerHandler.restoreSpinners();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mSearchButtonView = menu.findItem(R.id.search_button);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_code:
                mAdapter.sortBy(ItemComparator.SORT_BY_CODE);
                return true;
            case R.id.sort_by_name:
                mAdapter.sortBy(ItemComparator.SORT_BY_NAME);
                return true;
            case R.id.sort_by_date:
                mAdapter.sortBy(ItemComparator.SORT_BY_DATE);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        //if (sMedalLibrary.isInitiated())
        //    onMedalLibraryReady();
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.w("test", "onSaveInstanceState in : " + this.getClass().toString());
        outState.putInt("firstVisiblePosition", mLayoutManager.findFirstVisibleItemPosition());
        outState.putIntegerArrayList("currentState", new ArrayList<>(Arrays.asList(mSpinnerHandler.getCurrentState())));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            updateTitle();
        }
    }

    public void updateTitle() {
        final AppCompatActivity activity = (AppCompatActivity)getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(getTitle());
        }
    }

    protected abstract String getTitle();

    public void onMedalLibraryReady() {
        if (!isHidden() && mAdapter.isEmpty()) {
            mHandler.sendEmptyMessage(UPDATE_LIST);
            Log.w("test", "sending UPTADE_LIST message from onMedalLibraryReady");
        }
    }

    public void onDatabaseUpdateFinished() {
        if (!isHidden()) {
            mHandler.sendEmptyMessage(UPDATE_LIST);
            Log.w("test", "sending UPTADE_LIST message from onDatabaseUpdateFinished");
        }
    }

    public static final int UPDATE_LIST = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LIST:
                    removeMessages(UPDATE_LIST);
                    mAdapter.setOriginalList(); //restoreList();
                    mSpinnerHandler.restoreSpinners();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    public boolean enableSearchOption() {
        return true;
    }

    @Override
    public Filter getFilter() {
        return mAdapter.getFilter();
    }

    @Override
    public void restoreList() {
        mAdapter.restoreList();
    }

    @Override
    public void setTopPanelVisibility(boolean visible) {
        mTopPanelView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public RecyclerView getRV() {
        return mGridView;
    }

    public LinearLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    protected void restoreSpinners() {
        mSpinnerHandler.restoreSpinners();
    }

    protected void onListUpdated() {
        if (initialPosition != -1) {
            mLayoutManager.scrollToPosition(initialPosition);
            initialPosition = -1;
        }
    }
}
