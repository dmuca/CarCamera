package com.damianmuca.hoymm.kamerkasamochodowa;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;

import java.util.List;

public class SettingsActivity extends PreferenceActivity{
    public static Context settingsContext;
    // General Entries
    public SettingsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        settingsContext = getBaseContext();
        loadHeadersFromResource(R.xml.headers, target);
    }
}
