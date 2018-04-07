package com.davidpapazian.yokaiwatchmedals.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.davidpapazian.yokaiwatchmedals.R;

import java.util.Arrays;

public abstract class SpinnerPreference2 extends Preference {
    private String[] mEntries;
    public String[] mEntryValues;
    private boolean initialised = false;
    public int mSelection = -1;
    private Context context;
    private Spinner mSpinner;
    private MySpinnerAdapter mAdapter;
    private int titleResId;
    private Runnable defaultSelector;

    public SpinnerPreference2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinnerPreference2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setWidgetLayoutResource(R.layout.preference_spinner);
        getDefaultSelection();
    }

    public void setEntries(String[] entries) {
        mEntries = entries;
    }

    public void setEntryValues(String[] entryValues) {
        mEntries = entryValues;
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mSpinner = (Spinner) holder.findViewById(R.id.spinner);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpinner.performClick();
            }
        });
        mAdapter = new MySpinnerAdapter();
        mSpinner.setAdapter(mAdapter);
        mSpinner.setSelection(mSelection);
        mSpinner.setOnItemSelectedListener(mAdapter);
    }

    private class MySpinnerAdapter extends ArrayAdapter<String> implements AdapterView.OnItemSelectedListener {

        public MySpinnerAdapter() {
            super(context,  R.layout.spinner_item_small, R.id.text);
        }

        @Override
        public int getCount() {
            return mEntries.length;
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
            String title = mEntries[position];
            ((AutoFitTextView) view.findViewById(R.id.text)).setText(title, 20);
            return view;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.w("test", "onItemSelected, pos : " + String.valueOf(i) + " while initialised is " + String.valueOf(initialised));
            if (initialised) {
                mSelection = i;
                onSpinnerItemSelected(i);
            } else {
                initialised = true;
            }
        }
    }

    protected abstract void onSpinnerItemSelected(int position);

    public void setDefaultSelection(Runnable defaultSelector) {
        this.defaultSelector = defaultSelector;
    }

    public void getDefaultSelection() {
        defaultSelector.run();
    }
}
