package com.davidpapazian.yokaiwatchmedals.gui.Preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.tools.UpdateHandler;
import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.LocaleHelper;
import com.davidpapazian.yokaiwatchmedals.views.SpinnerPreference;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by David on 08/12/2017.
 */
public class PreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    public final static String CACHE_DIR = YWMApplication.CACHE_DIR;
    public final static String DB_DIR = YWMApplication.DB_DIR;
    public final static String ROOT_URL = YWMApplication.ROOT_URL;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        updateTitle();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.w("test", "onoptionitemselected in fragment");
        ((PreferencesActivity)getActivity()).setResults();
        return super.onOptionsItemSelected(item);
    }

    public void updateTitle() {
        final AppCompatActivity activity = (AppCompatActivity)getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(getTitle());
        }
    }

    public String getTitle() {
        return getString(R.string.title_fragment_general_preferences);
    }

    @Override
    public void onStart() {
        super.onStart();

        String[] appLanguages = getResources().getStringArray(R.array.app_lang_titles);
        String[] otherLanguages =  getResources().getStringArray(R.array.other_lang_titles);
        String[] appLanguageValues = getResources().getStringArray(R.array.app_lang_values);
        String[] otherLanguageValues =  getResources().getStringArray(R.array.other_lang_values);

        SpinnerPreference appLangSpinner = new SpinnerPreference(YWMApplication.getInstance(), appLanguages, appLanguageValues, R.string.title_spinner_app_lang) {
            @Override
            protected void onSpinnerItemSelected(int position) {
                String dbLang = mEntryValues[position];
                LocaleHelper.setLocale(getActivity(), LocaleHelper.dbLangToLocale(dbLang));
                ((PreferencesActivity)getActivity()).langageUpdateNeeded = true;
                MedalLibrary.initialise();
                getActivity().recreate();
            }
            @Override
            protected void setDefaultSelection() {
                mSelection = Arrays.asList(mEntryValues).indexOf(LocaleHelper.getAppLang());
            }
        };
        appLangSpinner.setOrder(2);

        SpinnerPreference medalLangSpinner = new SpinnerPreference(YWMApplication.getInstance(), otherLanguages, otherLanguageValues, R.string.title_spinner_medal_lang) {
            @Override
            protected void onSpinnerItemSelected(int position) {
                String lang = (position == 0) ? LocaleHelper.getAppLang() : mEntryValues[position];
                LocaleHelper.setMedalLang(lang);
                MedalLibrary.initialise();
            }
            @Override
            protected void setDefaultSelection() {
                mSelection = Math.max(Arrays.asList(mEntryValues).indexOf(LocaleHelper.getMedalLang()), 0);
            }
        };
        medalLangSpinner.setOrder(2);

        SpinnerPreference productLangSpinner = new SpinnerPreference(YWMApplication.getInstance(), otherLanguages, otherLanguageValues, R.string.title_spinner_product_lang) {
            @Override
            protected void onSpinnerItemSelected(int position) {
                String lang = (position == 0) ? LocaleHelper.getAppLang() : mEntryValues[position];
                LocaleHelper.setProductLang(lang);
                MedalLibrary.initialise();
            }
            @Override
            protected void setDefaultSelection() {
                mSelection = Math.max(Arrays.asList(mEntryValues).indexOf(LocaleHelper.getProductLang()), 0);
            }
        };
        productLangSpinner.setOrder(2);

        SpinnerPreference yokaiLangSpinner = new SpinnerPreference(YWMApplication.getInstance(), otherLanguages, otherLanguageValues, R.string.title_spinner_yokai_lang) {
            @Override
            protected void onSpinnerItemSelected(int position) {
                String lang = (position == 0) ? LocaleHelper.getAppLang() : mEntryValues[position];
                LocaleHelper.setYokaiLang(lang);
                MedalLibrary.initialise();
            }

            @Override
            protected void setDefaultSelection() {
                mSelection = Math.max(Arrays.asList(mEntryValues).indexOf(LocaleHelper.getYokaiLang()), 0);
            }
        };
        yokaiLangSpinner.setOrder(2);


        getPreferenceScreen().addPreference(appLangSpinner);
        getPreferenceScreen().addPreference(medalLangSpinner);
        getPreferenceScreen().addPreference(productLangSpinner);
        getPreferenceScreen().addPreference(yokaiLangSpinner);

    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences settings, String key) {
        Log.w("test", "a preference changed : " + key);
        switch (key){
            case "medal_500_enabled":
            case "product_500_enabled":
            case "yokai_500_enabled":
                String dir = key.replace("_enabled", "");
                UpdateHandler.updateFetched = false;
                if (settings.getBoolean(key, false)) {
                    ((PreferencesActivity)getActivity()).updateFetchingNeeded = true;
                } else {
                    Log.w("test", "emptying " + dir);
                    //delete images
                    for (File file : (new File(CACHE_DIR + "/" + dir)).listFiles())
                        file.delete();

                    //remove preferences
                    for (Map.Entry<String, ?> entry : settings.getAll().entrySet())
                        if (entry.getKey().startsWith(dir) && entry.getKey().endsWith("downloaded")) {
                            settings.edit().remove(entry.getKey()).apply();
                            Log.w("test", "removing pref " + entry.getKey());
                        }
                }
                break;
            case "only_yokai_on_medals":
                MedalLibrary.initialise();
            default:
                break;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(android.support.v7.preference.Preference preference) {
        switch (preference.getKey()) {
            case "reinitialise_medal_library":
                new AlertDialog.Builder(getPreferenceScreen().getContext()).setTitle("Are you sure you want to re-initialise library ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                MedalLibrary.initialise();
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                            }
                        })
                        .show();
        }
        return super.onPreferenceTreeClick(preference);
    }

    protected void loadFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, fragment)
                .addToBackStack("main")
                .commit();
    }

}