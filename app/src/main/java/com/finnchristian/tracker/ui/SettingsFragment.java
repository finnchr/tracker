package com.finnchristian.tracker.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.finnchristian.tracker.R;

public class SettingsFragment extends PreferenceFragment {

    final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateSummaries();

            // TODO Should update tracker service here
        }
    };

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.fragment_settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        updateSummaries();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    protected void updateSummaries() {
        final String timePreferenceKey = getResources().getString(R.string.pref_gps_time_interval_key);
        ListPreference timePreference = (ListPreference) getPreferenceScreen().findPreference(timePreferenceKey);
        timePreference.setSummary(timePreference.getEntry());

        final String distancePreferenceKey = getResources().getString(R.string.pref_gps_distance_interval_key);
        ListPreference distancePreference = (ListPreference) getPreferenceScreen().findPreference(distancePreferenceKey);
        distancePreference.setSummary(distancePreference.getEntry());
    }
}
