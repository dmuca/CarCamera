package com.damianmuca.hoymm.kamerkasamochodowa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.damianmuca.hoymm.kamerkasamochodowa.MainActivity.context;
import static com.damianmuca.hoymm.kamerkasamochodowa.MainActivity.getCameraInstance;

/**
 * Created by Hoymm on 2016-09-19.
 */

public class PhotoSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Camera myCameraObj;
    private List<String> photoQualityEntries_L, focusModesEntries_L;
    private SharedPreferences sharedPref;

    // Preference Frequency Of Shooting pictures (in seconds, String type)
    private Preference snapshotFrequencyP, focusModesP;

    // resolutions LIST as SIZE - of elements type
    private List <Camera.Size> sortedResolutionsL;

    boolean photoQualitySupported = true, focusModesSupported = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.photo_settings);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        initializateDialogForSnapshotFrequence();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // init camera
        initializateMyCameraObj();

        setEntriesAndEntryValues();
        // SUMMARIES
        refreshSummaries();
    }

    private void initializateDialogForSnapshotFrequence() {

        Preference snapshotFrequenceSP = findPreference(getResources().getString(R.string.SP_photo_snapshot_frequency));
        snapshotFrequenceSP.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {



                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.set_snapshot_frequency_in_sec);

                // Set up the input
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // check if input is INTEGER NUMBER
                        if (MainActivity.isNumeric(input.getText().toString())) {
                            // it cannot be lesser than 2 sec (too fast photo shooting)
                            if (Integer.parseInt(input.getText().toString()) < 1) {
                                Toast.makeText(getActivity(), R.string.minumum_frequency_is_1_sec
                                        , Toast.LENGTH_LONG).show();
                            }
                            // everything is ok, set new data
                            else {
                                SharedPreferences.Editor edit = sharedPref.edit();
                                edit.putString(getResources().getString(R.string.SP_photo_snapshot_frequency)
                                        , input.getText().toString());
                                edit.apply();

                                // Refresh Summary
                                snapshotFrequencyP.setSummary(Integer.parseInt(sharedPref.getString(getResources().getString
                                        (R.string.SP_photo_snapshot_frequency), "30")) + getString(R.string._seconds));


                                // show warning
                                if (Integer.parseInt(input.getText().toString()) < 3) {

                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(getActivity());
                                    builder.setMessage(R.string.warrning_fast_frequency_might_cause_problems)
                                            .setCancelable(false)
                                            .setIcon(android.R.drawable.stat_sys_warning)
                                            .setPositiveButton(R.string.ok_im_aware_of_danger, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).create().show();
                                }
                            }
                        }
                        // improper format
                        else{
                            Toast.makeText(getActivity(), R.string.wrong_format_insert_integer_numb
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        releaseCamera();              // release the camera immediately on pause event
    }


    private void releaseCamera(){
        if (myCameraObj != null){
            myCameraObj.release();        // release the camera for other applications
            myCameraObj = null;
        }
    }


    private void refreshSummaries() {
        // SUMMARIES
        String photoResolution = sharedPref.getString(getResources().getString(R.string.SP_photo_resolution), "default");
        Preference photoQualityP = findPreference(getResources().getString(R.string.SP_photo_resolution));
        // Photo Quality
        if (!photoResolution.equals("default")) {
            photoQualityP.setSummary(photoQualityEntries_L.get(Integer.parseInt(photoResolution)));
        }
        else{
            photoQualityP.setSummary("1280x720 + ("
                    + Math.round((1280*720)/100000.0)/10.0 + " MP)");
        }

        // Snapshot Frequency
        snapshotFrequencyP = findPreference(getResources().getString(R.string.SP_photo_snapshot_frequency));
        snapshotFrequencyP.setSummary(Integer.parseInt(sharedPref.getString
                (getResources().getString(R.string.SP_photo_snapshot_frequency),"30")) + getString(R.string._seconds));


        // Focus Mode
        focusModesP = findPreference(getResources().getString(R.string.SP_photo_focus_mode));
        focusModesP.setSummary(focusModesEntries_L.get(Integer.parseInt(sharedPref.getString(getResources()
                        .getString(R.string.SP_photo_focus_mode), getString(R.string.defaultt)))));
    }

    private void setEntriesAndEntryValues() {
        // Photo Resolution
        setEntriesAndEntryValuesForPhotoResolution();
    }

    private void setEntriesAndEntryValuesForPhotoResolution() {
        // #### ENTRIES and ENTRY VALUES ###

        // VIDEO QUALITY set Entries and Entry Values
        try {
            sortedResolutionsL = MainActivity.getPhotoSizesL();

            photoQualityEntries_L = new ArrayList<>();
            // add specified sizes (1920x1080 ect.)
            for (Camera.Size size : sortedResolutionsL){
                    photoQualityEntries_L.add(size.width + "x" + size.height + " ("
                            + Math.round((size.width*size.height)/100000.0)/10.0 + " MP)");
            }
            photoQualitySupported = true;


            ListPreference photoQualityP = (ListPreference) findPreference(getResources().getString(R.string.SP_photo_resolution));
            // entries (E)
            photoQualityP.setEntries(photoQualityEntries_L.toArray(new CharSequence[photoQualityEntries_L.size()]));

            // entry values (EV)
            CharSequence [] photoQualityEV_CQ = new CharSequence[photoQualityEntries_L.size()];
            for (int i = 0; i < photoQualityEV_CQ.length; ++i)
                photoQualityEV_CQ [i] =  String.valueOf(i);
            photoQualityP.setEntryValues(photoQualityEV_CQ);
        }
        catch (NullPointerException e){
            // NULL if NOT supported
            photoQualitySupported = false;
        }


        //---
        // FOCUS MODE set Entries and Entry Values
        try {
            focusModesEntries_L = new ArrayList<>();
            focusModesEntries_L = myCameraObj.getParameters().getSupportedFocusModes();
            focusModesSupported = true;



            ListPreference localfocusModesP = (ListPreference) findPreference(getResources().getString(R.string.SP_photo_focus_mode));
            // entries (E)
            localfocusModesP.setEntries(focusModesEntries_L.toArray(new CharSequence[focusModesEntries_L.size()]));

            // entry values (EV)
            CharSequence [] focusModesEV_CQ = new CharSequence[focusModesEntries_L.size()];
            for (int i = 0; i < focusModesEV_CQ.length; ++i)
                focusModesEV_CQ [i] =  String.valueOf(i);
            localfocusModesP.setEntryValues(focusModesEV_CQ);
        }
        catch (NullPointerException e){
            // NULL if NOT supported
            focusModesSupported = false;
        }

    }

    private void initializateMyCameraObj() {

        try {
            myCameraObj = getCameraInstance();

            //Toast.makeText(context, sharedPref.getString(getResources().getString(R.string.SP_camera_mode), "0")
            // , Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            //Toast.makeText(this, "Camera is currently used by another application", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference currentPreference = findPreference(key);
        // isAdded() prevents crash when changing values few times
        if (isAdded()) {
            synchronized (this) {
                // change PHOTO RESOLUTION Summary
                if (key.equals(getResources().getString(R.string.SP_photo_resolution))) {
                    currentPreference
                            .setSummary(photoQualityEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));

                }

                // change Snapshot Frequency
                else if (key.equals(getResources().getString(R.string.SP_photo_snapshot_frequency))) {
                    currentPreference.setSummary(sharedPref.getString(getResources()
                            .getString(R.string.SP_photo_snapshot_frequency), "30") + getString(R.string._seconds));
                }

                // change Focus Mode
                else if (key.equals(getResources().getString(R.string.SP_photo_focus_mode))) {
                    currentPreference.setSummary(focusModesEntries_L.get
                            (Integer.parseInt(sharedPref.getString(getResources()
                            .getString(R.string.SP_photo_focus_mode), getString(R.string.defaultt)))));
                }
            }
        }
    }
}
