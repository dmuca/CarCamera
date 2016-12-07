package com.damianmuca.hoymm.kamerkasamochodowa;

import android.preference.PreferenceActivity;

import java.util.List;

public class SettingsActivity extends PreferenceActivity{

    // General Entries
    public SettingsActivity() {
        // Required empty public constructor
    }


    @Override
    protected boolean isValidFragment(String fragmentName) {
        boolean result = false;

        // check if invoked class exists
        for (String checkingString : StaticValues.mySubsettingsClassesNamesArray)
            if (checkingString.equals(fragmentName))
                result = true;
        return result;
    }


    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers, target);
        // initializate SUBSETTINGS NAMES array, so later I can use it inside isValidFragment method
        StaticValues.mySubsettingsClassesNamesArray = new String [target.size()];
        for (int i = 0; i < target.size(); ++i)
            StaticValues.mySubsettingsClassesNamesArray[i] = target.get(i).fragment;
    }
}
