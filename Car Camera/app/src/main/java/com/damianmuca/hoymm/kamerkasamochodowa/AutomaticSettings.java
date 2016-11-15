package com.damianmuca.hoymm.kamerkasamochodowa;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Hoymm on 2016-09-19.
 */

public class AutomaticSettings extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.automatic_processes_settings);
    }
}
