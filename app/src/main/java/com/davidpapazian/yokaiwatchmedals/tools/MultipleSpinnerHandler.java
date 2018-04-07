package com.davidpapazian.yokaiwatchmedals.tools;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class MultipleSpinnerHandler {

    private int NB_SPINNERS;
    private Integer[] currentState;
    private int dropDownViewResId;
    private int selectedViewResId;
    private Context context;
    private SingleSpinnerAdapter[] spinnerAdapters;
    private Spinner[] spinners;
    private String[] spinnerNames;
    private boolean[] initialised;
    private boolean[] hasChanged;
    private AutoFitTextView[] spinnerTitles;
    private boolean allInitialised;

    public MultipleSpinnerHandler(Context context, Spinner[] spinners, String[] spinnerNames, AutoFitTextView[] spinnerTitles, Integer[] initialState) {
        this.context = context;
        this.spinners = spinners;
        this.spinnerNames = spinnerNames;
        this.spinnerTitles = spinnerTitles;
        this.currentState = initialState;
        initialise();
    }

    private void initialise() {
        dropDownViewResId = setDropDownViewResId();
        selectedViewResId = setSelectedViewResId();
        NB_SPINNERS = spinners.length;
        spinnerAdapters = new SingleSpinnerAdapter[NB_SPINNERS];
        initialised = new boolean[NB_SPINNERS];
        hasChanged = new boolean[NB_SPINNERS];
        Spinner spinner;
        SingleSpinnerAdapter spinnerAdapter;
        for(int i = 0; i<NB_SPINNERS; i++) {
            spinner = spinners[i];
            initialised[i] = false;
            hasChanged[i] = false;
            spinnerAdapter = new SingleSpinnerAdapter(i);
            spinnerTitles[i].setText(getSpinnerTitle(i), 20);
            spinner.setAdapter(spinnerAdapter);
            Log.w("test", "setting listener to spinner");
            spinner.setOnItemSelectedListener(spinnerAdapter);
            spinnerAdapters[i] = spinnerAdapter;
        }
        //initialiseCurrentState();
    }

    public Integer[] getCurrentState() {
        return currentState;
    }

    /*
    public void initialiseSavedState() {
        Log.w("test", "initialiseCurrentState");

        if (!isCurrentStateDefault()) {    //update to saved state
            for (int i=0; i<NB_SPINNERS; i++)
                spinners[i].setSelection(currentState[i]);
        }
        filterResultsFromState(currentState);
    }
    */

    public void setState(Integer[] state) {
        currentState = state;
    }

    private void onSpinnerItemSelectedByUser(int id, int spinner) {
        boolean sameState = currentState[spinner] == id;
        currentState[spinner] = id;

        if (initialised[spinner])
            filterResultsFromState(currentState); //Log.w("test", "old case");
        else
            initialised[spinner] = true;

        if (sameState)
            return;

        Map<Integer, ArrayList<Integer>> updates = new HashMap<>();

        for (int i=0; i<NB_SPINNERS; i++) {
            if (i != spinner) {
                if (currentState[i] == 0) {
                    updates.put(i, getItemFilterResult(currentState, i));
                } else {
                    int old = currentState[i];
                    currentState[i] = 0;
                    updates.put(i, getItemFilterResult(currentState, i));
                    currentState[i] = old;
                }
            }
        }

        for (int i : updates.keySet()) {
            Log.w("test", "updating spinners, from onSpinnerItemSelectedByUser");
            updateSpinner(updates.get(i), i);
        }
    }

    private void test(int id, int spinner) {
        Log.w("test5", "in test, spinner = " + String.valueOf(spinner) + ", id = " + String.valueOf(id));

        if (!initialised[spinner]) {
            Log.w("test5", "in test, spinner = " + String.valueOf(spinner) + ", id = " + String.valueOf(id) + ", initialised[spinner] false");
            initialised[spinner] = true;
            allInitialised = true;
            for (boolean b : initialised)
                allInitialised = b && allInitialised;
        }

        if (allInitialised) {
            //if (currentState[spinner] == id)
            //    return;
            currentState[spinner] = id;
            Log.w("test5", "in test, spinner = " + String.valueOf(spinner) + ", id = " + String.valueOf(id) + ", initialised[spinner] true");
            filterResultsFromState(currentState);
            Map<Integer, ArrayList<Integer>> updates = new HashMap<>();
            for (int i = 0; i < NB_SPINNERS; i++) { //update each spinner as if spinner is not selected but others are
                int old = currentState[i];
                currentState[i] = 0;
                updates.put(i, getItemFilterResult(currentState, i));
                currentState[i] = old;
            }
            for (int i : updates.keySet())
                updateSpinner(updates.get(i), i);
        }
    }

    private ArrayList<Integer> getItemFilterResult(Integer[] newState, int spinner) {
        Set<Integer> list = new HashSet<>();
        for (Integer[] state : getFilterStates()) {
            boolean toAdd = true;
            for (int i=0; i<NB_SPINNERS; i++)
                if(!(newState[i] == 0 || state[i] == newState[i])) {
                    toAdd = false;
                    break;
                }
            if (toAdd)
                list.add(state[spinner]);
        }
        ArrayList<Integer> newList = new ArrayList<>();
        newList.add(0);
        newList.addAll(list);
        return new ArrayList<>(newList);
    }

    private void updateSpinner(final ArrayList<Integer> list, final int spinnerId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                spinnerAdapters[spinnerId].update(list, false);
            }
        }).start();
    }

    private class SingleSpinnerAdapter extends ArrayAdapter<Integer> implements AdapterView.OnItemSelectedListener {
        private ArrayList<Integer> mDataSet = new ArrayList<>();
        private final ExecutorService mUpdateExecutor = Executors.newSingleThreadExecutor();
        private final FilterComparator sFilterComparator = new FilterComparator();

        private int spinnerId;

        public SingleSpinnerAdapter(int spinnerId) {
            super(context,  R.layout.spinner_item_small);
            this.spinnerId = spinnerId;
        }

        @Override
        public int getCount() {
            return mDataSet.size();
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //Log.w("test5", "getDropDownView for spinner : " + String.valueOf(spinnerId) + ", position = " + String.valueOf(position) + " and : hasChanged[spinnerId] : " + String.valueOf(hasChanged[spinnerId]));
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(dropDownViewResId, null);
            }
            else {view = convertView;}
            return getCustomView(position, view);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(selectedViewResId, null);
            }
            else {view = convertView;}
            //mDataSet.indexOf(currentState[spinnerId])
            return getCustomView(position, view);
        }

        public View getCustomView(int position, View view) {
            int id = mDataSet.get(position);
            ImageView imageView = (ImageView) view.findViewById(getItemImageViewId());
            int imageId = context.getResources().getIdentifier(getDrawableName(id, spinnerId), "drawable", context.getPackageName());
            String name = getName(id, spinnerNames[spinnerId]);
            imageView.setImageResource(imageId);
            ((AutoFitTextView) view.findViewById(getItemTextViewId())).setText(name, 20);
            return view;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.w("test", "onItemSelected inside, spinner : " + String.valueOf(spinnerId) + ", for id " + String.valueOf(i));
            //onSpinnerItemSelectedByUser(mDataSet.get(i), spinnerId);
            test(mDataSet.get(i), spinnerId);
        }

        public void update(final ArrayList<Integer> items, final boolean initialisation) {
            mUpdateExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Collections.sort(items, sFilterComparator);
                    final ArrayList<Integer> newList = new ArrayList<>();
                    newList.addAll(items);
                    YWMApplication.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            mDataSet = new ArrayList<>(items);
                            //hasChanged[spinnerId] = true;
                            spinners[spinnerId].setSelection(mDataSet.indexOf(currentState[spinnerId])); // reselect the right position
                            notifyDataSetChanged();
                            onSpinnerUpdateFinished(currentState);
                        }
                    });
                }
            });
        }

        public ArrayList<Integer> getDataSet() {
            return mDataSet;
        }
    }

    private static class FilterComparator implements Comparator<Integer> {

        public FilterComparator(){
        }

        @Override
        public int compare(Integer i, Integer j){
            return i - j;
        }
    }

    //CAN BE OVERRIDEN
    public int setDropDownViewResId() {
        return context.getResources().getIdentifier("spinner_item_small", "layout", context.getPackageName());
    }

    //CAN BE OVERRIDEN
    public int setSelectedViewResId() {
        return context.getResources().getIdentifier("spinner_item_small_down", "layout", context.getPackageName());
    }

    //CAN BE OVERRIDEN
    public String getSpinnerTitle(int spinnerId) {
        return context.getString(context.getResources().getIdentifier("spinner_" + spinnerNames[spinnerId].toLowerCase(Locale.ENGLISH) + "_title", "string", context.getPackageName()));
    }

    //CAN BE OVERRIDEN
    public String getDrawableName(int id, int spinnerId) {
        return spinnerNames[spinnerId].toLowerCase(Locale.ENGLISH) + "_" + String.format(Locale.ENGLISH, "%02d", id);
    }

    //CAN BE OVERRIDEN
    public int getItemImageViewId() {
        return context.getResources().getIdentifier("image", "id", context.getPackageName());
    }

    //CAN BE OVERRIDEN
    public int getItemTextViewId() {
        return context.getResources().getIdentifier("text", "id", context.getPackageName());
    }

    //TO OVERRIDE
    public abstract String getName(int id, String spinnerName);

    //TO OVERRIDE
    public abstract void filterResultsFromState(Integer[] currentState);

    //TO OVERRIDE
    public abstract ArrayList<Integer[]> getFilterStates();

    //TO OVERRIDE
    public abstract ArrayList<Integer> getSpinnerOriginalLists(String spinnerName);

    //TO OVERRIDE
    public abstract void onSpinnerUpdateFinished(Integer[] currentState);

    //CAN BE CALLED
    public final void restoreSpinners() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w("test", "updating spinners, from restoreSpinners");
                for (int i=0; i<NB_SPINNERS; i++) {
                    spinnerAdapters[i].update(getSpinnerOriginalLists(spinnerNames[i]), true);
                }
            }
        }).start();
    }

    private boolean isCurrentStateDefault() {
        boolean def = true;
        for (int i : currentState)
            def = (def && (i == 0));
        return def;
    }
}
