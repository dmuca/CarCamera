package com.damianmuca.hoymm.kamerkasamochodowa;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;

import java.util.List;

public class SettingsActivity extends PreferenceActivity{
    //public static Context settingsContext;


    static private String subclassClickedName;
    // General Entries
    public SettingsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        subclassClickedName = header.fragment;
        super.onHeaderClick(header, position);
    }

    // isVaildFragment must be override when using on API > 19, otherwise attempt to open settings will crash an application
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return subclassClickedName.equals(fragmentName);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        //settingsContext = getBaseContext();
        loadHeadersFromResource(R.xml.headers, target);
    }
}
