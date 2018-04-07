package com.davidpapazian.yokaiwatchmedals.gui.ProductList;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.BaseGridFragment;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.ImageLoader;
import com.davidpapazian.yokaiwatchmedals.tools.MultipleSpinnerHandler;
import com.davidpapazian.yokaiwatchmedals.tools.Utils;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;
import com.davidpapazian.yokaiwatchmedals.views.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.Locale;

public class ProductListFragment extends BaseGridFragment {

    public ProductListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ProductListAdapter(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_product_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public String getTitle() {
        return getString(R.string.title_product_list);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        updateTitle();
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        mGridView = (RecyclerView) view.findViewById(R.id.list);
        mGridView.getItemAnimator().setChangeDuration(0);
        mTopPanelView = view.findViewById(R.id.top_pannel);
        mLayoutManager = new LinearLayoutManager(getActivity());

        int width = Utils.dpToPx(80);
        mAdapter.setImageLoader(new ImageLoader(getActivity(), "product", width, width));

        mGridView.setLayoutManager(mLayoutManager);
        mGridView.addItemDecoration(new SpaceItemDecoration(Utils.dpToPx(3)));
        mGridView.setAdapter(mAdapter);
        createSpinners(view);
        return view;
    }

    private void createSpinners(View view) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            ((LinearLayout) view.findViewById(R.id.spinners)).setOrientation(LinearLayout.HORIZONTAL);

        Spinner[] spinners = new Spinner[1];
        spinners[0] = (Spinner) view.findViewById(R.id.spinner_product_type).findViewById(R.id.spinner);
        AutoFitTextView[] spinnerTitles = new AutoFitTextView[1];
        spinnerTitles[0] = (AutoFitTextView) view.findViewById(R.id.spinner_product_type).findViewById(R.id.text);
        String[] spinnerNames = new String[]{"ProductType"};
        mSpinnerHandler = new MultipleSpinnerHandler(getActivity(), spinners, spinnerNames, spinnerTitles, (initialState == null ? new Integer[]{0} : initialState)) {

            @Override
            public void filterResultsFromState(Integer[] currentState) {
                mAdapter.getSpinnerFilter().filter(currentState);
            }

            @Override
            public ArrayList<Integer> getSpinnerOriginalLists(String spinnerName) {
                ArrayList<Integer> list = new ArrayList<>();
                for (int i=0; i<MedalLibrary.getInstance().getAttributeNames(spinnerName).size(); i++)
                    list.add(i);
                return list;
            }

            @Override
            public String getName(int id, String spinnerName) {
                return MedalLibrary.getInstance().getAttributeNames(spinnerName).get(id);
            }

            @Override
            public ArrayList<Integer[]> getFilterStates() {
                return MedalLibrary.getInstance().getProductFilterStates();
            }

            @Override
            public void onSpinnerUpdateFinished(Integer[] currentState) {
                if (mSearchButtonView != null) {
                    mSearchButtonView.setVisible(currentState[0] == 0);
                }
            }
        };
    }
}
