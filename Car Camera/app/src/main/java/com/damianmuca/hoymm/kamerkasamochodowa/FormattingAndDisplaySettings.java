package com.damianmuca.hoymm.kamerkasamochodowa;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Hoymm on 2016-09-24.
 */

public class FormattingAndDisplaySettings extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.formatting_and_display_settings);
    }
}