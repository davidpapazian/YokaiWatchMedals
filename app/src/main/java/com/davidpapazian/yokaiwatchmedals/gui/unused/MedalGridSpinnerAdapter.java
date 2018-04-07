package com.davidpapazian.yokaiwatchmedals.gui.unused;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.gui.MedalGrid.MedalGridFragment;
import com.davidpapazian.yokaiwatchmedals.views.AutoFitTextView;
import com.davidpapazian.yokaiwatchmedals.YWMApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedalGridSpinnerAdapter extends ArrayAdapter<String> implements AdapterView.OnItemSelectedListener {

    private ArrayList<Integer> mDataSet = new ArrayList<>();
    private final ExecutorService mUpdateExecutor = Executors.newSingleThreadExecutor();
    private static final FilterComparator sFilterComparator = new FilterComparator();

    private Context context;
    private String spinnerName;
    private int spinnerId;
    private MedalGridFragment fragment;



    public MedalGridSpinnerAdapter(Context context, int spinnerId, MedalGridFragment fragment) {
        super(context,  R.layout.spinner_item_small, R.id.text);
        this.context = context;
        this.fragment = fragment;
        this.spinnerId = spinnerId;
        this.spinnerName = "";//MedalGridFragment.getFilterTable(spinnerId);
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.spinner_item_small, null);
        }
        else {view = convertView;}
        return getCustomView(position, view);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup prnt) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.spinner_item_small_down, null);
        }
        else {view = convertView;}
        return getCustomView(position, view);
    }

    public View getCustomView(int position, View view) {
        int id = mDataSet.get(position);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        String word = "medal_" + spinnerName.toLowerCase() + "_" + String.format(Locale.ENGLISH, "%02d", id);
        int imageId = context.getResources().getIdentifier("com.davidpapazian.yokaiwatchmedals:drawable/" + word, null, null);
        String name = context.getString(context.getResources().getIdentifier("com.davidpapazian.yokaiwatchmedals:string/" + word, null, null));
        imageView.setImageResource(imageId);
        ((AutoFitTextView) view.findViewById(R.id.text)).setText(name, 20);
        return view;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        //fragment.onSpinnerItemSelected(mDataSet.get(i), spinnerId);
    }

    public void update(final ArrayList<Integer> items) {
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
                        notifyDataSetChanged();
                        //fragment.onSpinnerUpdateFinished(spinnerId);
                    }
                });
            }
        });
    }

    public ArrayList<Integer> getDataSet() {
        return mDataSet;
    }

    private static class FilterComparator implements Comparator<Integer> {

        public FilterComparator(){
        }

        @Override
        public int compare(Integer i, Integer j){
            return i - j;
        }
    }
}