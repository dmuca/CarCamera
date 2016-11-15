package com.damianmuca.hoymm.kamerkasamochodowa;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.damianmuca.hoymm.kamerkasamochodowa.MainActivity.getCameraInstance;

/**
 * Created by Hoymm on 2016-09-19.
 */

public class VideoSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    Camera myCameraObj;
    private SharedPreferences sharedPref;
    private List<String> videoQualityEntries_L,videoResolutionEntries_L
            , videoEncoderEntries_L, bitrateEntries_L
            ,framerateEntries_L ,fileFormatEntries_L, focusModesEntries_L
            ,cameraOperationEntries_L, videoLengthFileEntries_L
            , protectVideoFilesEntries_L;

    // resolutions LIST as SIZE - of elements type
    List <Camera.Size> sortedResolutionsL;


    boolean videoQualitySupported = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.video_settings);
    }



    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        // init camera
        initializateMyCameraObj();


        setEntriesAndEntryValues();

        // enable changing subsettings like (framerate, resolution... ) only when custom resolution option is activated
        ListPreference videoQualityLP = (ListPreference) findPreference(getResources().getString(R.string.SP_video_quality));
        enableOrDisableSubsettings(Integer.valueOf(videoQualityLP.getValue()));


        // SUMMARIES
        refreshSummaries();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference currentPreference = findPreference(key);
        // isAdded() prevents crash when changing values few times
        if(isAdded()) {
            synchronized (this) {
                // change VIDEO QUALITY Summary
                if (key.equals(getResources().getString(R.string.SP_video_quality))) {
                    currentPreference
                            .setSummary(videoQualityEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                    enableOrDisableSubsettings(Integer.parseInt(sharedPreferences.getString(key, "0")));
                }

                // check IF i have permission to use MICROPHONE / change VOLUME enable/disabled
                else if (key.equals(getResources().getString(R.string.SP_is_volume_enable))) {
                    if (ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {

                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                MainActivity.MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                        // MY_PERMISSIONS_REQUEST_CAMERA is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                        currentPreference.setEnabled(false);
                        // save SharedPref
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(getResources().getString(R.string.SP_is_volume_enable), false);
                        editor.apply();
                    }
                }
                // CUSTOM MODE SUBSETTINGS...
                else {
                    // get SharedPreferences DB to save some data
                    SharedPreferences.Editor editor = sharedPref.edit();
                    // if  VIDEO RESOLUTION
                    if (key.equals(getResources().getString(R.string.SP_video_resolution))) {
                        if (Integer.parseInt(sharedPreferences.getString(key, "0")) != videoResolutionEntries_L.size()-1)
                        {
                            currentPreference.setSummary
                                    (videoResolutionEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                        }
                        // when CUSTOM RESOLUTION
                        else{

                        }
                        // save saved data into SharedPreferences DB
                        editor.putInt(getResources().getString(R.string.SP_video_custom_resolution)
                                , Integer.parseInt(sharedPreferences.getString(key, "0")));
                    }
                    // if  VIDEO ENCODER
                    else if (key.equals(getResources().getString(R.string.SP_video_encoder))) {
                        currentPreference.setSummary
                                (videoEncoderEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                        // save saved data into SharedPreferences DB
                        editor.putInt(getResources().getString(R.string.SP_video_custom_encoder)
                                , Integer.parseInt(sharedPreferences.getString(key, "0")));
                    }
                    // if  VIDEO BITRATE
                    else if (key.equals(getResources().getString(R.string.SP_video_bitrate))) {
                        currentPreference
                                .setSummary(bitrateEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                        // save saved data into SharedPreferences DB
                        editor.putInt(getResources().getString(R.string.SP_video_custom_bitrate)
                                , Integer.parseInt(sharedPreferences.getString(key, "0")));
                    }
                    // if  VIDEO FRAMERATE
                    else if (key.equals(getResources().getString(R.string.SP_video_framerate))) {
                        currentPreference
                                .setSummary(framerateEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                        // save saved data into SharedPreferences DB
                        editor.putInt(getResources().getString(R.string.SP_video_custom_framerate)
                                , Integer.parseInt(sharedPreferences.getString(key, "0")));
                    }
                    // if  VIDEO FILE FORMAT
                    else if (key.equals(getResources().getString(R.string.SP_video_file_format))) {
                        currentPreference
                                .setSummary(fileFormatEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                        // save saved data into SharedPreferences DB
                        editor.putInt(getResources().getString(R.string.SP_video_custom_file_format)
                                , Integer.parseInt(sharedPreferences.getString(key, "0")));
                    }
                    editor.apply();
                }
                // Video Focus Mode
                if (key.equals(getResources().getString(R.string.SP_video_focus_mode)))
                    currentPreference.setSummary(focusModesEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                // if Video Camera Operation
                else if (key.equals(getResources().getString(R.string.SP_video_camera_operation)))
                    currentPreference.setSummary(getResources()
                            .getStringArray(R.array.ev_video_camera_operation)[Integer.parseInt(sharedPreferences.getString(key, "0"))]);
                // if  Video File Length
                else if (key.equals(getResources().getString(R.string.SP_video_length_per_file)))
                    currentPreference.setSummary(getResources()
                            .getStringArray(R.array.e_video_file_length)[Integer.parseInt(sharedPreferences.getString(key, "0"))]);
                // if  Video File Length
                else if (key.equals(getResources().getString(R.string.SP_video_protection_force)))
                    currentPreference.setSummary(getResources()
                            .getStringArray(R.array.e_video_files_protection)[Integer.parseInt(sharedPreferences.getString(key, "0"))]);

            }
        }
    }

    private void enableOrDisableSubsettings(int videoQualityIndex) {

        ListPreference videoResolutionLP = (ListPreference)
                findPreference(getResources().getString(R.string.SP_video_resolution));

        ListPreference videoEncoderLP = (ListPreference)
                findPreference(getResources().getString(R.string.SP_video_encoder));

        ListPreference bitrateLP = (ListPreference)
                findPreference(getResources().getString(R.string.SP_video_bitrate));

        ListPreference framerateLP = (ListPreference)
                findPreference(getResources().getString(R.string.SP_video_framerate));

        ListPreference fileformatLP = (ListPreference)
                findPreference(getResources().getString(R.string.SP_video_file_format));

        // if CUSTOM
        if (videoQualityIndex==2){  // index of 2 equals CUSTOM RESOLUTION
            videoResolutionLP.setEnabled(true);
            videoEncoderLP.setEnabled(true);
            bitrateLP.setEnabled(true);
            framerateLP.setEnabled(true);
            fileformatLP.setEnabled(true);

            // Video Resolution
            int videoResolutionIndex = sharedPref.getInt(getResources().getString(R.string.SP_video_custom_resolution),1);
            videoResolutionLP.setSummary(videoResolutionEntries_L.get(videoResolutionIndex));
            videoResolutionLP.setValue(String.valueOf(videoResolutionIndex));

            // Video Encoder
            int videoEncoderIndex = sharedPref.getInt(getResources().getString(R.string.SP_video_custom_encoder),0);
            videoEncoderLP.setSummary(videoEncoderEntries_L.get(videoEncoderIndex));
            videoEncoderLP.setValue(String.valueOf(videoEncoderIndex));

            // Bitrate
            int bitrateIndex = sharedPref.getInt(getResources().getString(R.string.SP_video_custom_bitrate),4);
            bitrateLP.setSummary(bitrateEntries_L.get(bitrateIndex));
            bitrateLP.setValue(String.valueOf(bitrateIndex));

            // Framerate
            int framerateIndex = sharedPref.getInt(getResources().getString(R.string.SP_video_custom_framerate),0);
            framerateLP.setSummary(framerateEntries_L.get(framerateIndex));
            framerateLP.setValue(String.valueOf(framerateIndex));

            // File Format
            int fileFormatIndex = sharedPref.getInt(getResources().getString(R.string.SP_video_custom_file_format),1);
            fileformatLP.setSummary(fileFormatEntries_L.get(fileFormatIndex));
            fileformatLP.setValue(String.valueOf(fileFormatIndex));

        }
        // otherwise WE MUST BLOCK subsettings CHANGABLE, and set it's summary to proper value
        else{
            // # 1 set video resolution subsetting
            videoResolutionLP.setEnabled(false);
            int bitrateQualityMultipler = 2;
            int minBitrate = -1;
                // User have choosen his own resolution
            if (videoQualityIndex > 2) {
                videoResolutionLP.setSummary(videoResolutionEntries_L.get(videoQualityIndex - 3));
                minBitrate = (int) Math.ceil(sortedResolutionsL.get(videoQualityIndex - 3).width
                        *sortedResolutionsL.get(videoQualityIndex - 3).height
                        *30
                        *StaticValues.bitrateMultiplication
                        *bitrateQualityMultipler
                ); // round to ceil

            }
                // 0 - High Quality
            else if (videoQualityIndex == 0) {
                videoResolutionLP.setSummary(videoResolutionEntries_L.get(0));

                minBitrate = (int) Math.ceil(
                        sortedResolutionsL.get(0).width
                        *sortedResolutionsL.get(0).height
                        *30
                        *StaticValues.bitrateMultiplication
                        *bitrateQualityMultipler
                ); // round to ceil
            }
                // 1 - Low Quality
            else if (videoQualityIndex == 1) {
                videoResolutionLP.setSummary(videoResolutionEntries_L.get(videoResolutionEntries_L.size() - 3));
                minBitrate = (int) Math.ceil(
                        sortedResolutionsL.get(videoResolutionEntries_L.size() - 3).width
                            *sortedResolutionsL.get(videoResolutionEntries_L.size() - 3).height
                            *30
                            *StaticValues.bitrateMultiplication
                            *bitrateQualityMultipler
                    ); // round to ceil
            }

            videoEncoderLP.setEnabled(false);
            videoEncoderLP.setSummary(videoEncoderEntries_L.get(0));

            bitrateLP.setEnabled(false);
            bitrateLP.setSummary(showBitrateProperValue(minBitrate));


            framerateLP.setEnabled(false);
            framerateLP.setSummary(framerateEntries_L.get(0));

            fileformatLP.setEnabled(false);
            fileformatLP.setSummary(fileFormatEntries_L.get(1));
        }

    }

    private void setEntriesAndEntryValues() {
        // Video Quality
        setEntriesAndEntryValuesForVideoQuality();
        // Video Resolution
        setEntriesAndEntryValuesForVideoResolution();
        // Video Encoder
        setEntriesAndEntryValuesForVideoEncoder();
        // Bitrate
        setEntriesAndEntryValuesForBitrate();
        // Frame Rate
        setEntriesAndEntryValuesForFrameRate();
        // File Format
        setEntriesAndEntryValuesForFileFormat();
        // Focus Modes
        setEntriesAndEntryValuesForFocusMode();
    }

    private void setEntriesAndEntryValuesForFocusMode() {

        // Focus Modes
        focusModesEntries_L = myCameraObj.getParameters().getSupportedFocusModes();



        // ################   Initializate VIDEO FOCUS MODES (entry values/ entries) ################
        ListPreference videoFocusModesP = (ListPreference) findPreference(getResources().getString(R.string.SP_video_focus_mode));
        // entries (E)
        videoFocusModesP.setEntries(focusModesEntries_L.toArray(new CharSequence[focusModesEntries_L.size()]));

        // entry values (EV)
        CharSequence [] focusModesEV_CQ = new CharSequence[focusModesEntries_L.size()];
        for (int i = 0; i < focusModesEV_CQ.length; ++i)
            focusModesEV_CQ [i] =  String.valueOf(i);
        videoFocusModesP.setEntryValues(focusModesEV_CQ);
    }

    private void setEntriesAndEntryValuesForFileFormat() {
        // File Format
        fileFormatEntries_L = Arrays.asList(getResources().getStringArray(R.array.e_video_custom_file_format));


        // ################   Initializate FILE FORMATS (entry values/ entries) ################
        ListPreference fileFormatsP = (ListPreference) findPreference(getResources().getString(R.string.SP_video_file_format));
        // entries (E)
        fileFormatsP.setEntries(fileFormatEntries_L.toArray(new CharSequence[fileFormatEntries_L.size()]));
        // entry values (EV)
        CharSequence [] fileFormatsEV_CQ = new CharSequence[fileFormatEntries_L.size()];
        for (int i = 0; i < fileFormatsEV_CQ.length; ++i)
            fileFormatsEV_CQ [i] =  String.valueOf(i);
        fileFormatsP.setEntryValues(fileFormatsEV_CQ);

    }

    private void setEntriesAndEntryValuesForFrameRate() {
        // VIDEO FRAME RATE
        framerateEntries_L = Arrays.asList(getResources().getStringArray(R.array.e_video_custom_framerate));


        // ################   Initializate VIDEO FRAME RATE (entry values/ entries) ################
        ListPreference videoFrameRateP = (ListPreference) findPreference(getResources().getString(R.string.SP_video_framerate));
        // entries (E)
        videoFrameRateP.setEntries(framerateEntries_L.toArray(new CharSequence[framerateEntries_L.size()]));
        // entry values (EV)
        CharSequence [] frameRateEV_CQ = new CharSequence[framerateEntries_L.size()];
        for (int i = 0; i < frameRateEV_CQ.length; ++i)
            frameRateEV_CQ [i] =  String.valueOf(i);
        videoFrameRateP.setEntryValues(frameRateEV_CQ);
    }

    private void setEntriesAndEntryValuesForVideoEncoder() {

        // VIDEO ENCODER
        videoEncoderEntries_L = new ArrayList<>();
        for (String item : getResources().getStringArray(R.array.e_video_custom_encoder))
            videoEncoderEntries_L.add(item);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // VP8 encoder is supported since ANDROID 4.3+
        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP)
            videoEncoderEntries_L.add(videoEncoderEntries_L.size(), "VP8");


        // ################   Initializate VIDEO ENCODER (entry values/ entries) ################
        ListPreference videoEncoderP = (ListPreference) findPreference(getResources().getString(R.string.SP_video_encoder));
        // entries (E)
        videoEncoderP.setEntries(videoEncoderEntries_L.toArray(new CharSequence[videoEncoderEntries_L.size()]));
        // entry values (EV)
        CharSequence [] videoEncoderEV_CQ = new CharSequence[videoEncoderEntries_L.size()];
        for (int i = 0; i < videoEncoderEV_CQ.length; ++i)
            videoEncoderEV_CQ [i] =  String.valueOf(i);
        videoEncoderP.setEntryValues(videoEncoderEV_CQ);


    }

    private void setEntriesAndEntryValuesForVideoResolution() {

        // VIDEO RESOLUTION
        try {
            videoResolutionEntries_L = new ArrayList<>();

            // add specified sizes (1920x1080 ect.)
            for (Camera.Size size : sortedResolutionsL){
                if (size.width == 176 && size.height == 144)
                    videoResolutionEntries_L.add("QCIF (176x144)");
                else if (size.width == 320 && size.height == 240)
                    videoResolutionEntries_L.add("QVGA (320x240)");
                else if (size.width == 400 && size.height == 240)
                    videoResolutionEntries_L.add("WQVGA (400x240)");
                else if (size.width == 352 && size.height == 288)
                    videoResolutionEntries_L.add("CIF (352x288)");
                else if (size.width == 480 && size.height == 320)
                    videoResolutionEntries_L.add("HVGA (480x320)");
                else if (size.width == 640 && size.height == 480)
                    videoResolutionEntries_L.add("VGA (640x480)");
                else if (size.width == 720 && size.height == 480)
                    videoResolutionEntries_L.add("D1 (720x480)");
                else if (size.width == 800 && size.height == 480)
                    videoResolutionEntries_L.add("WVGA (800x480)");
                else if (size.width == 864 && size.height == 480)
                    videoResolutionEntries_L.add("FWVGA (864x480)");
                else if (size.width == 1280 && size.height == 720)
                    videoResolutionEntries_L.add("HD 720p (1280x720)");
                else if (size.width == 1920 && size.height == 1080)
                    videoResolutionEntries_L.add("HD 1080p (1920x1080)");
                else if (size.width == 2048 && size.height == 1080)
                    videoResolutionEntries_L.add("2K 1080p (2048x1080)");
                else if (size.width == 1920 && size.height == 1088)
                    videoResolutionEntries_L.add("HD 1088p (1920x1088)");
                else if (size.width == 3840 && size.height == 2160)
                    videoResolutionEntries_L.add("UHD 4K 1088p (3840x2160)");
                else
                    videoResolutionEntries_L.add("(" + size.width + "x" + size.height + ")");
            }
        }
        catch (NullPointerException e){
            // NULL if NOT supported
            videoQualitySupported = false;
        }


        // ################   Initializate VIDEO RESOLUTINOS (entry values/ entries) ################
        ListPreference videoResolutionP = (ListPreference) findPreference(getResources().getString(R.string.SP_video_resolution));
        if (videoQualitySupported){
            // entries (E)
            videoResolutionP.setEntries(videoResolutionEntries_L.toArray(new CharSequence[videoResolutionEntries_L.size()]));

            // entry values (EV)
            CharSequence [] videoResolutionEV_CQ = new CharSequence[videoResolutionEntries_L.size()];
            for (int i = 0; i < videoResolutionEV_CQ.length; ++i)
                videoResolutionEV_CQ [i] =  String.valueOf(i);
            videoResolutionP.setEntryValues(videoResolutionEV_CQ);
        }



    }

    private void setEntriesAndEntryValuesForBitrate() {
        // VIDEO BITRATE
        refreshEntriesAndEntryValuesForBitrate(640, 480, 30);


        // ################   Initializate VIDEO BITRATE (entry values/ entries) ################
        ListPreference videoBitrateP = (ListPreference) findPreference(getResources().getString(R.string.SP_video_bitrate));
        // entries (E)
        videoBitrateP.setEntries(bitrateEntries_L.toArray(new CharSequence[bitrateEntries_L.size()]));
        // entry values (EV)
        CharSequence [] bitrateEV_CQ = new CharSequence[bitrateEntries_L.size()];
        for (int i = 0; i < bitrateEV_CQ.length; ++i)
            bitrateEV_CQ [i] =  String.valueOf(i);
        videoBitrateP.setEntryValues(bitrateEV_CQ);

    }

    private void setEntriesAndEntryValuesForVideoQuality() {
        // VIDEO QUALITY set Entries and Entry
        try {
            videoQualityEntries_L = new ArrayList<>();
            // add High / Low / Custom Quality
            for(String item : getResources().getStringArray(R.array.e_video_quality))
                videoQualityEntries_L.add(item);


            sortedResolutionsL = MainActivity.getCameraSizesL();

            // add specified sizes (1920x1080 ect.)
            for (Camera.Size size : sortedResolutionsL){
                if (size.width == 176 && size.height == 144)
                    videoQualityEntries_L.add("QCIF (176x144)");
                else if (size.width == 320 && size.height == 240)
                    videoQualityEntries_L.add("QVGA (320x240)");
                else if (size.width == 400 && size.height == 240)
                    videoQualityEntries_L.add("WQVGA (400x240)");
                else if (size.width == 352 && size.height == 288)
                    videoQualityEntries_L.add("CIF (352x288)");
                else if (size.width == 480 && size.height == 320)
                    videoQualityEntries_L.add("HVGA (480x320)");
                else if (size.width == 640 && size.height == 480)
                    videoQualityEntries_L.add("VGA (640x480)");
                else if (size.width == 720 && size.height == 480)
                    videoQualityEntries_L.add("D1 (720x480)");
                else if (size.width == 800 && size.height == 480)
                    videoQualityEntries_L.add("WVGA (800x480)");
                else if (size.width == 864 && size.height == 480)
                    videoQualityEntries_L.add("FWVGA (864x480)");
                else if (size.width == 1280 && size.height == 720)
                    videoQualityEntries_L.add("HD 720p (1280x720)");
                else if (size.width == 1920 && size.height == 1080)
                    videoQualityEntries_L.add("HD 1080p (1920x1080)");
                else if (size.width == 2048 && size.height == 1080)
                    videoQualityEntries_L.add("2K 1080p (2048x1080)");
                else if (size.width == 1920 && size.height == 1088)
                    videoQualityEntries_L.add("HD 1088p (1920x1088)");
                else if (size.width == 3840 && size.height == 2160)
                    videoQualityEntries_L.add("UHD 4K 1088p (3840x2160)");
                else
                    videoQualityEntries_L.add("(" + size.width + "x" + size.height + ")");
            }
            videoQualitySupported = true;
        }
        catch (NullPointerException e){
            // NULL if NOT supported
            videoQualitySupported = false;
        }

        // ################   Initializate VIDEO QUALITY (entry values/ entries) ################
        ListPreference videoQualityP = (ListPreference) findPreference(getResources().getString(R.string.SP_video_quality));
        if (videoQualitySupported){
            // entries (E)
            videoQualityP.setEntries(videoQualityEntries_L.toArray(new CharSequence[videoQualityEntries_L.size()]));

            // entry values (EV)
            CharSequence [] videoQualityEV_CQ = new CharSequence[videoQualityEntries_L.size()];
            for (int i = 0; i < videoQualityEV_CQ.length; ++i)
                videoQualityEV_CQ [i] =  String.valueOf(i);
            videoQualityP.setEntryValues(videoQualityEV_CQ);
        }

    }
    private void refreshEntriesAndEntryValuesForBitrate(int resWidth, int resHeight, int FPS) {

        int bitRateVideoMin = (int) Math.ceil(resWidth*resHeight*FPS*StaticValues.bitrateMultiplication); // round to ceil
        bitrateEntries_L = new ArrayList<>();

        double [] bitrateMultiply = {3.0,2.5,2.0,1.5,1.0};
        int currentBitrateMultiply = 0;
        for (String item : getResources().getStringArray(R.array.e_video_custom_bitrate)) {
            // Entries: Low, Low-Medium (232mbps)... etc
            if (currentBitrateMultiply < bitrateMultiply.length) {
                bitrateEntries_L.add(item
                        + " (" + showBitrateProperValue((int) (bitRateVideoMin * bitrateMultiply[currentBitrateMultiply])) + ")");
                ++currentBitrateMultiply;
            }
            // entry: Custom
            else
                bitrateEntries_L.add(item);
        }
    }

    private String showBitrateProperValue(int bitRateVideoMin) {
        String result;

        if (bitRateVideoMin > 1000000000)
            result = ((bitRateVideoMin/100000000)/10.0) + "gbps";
        else if (bitRateVideoMin > 1000000)
            result = ((bitRateVideoMin/100000)/10.0) + "mbps";
        else if (bitRateVideoMin > 1000)
            result = ((bitRateVideoMin/100)/10.0) + "kbps";
        else
            result = bitRateVideoMin + "bps";

        return result;
    }

    private void refreshSummaries() {

        // set Video Quality Summary (describe)
        String videoQuality = sharedPref.getString(getResources().getString(R.string.SP_video_quality), "0");
        Preference videoQualityP = findPreference(getResources().getString(R.string.SP_video_quality));
        videoQualityP.setSummary(videoQualityEntries_L.get(Integer.parseInt(videoQuality)));

        // set Focus Mode Summary (describe)
        String focusMode = sharedPref.getString(getResources().getString(R.string.SP_video_focus_mode), "0");
        Preference videoFocusModeP = findPreference(getResources().getString(R.string.SP_video_focus_mode));
        videoFocusModeP.setSummary(focusModesEntries_L.get(Integer.parseInt(focusMode)));


        // set Camera Operation (describe)
        String cameraOperation = sharedPref.getString(getResources().getString(R.string.SP_video_camera_operation), "0");
        ListPreference cameraOperationLP =
                (ListPreference) findPreference(getResources().getString(R.string.SP_video_camera_operation));
        cameraOperationLP.setSummary(cameraOperationLP.getEntries()[Integer.valueOf(cameraOperation)]);


        // set Video File Length (describe)
        String videoFileLength = sharedPref.getString(getResources().getString(R.string.SP_video_length_per_file), "0");
        ListPreference videoFileLengthLP =
                (ListPreference) findPreference(getResources().getString(R.string.SP_video_length_per_file));
        videoFileLengthLP.setSummary(videoFileLengthLP.getEntries()[Integer.valueOf(videoFileLength)]);


        // set Protect Video Files When Occurs (describe)
        String protectVideoFilesWhenOccurs = sharedPref
                .getString(getResources().getString(R.string.SP_video_protection_force), "0");

        ListPreference protectVideoFilesWhenOccursLP =
                (ListPreference) findPreference(getResources().getString(R.string.SP_video_protection_force));

        protectVideoFilesWhenOccursLP.setSummary
                (protectVideoFilesWhenOccursLP.getEntries()[Integer.valueOf(protectVideoFilesWhenOccurs)]);

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
}
