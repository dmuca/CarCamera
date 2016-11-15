package com.damianmuca.hoymm.kamerkasamochodowa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by Hoymm on 2016-09-19.
 */

public class GeneralSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private String [] backButtonEntries, gpsSatellitesEntries;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        backButtonEntries = getResources().getStringArray(R.array.e_back_button);
        gpsSatellitesEntries = getResources().getStringArray(R.array.e_gps_satellite_types);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        Preference connectionPref = findPreference(key);
        // use GPS satellites settings
        if (key.equals(getResources().getString(R.string.SP_general_gps_settings))){
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(gpsSatellitesEntries[Integer.parseInt(sharedPreferences.getString(key, "0"))]);
        }
        // CheckBoxPreference if use app own brightness
        else if (key.equals(getResources().getString(R.string.SP_general_brightness_seek_bar))){
            if (sharedPreferences.getBoolean(key, false))
                connectionPref.setSummary(R.string.may_not_work_if_auto_brightness);
            else
                connectionPref.setSummary(R.string.defaultt);
        }
        // Seekbar for app own brightness
        else if (key.equals(getResources().getString(R.string.SP_general_brightness_seek_bar_intensity))){

        }
        else if (key.equals(getResources().getString(R.string.SP_general_screen_locked))){

        }
        else if (key.equals(getResources().getString(R.string.SP_general_back_button))){
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(backButtonEntries[Integer.parseInt(sharedPreferences.getString(key, "0"))]);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        // set GPS satellites SETTING summary (describe)
        String satellitesGPSValueString = sharedPref.getString(getResources().getString(R.string.SP_general_gps_settings), "0");
        Preference satellitesGPSPreference = findPreference(getResources().getString(R.string.SP_general_gps_settings));
        satellitesGPSPreference.setSummary(gpsSatellitesEntries[Integer.parseInt(satellitesGPSValueString)]);

        // set summary for CheckBoxPreference BRIGHTNESS settings
        Preference checkBoxBrightnessPreference = findPreference(getResources().getString(R.string.SP_general_brightness_seek_bar));
        if (sharedPref.getBoolean(getResources().getString(R.string.SP_general_brightness_seek_bar),false))
            checkBoxBrightnessPreference.setSummary(R.string.may_not_work_if_auto_brightness);
        else
            checkBoxBrightnessPreference.setSummary(R.string.defaultt);

        // set BACK BUTTON SETTING summary (describe)
        String backButtonValueString = sharedPref.getString(getResources().getString(R.string.SP_general_back_button), "0");
        Preference backButtonPreference = findPreference(getResources().getString(R.string.SP_general_back_button));
        backButtonPreference.setSummary(backButtonEntries[Integer.parseInt(backButtonValueString)]);

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
