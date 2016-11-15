package com.damianmuca.hoymm.kamerkasamochodowa;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Hoymm on 2016-09-24.
 */

public class OverlaySettings extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.overlays_settings);
    }
}