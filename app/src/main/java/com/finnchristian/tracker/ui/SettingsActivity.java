package com.finnchristian.tracker.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.finnchristian.tracker.R;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.settings_fragment_container, new SettingsFragment())
                .commit();
    }
}
