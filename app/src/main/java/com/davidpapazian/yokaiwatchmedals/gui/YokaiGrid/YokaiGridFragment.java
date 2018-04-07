package com.davidpapazian.yokaiwatchmedals.gui.YokaiGrid;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
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

public class YokaiGridFragment extends BaseGridFragment {

    private final static int SPACING = 3; //spacing in dp
    private final static int PADDING = 3; //padding in dp

    public YokaiGridFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new YokaiGridAdapter(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_yokai_grid, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public String getTitle() {
        return getString(R.string.title_yokai_grid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        updateTitle();
        View view =  inflater.inflate(R.layout.fragment_yokai_grid, container, false);
        mGridView = (RecyclerView) view.findViewById(R.id.list);
        mTopPanelView = view.findViewById(R.id.top_pannel);
        int column = landscape ? 5 : 3;
        mLayoutManager = new GridLayoutManager(getActivity(), column);

        int width = (int) Math.floor( (Utils.getWidthPx() - Utils.dpToPx(2*PADDING + (column-1)*SPACING)) / column );
        mAdapter.setImageLoader(new ImageLoader(getActivity(), "yokai", width, width));

        mGridView.setLayoutManager(mLayoutManager);
        mGridView.addItemDecoration(new SpaceItemDecoration(Utils.dpToPx(3)));
        mGridView.setAdapter(mAdapter);
        createSpinners(view);
        return view;
    }

    private void createSpinners(View view) {
        Spinner[] spinners = new Spinner[3];
        spinners[0] = (Spinner) view.findViewById(R.id.spinner_yokai_game).findViewById(R.id.spinner);
        spinners[1] = (Spinner) view.findViewById(R.id.spinner_yokai_tribe).findViewById(R.id.spinner);
        spinners[2] = (Spinner) view.findViewById(R.id.spinner_yokai_type).findViewById(R.id.spinner);
        AutoFitTextView[] spinnerTitles = new AutoFitTextView[3];
        spinnerTitles[0] = (AutoFitTextView) view.findViewById(R.id.spinner_yokai_game).findViewById(R.id.text);
        spinnerTitles[1] = (AutoFitTextView) view.findViewById(R.id.spinner_yokai_tribe).findViewById(R.id.text);
        spinnerTitles[2] = (AutoFitTextView) view.findViewById(R.id.spinner_yokai_type).findViewById(R.id.text);
        String[] spinnerNames = new String[]{"YokaiGame", "YokaiTribe", "YokaiType"};
        mSpinnerHandler = new MultipleSpinnerHandler(getActivity(), spinners, spinnerNames, spinnerTitles, (initialState == null ? new Integer[]{0,0,0} : initialState)) {

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
                return MedalLibrary.getInstance().getYokaiFilterStates();
            }

            @Override
            public void onSpinnerUpdateFinished(Integer[] currentState) {
                if (mSearchButtonView != null) {
                    mSearchButtonView.setVisible(currentState[0] == 0 && currentState[1] == 0 && currentState[2] == 0);
                }
            }
        };
    }
}