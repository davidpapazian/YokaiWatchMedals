package com.davidpapazian.yokaiwatchmedals.gui.Preferences;

import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.davidpapazian.yokaiwatchmedals.R;
import com.davidpapazian.yokaiwatchmedals.tools.LocaleHelper;

public class PreferencesActivity extends AppCompatActivity {

    private AppBarLayout mAppBarLayout;
    public boolean updateFetchingNeeded = false;
    public boolean langageUpdateNeeded = false;
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_preferences);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        Log.w("test", "pref activity create, savedstate null : " + String.valueOf(savedInstanceState == null));

        if (savedInstanceState!=null) {
            updateFetchingNeeded = savedInstanceState.getBoolean("updateFetchingNeeded", false);
            langageUpdateNeeded = savedInstanceState.getBoolean("langageUpdateNeeded", false);
        }
        //if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, new PreferencesFragment())
                    .commit();
        //}
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleHelper.onAttach(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setResults();
        Log.w("test", "oois activity");

        if (item.getItemId() == android.R.id.home) {
            if (!getSupportFragmentManager().popBackStackImmediate())
                finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("updateFetchingNeeded", updateFetchingNeeded);
        outState.putBoolean("langageUpdateNeeded", langageUpdateNeeded);
        Log.w("test", "setting result in onSaveInstanceState in PreferencesActivity");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        setResults();
        Log.w("test", "in onStop in preferencesactivity");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        setResults();
        Log.w("test", "in onBackPressed in preferencesactivity");
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbar.setBackgroundColor(getResources().getColor(R.color.nice_blue));
        return super.onCreateOptionsMenu(menu);
    }

    public void setResults() {
        Intent intent = new Intent();
        intent.putExtra("updateFetchingNeeded", updateFetchingNeeded);
        intent.putExtra("langageUpdateNeeded", langageUpdateNeeded);
        setResult(RESULT_OK, intent);
    }
}
