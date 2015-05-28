package com.finnchristian.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.finnchristian.tracker.model.runkeeper.Token;
import com.google.common.base.Strings;

public class SettingsUtil {
    private static int parseInt(final String value, final int defaultValue) {
        try {
            return Integer.parseInt(value);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }

    private final Context context;

    public SettingsUtil(final Context context) {
        this.context = context;
    }

    public int getGpsDistanceInterval() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = context.getString(R.string.pref_gps_distance_interval_key);
        final String defaultValue = context.getString(R.string.pref_gps_distance_interval_defaultValue);
        final String value = sharedPref.getString(key, defaultValue);

        int distance = parseInt(value, -1);
        if(distance < 0) {
            distance = parseInt(defaultValue, -1);
        }

        return distance;
    }

    public int getGpsTimeInterval() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = context.getString(R.string.pref_gps_time_interval_key);
        final String defaultValue = context.getString(R.string.pref_gps_time_interval_defaultValue);
        final String value = sharedPref.getString(key, defaultValue);

        int time = parseInt(value, -1);
        if(time < 0) {
            time = parseInt(defaultValue, -1);
        }

        return time;
    }

    public Token getRunKeeperToken() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final String tokenTypeKey = context.getString(R.string.pref_runkeeper_token_type_key);
        final String tokenType = sharedPref.getString(tokenTypeKey, null);

        final String accessTokenKey = context.getString(R.string.pref_runkeeper_access_token_key);
        final String accessToken = sharedPref.getString(accessTokenKey, null);

        if(!Strings.isNullOrEmpty(tokenType) && !Strings.isNullOrEmpty(accessToken)) {
            return new Token(tokenType, accessToken);
        }
        else {
            return null;
        }
    }

    public void setRunKeeperToken(final Token token) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();

        final String tokenTypeKey = context.getString(R.string.pref_runkeeper_token_type_key);
        editor.putString(tokenTypeKey, token != null ? token.getTokenType() : null);

        final String accessTokenKey = context.getString(R.string.pref_runkeeper_access_token_key);
        editor.putString(accessTokenKey, token != null ? token.getAccessToken() : null);

        editor.commit();
    }

}
