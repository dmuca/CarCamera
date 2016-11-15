package com.damianmuca.hoymm.kamerkasamochodowa;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.damianmuca.hoymm.kamerkasamochodowa.MainActivity.getCameraInstance;

/**
 * Created by Hoymm on 2016-09-19.
 */

public class CameraSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    Camera myCameraObj;
    private String [] orientationEntries, cameraModeEntries;
    private List <String> sceneModeEntries_L, whiteBalanceEntries_L
            , antibandingEntries_L, exposureCompensationEntries_L, fallingInCaseOfErrorEntries_L;
    private SharedPreferences sharedPref;
    // Declare CURRENT CAMERA MODE
    private String
            currentCamMode
            ,currentSceneMode = null
            , currentWhiteBalance = null
            , currentExposureCompensation = null
            , currentAntibanding = null
            , currentFallingInCaseOfError = null;
    // Declare CURRENT CAMERA MODE
    private int currentSceneModePath
            , currentWhiteBalancePath
            , currentExposureCompensationPath
            , currentAntibandingPath
            , currentFallingInCaseOfErrorPath;

    boolean sceneModeSupported = true
            , whiteBalanceSupported = true
            , antibandingSupported = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.camera_settings);


    }


    private void setEntriesAndEntryValues(){
        orientationEntries = getResources().getStringArray(R.array.e_orientation);
        cameraModeEntries = getResources().getStringArray(R.array.e_cam_mode);

        // SCENE MODE set Entries and Entry
        try {
            sceneModeEntries_L = myCameraObj.getParameters().getSupportedSceneModes();
            sceneModeSupported = true;
        }
        catch (NullPointerException e){
            // NULL if NOT supported
            sceneModeSupported = false;
        }

        // WHITE BALANCE
        try {
            whiteBalanceEntries_L = myCameraObj.getParameters().getSupportedWhiteBalance();
            // add not set ENTRY
            whiteBalanceEntries_L.add(0,getString(R.string.not_set));
            whiteBalanceSupported = true;
        }
        catch (NullPointerException e){
            // NULL if NOT supported
            whiteBalanceSupported = false;
        }

        // EXPOSURE COMPENSATION
        exposureCompensationEntries_L = new ArrayList<>();
        for (int i = 0
             ; i < myCameraObj.getParameters().getMaxExposureCompensation() - myCameraObj.getParameters().getMinExposureCompensation() + 1
                ; ++i){
            exposureCompensationEntries_L.add(String.valueOf(myCameraObj.getParameters().getMinExposureCompensation() + i));
        }
        // add not set ENTRY
        exposureCompensationEntries_L.add(0,getString(R.string.not_set));


        // ANTIBANDING
        try {
            antibandingEntries_L = myCameraObj.getParameters().getSupportedAntibanding();
            antibandingSupported = true;
            // add not set ENTRY
            antibandingEntries_L.add(0,getString(R.string.not_set));
        }
        catch (NullPointerException e){
            // NULL if NOT supported
            antibandingSupported = false;
        }

        // FALLING IN CASE OF ERRORS
        fallingInCaseOfErrorEntries_L =
                new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.e_cam_falling_in_case_of_error)));




        // ################   Initializate SCENE MODES (entry values/ entries) ################
        ListPreference sceneModeP = (ListPreference) findPreference(getResources().getString(R.string.SP_scene_mode));
        if (sceneModeSupported){

            // entries (E)
            sceneModeP.setEntries(sceneModeEntries_L.toArray(new CharSequence[sceneModeEntries_L.size()]));

            // entry values (EV)
            CharSequence [] sceneModeEV_CQ = new CharSequence[sceneModeEntries_L.size()];
            for (int i = 0; i < sceneModeEV_CQ.length; ++i)
                sceneModeEV_CQ [i] =  String.valueOf(i);
            sceneModeP.setEntryValues(sceneModeEV_CQ);
        }


        // ################  Initializate WHITE BALANCE (entry values/ entries) ################
        ListPreference whiteBalanceP = (ListPreference) findPreference(getResources().getString(R.string.SP_camera_white_balance));

        if (whiteBalanceSupported){
            // add not set ENTRY
            whiteBalanceEntries_L.add(0,getString(R.string.not_set));

            // entries (E)
            whiteBalanceP.setEntries(whiteBalanceEntries_L.toArray(new CharSequence[whiteBalanceEntries_L.size()]));
            // entry values (EV)
            CharSequence[] whiteBalanceEV_CQ = new CharSequence[whiteBalanceEntries_L.size()];
            for (int i = 0; i < whiteBalanceEV_CQ.length; ++i)
                whiteBalanceEV_CQ[i] = String.valueOf(i);
            whiteBalanceP.setEntryValues(whiteBalanceEV_CQ);
        }


        // ################  Initializate EXPOSURE COMPENSATION (entry values/ entries) ################
        ListPreference exposureCompensationP = (ListPreference) findPreference(getResources().getString(R.string.SP_camera_exposure_compensation));

        int maxExposureCompensationE = myCameraObj.getParameters().getMaxExposureCompensation()
                , minExposureCompensationE = myCameraObj.getParameters().getMinExposureCompensation();

        // generate Exposure Compensation ENTRIES LIST and ENTRIES
        CharSequence [] exposureCompensationE_L = new CharSequence[maxExposureCompensationE-minExposureCompensationE+2]
                ,exposureCompensationEV_L = new CharSequence[maxExposureCompensationE-minExposureCompensationE+2];


        // add not set ENTRY
        exposureCompensationE_L[0] = getString(R.string.not_set);
        exposureCompensationEV_L[0] = "0";
        // add exposure compensation ENTRIES and it's entry values (id's)
        for (int i = 1; i < maxExposureCompensationE - minExposureCompensationE + 2; ++i) {
            exposureCompensationE_L[i] = String.valueOf(minExposureCompensationE + i - 1);
            exposureCompensationEV_L[i] = String.valueOf(i);
        }
        // entries (E)
        exposureCompensationP.setEntries(exposureCompensationE_L);
        // entry values (EV)
        exposureCompensationP.setEntryValues(exposureCompensationEV_L);





        // ################  Initializate ANTIBANDING (entry values/ entries) ################
        ListPreference antibandingP = (ListPreference) findPreference(getResources().getString(R.string.SP_camera_antibanding));

        if (antibandingSupported){
            // entries (E)
            antibandingP.setEntries(antibandingEntries_L.toArray(new CharSequence[antibandingEntries_L.size()]));
            // entry values (EV)
            CharSequence[] antibandingEV_CQ = new CharSequence[antibandingEntries_L.size()];
            for (int i = 0; i < antibandingEV_CQ.length; ++i)
                antibandingEV_CQ[i] = String.valueOf(i);
            antibandingP.setEntryValues(antibandingEV_CQ);
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference currentPreference = findPreference(key);
        // Camera Orientation
        // isAdded() prevents crash when changing values few times
        if(isAdded()) {
            synchronized (this) {

                // change ORIENTATION SETTINGS
                if (key.equals(getResources().getString(R.string.SP_orientation)))
                    currentPreference.setSummary(orientationEntries[Integer.parseInt(sharedPreferences.getString(key, "0"))]);
                // change CAMERA MODE setting
                else if (key.equals(getResources().getString(R.string.SP_camera_mode))) {
                    readSharedPreferencesForEachCameraMode(sharedPreferences.getString(key, "0"));
                    currentPreference.setSummary(cameraModeEntries[Integer.parseInt(sharedPreferences.getString(key, "0"))]);
                    refreshSummaries();


                    /*// if CAMERA MODE has been changed then you must change settings for the SETTED ONE
                    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    String sceneModeValue = getResources().getString(currentSceneModePath);
                    String whiteBalanceValue = getResources().getString(currentWhiteBalancePath);
                    String exposureCompensationValue = getResources().getString(currentExposureCompensationPath);
                    String antibandingValue = getResources().getString(currentAntibandingPath);
                    String fallingInCaseOfErrorValue = getResources().getString(currentFallingInCaseOfErrorPath);*/

                }

                // change SCENE MODE setting
                else if (key.equals(getResources().getString(R.string.SP_scene_mode)) && sceneModeSupported) {
                    String sME = sceneModeEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0")));
                    currentPreference.setSummary(sME);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    //String cSMP = getResources().getString(currentSceneModePath);
                    editor.putString(getResources().getString(currentSceneModePath), sharedPreferences.getString(key, "0"));
                    editor.apply();
                }

                // change CAMERA WHITE BALANCE setting
                else if (key.equals(getResources().getString(R.string.SP_camera_white_balance)) && whiteBalanceSupported) {
                        currentPreference.setSummary(whiteBalanceEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getResources().getString(currentWhiteBalancePath), sharedPreferences.getString(key, "0"));
                        editor.apply();
                }

                // change CAMERA EXPOSURE COMPENSATION setting
                else if (key.equals(getResources().getString(R.string.SP_camera_exposure_compensation))) {
                    currentPreference.setSummary(exposureCompensationEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getResources().getString(currentExposureCompensationPath), sharedPreferences.getString(key, "0"));
                    editor.apply();
                }

                // change ANTIBANDING setting
                else if (key.equals(getResources().getString(R.string.SP_camera_antibanding)) && antibandingSupported) {
                    currentPreference.setSummary(antibandingEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getResources().getString(currentAntibandingPath), sharedPreferences.getString(key, "0"));
                    editor.apply();
                }

                // change FALLING IN CASE OF ERROR setting
                else if (key.equals(getResources().getString(R.string.SP_camera_falling_in_case_of_error))) {
                    currentPreference.setSummary(fallingInCaseOfErrorEntries_L.get(Integer.parseInt(sharedPreferences.getString(key, "0"))));
                    SharedPreferences.Editor editor = sharedPref.edit();

                    String first = getResources().getString(currentFallingInCaseOfErrorPath);
                    String second = sharedPreferences.getString(key, "0");

                    editor.putString(first, second);
                    editor.apply();
                }
            }

        }

    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // init camera
        initializateMyCameraObj();

        setEntriesAndEntryValues();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);

        readSharedPreferencesForEachCameraMode(sharedPref.getString(getResources().getString(R.string.SP_camera_mode), "0"));
        refreshSummaries();
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


        // set orienatation Summary (describe)
        String cameraOrientation = sharedPref.getString(getResources().getString(R.string.SP_orientation), "0");
        Preference cameraOrientationPref = findPreference(getResources().getString(R.string.SP_orientation));
        cameraOrientationPref.setSummary(orientationEntries[Integer.parseInt(cameraOrientation)]);


        // PREFERENCES
        camMode = (ListPreference) findPreference(getResources().getString(R.string.SP_camera_mode));
        sceneMode = (ListPreference) findPreference(getResources().getString(R.string.SP_scene_mode));
        whiteBalance = (ListPreference) findPreference(getResources().getString(R.string.SP_camera_white_balance));
        exposureCompensation = (ListPreference) findPreference(getResources().getString(R.string.SP_camera_exposure_compensation));
        antibanding = (ListPreference) findPreference(getResources().getString(R.string.SP_camera_antibanding));
        fallingInCaseOfError = (ListPreference) findPreference(getResources().getString(R.string.SP_camera_falling_in_case_of_error));


        String curCamMode = sharedPref.getString(getResources().getString(R.string.SP_camera_mode), "0");
        // SHAREDPREFERENCES
            //sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);





        // cameraMode is always enabled
        currentCamMode = sharedPref.getString(getResources().getString(R.string.SP_camera_mode), "0");
        camMode.setSummary(cameraModeEntries[Integer.parseInt(currentCamMode)]);
        camMode.setValue(currentCamMode);
        // get EACH values (expressed in Strings)
        if (!curCamMode.equals("0")) {

            SharedPreferences.Editor editor = sharedPref.edit();
            int defaultSceneMode, defaultWhiteBalance;
            // DAYLIGHT
            if (currentCamMode.equals("1")) {
                defaultSceneMode = myCameraObj.getParameters().getSupportedSceneModes().indexOf("landscape");
                defaultWhiteBalance = myCameraObj.getParameters().getSupportedWhiteBalance().indexOf("auto");
            }
            // SUNNY
            else if (currentCamMode.equals("2")) {
                defaultSceneMode = myCameraObj.getParameters().getSupportedSceneModes().indexOf("beach");
                defaultWhiteBalance = myCameraObj.getParameters().getSupportedWhiteBalance().indexOf("daylight");
            }
            // CLOUDY
            else if (currentCamMode.equals("3")) {
                defaultSceneMode = myCameraObj.getParameters().getSupportedSceneModes().indexOf("candlelight");
                defaultWhiteBalance = myCameraObj.getParameters().getSupportedWhiteBalance().indexOf("cloudy-daylight");
            }
            // MOONLIGHT
            else if (currentCamMode.equals("4")) {
                defaultSceneMode = myCameraObj.getParameters().getSupportedSceneModes().indexOf("fireworks");
                defaultWhiteBalance = myCameraObj.getParameters().getSupportedWhiteBalance().indexOf("twilight");
            }
            // DARK NIGHT
            else if (currentCamMode.equals("5")) {
                defaultSceneMode = myCameraObj.getParameters().getSupportedSceneModes().indexOf("night");
                defaultWhiteBalance = myCameraObj.getParameters().getSupportedWhiteBalance().indexOf("shade");
            }
            // CITY NIGHT
            else if (currentCamMode.equals("6")) {
                defaultSceneMode = myCameraObj.getParameters().getSupportedSceneModes().indexOf("party");
                defaultWhiteBalance = myCameraObj.getParameters().getSupportedWhiteBalance().indexOf("fluorescent");
            }
            // CUSTOM
            else if (currentCamMode.equals("7")) {
                defaultSceneMode = 0;
                defaultWhiteBalance = 0;
            }
            else{
                // something is WRONG -- ERROR that line should NOT be invoked !
                defaultSceneMode = defaultWhiteBalance = -2;
            }
            // in CASE phone has NO SUCH MODE, then change it to index 0, it's AUTO value
            defaultSceneMode = defaultSceneMode == - 1 ? 0 : defaultSceneMode;
            defaultWhiteBalance = defaultWhiteBalance == -1 ? 0 : defaultWhiteBalance;
            // save DEFAULT values of modes to shared preferences (so the app can read it from SP when you go back to Camera Screen)
            editor.putString(getResources().getString(currentSceneModePath), String.valueOf(defaultSceneMode));
            editor.putString(getResources().getString(currentWhiteBalancePath), String.valueOf(defaultWhiteBalance));
            editor.apply();

            // set CURRENT settings properties
            currentSceneMode = sharedPref.getString(getResources().getString(currentSceneModePath), String.valueOf(defaultSceneMode));
            currentWhiteBalance = sharedPref.getString(getResources().getString(currentWhiteBalancePath), String.valueOf(defaultWhiteBalance));
            currentExposureCompensation = sharedPref.getString(getResources().getString(currentExposureCompensationPath), "0");
            currentAntibanding = sharedPref.getString(getResources().getString(currentAntibandingPath), "0");
            currentFallingInCaseOfError = sharedPref.getString(getResources().getString(currentFallingInCaseOfErrorPath), "0");





            // refresh subsettings (scene mode, white balance, exposure... )summaries
            if (sceneModeSupported)
                sceneMode.setSummary(sceneModeEntries_L.get(Integer.parseInt(currentSceneMode)));
            if (whiteBalanceSupported)
                whiteBalance.setSummary(whiteBalanceEntries_L.get(Integer.parseInt(currentWhiteBalance)));

            exposureCompensation.setSummary(exposureCompensationEntries_L.get(Integer.parseInt(currentExposureCompensation)));
            if (antibandingSupported)
                antibanding.setSummary(antibandingEntries_L.get(Integer.parseInt(currentAntibanding)));
            fallingInCaseOfError.setSummary(fallingInCaseOfErrorEntries_L.get(Integer.parseInt(currentFallingInCaseOfError)));






            // refresh subsettings (scene mode, white balance, exposure... ) current values
            sceneMode.setValue(currentSceneMode);
            whiteBalance.setValue(currentWhiteBalance);
            exposureCompensation.setValue(currentExposureCompensation);
            antibanding.setValue(currentAntibanding);
            fallingInCaseOfError.setValue(currentFallingInCaseOfError);
        }
        else {

            // refresh subsettings (scene mode, white balance, exposure... )summaries
            sceneMode.setSummary(getString(R.string._default_));
            whiteBalance.setSummary(getString(R.string._default_));
            exposureCompensation.setSummary(getString(R.string._default_));
            antibanding.setSummary(getString(R.string._default_));
            fallingInCaseOfError.setSummary(getResources().getStringArray(R.array.e_cam_falling_in_case_of_error)[0]);
        }





        // DISABLE or ENABLE ListPreference depend on Camera Mode if (Not set), then disable
        sceneMode.setEnabled(!curCamMode.equals("0") && sceneModeSupported);
        whiteBalance.setEnabled(!curCamMode.equals("0") && whiteBalanceSupported);
        exposureCompensation.setEnabled(!curCamMode.equals("0"));
        antibanding.setEnabled(!curCamMode.equals("0") && antibandingSupported);
        fallingInCaseOfError.setEnabled(!curCamMode.equals("0"));

    }
    ListPreference camMode, sceneMode,whiteBalance,exposureCompensation,antibanding,fallingInCaseOfError;
    private void readSharedPreferencesForEachCameraMode(String camMode){
        // camera mode NOT SET
        if (camMode.equals("0")){
            currentSceneMode = null;
            currentWhiteBalance = null;
            currentExposureCompensation = null;
            currentAntibanding = null;
            currentFallingInCaseOfError = null;
        }
        // camera mode Daylight
        else if (camMode.equals("1")){
            currentSceneModePath = R.string.SP_scene_mode_daylight;
            currentWhiteBalancePath = R.string.SP_camera_white_balance_daylight;
            currentExposureCompensationPath = R.string.SP_camera_exposure_compensation_daylight;
            currentAntibandingPath = R.string.SP_camera_antibanding_daylight;
            currentFallingInCaseOfErrorPath = R.string.SP_camera_falling_in_case_of_error_daylight;
        }
        // camera mode Sunny
        else if (camMode.equals("2")){
            currentSceneModePath = R.string.SP_scene_mode_sunny;
            currentWhiteBalancePath = R.string.SP_camera_white_balance_sunny;
            currentExposureCompensationPath = R.string.SP_camera_exposure_compensation_sunny;
            currentAntibandingPath = R.string.SP_camera_antibanding_sunny;
            currentFallingInCaseOfErrorPath = R.string.SP_camera_falling_in_case_of_error_sunny;

        }
        // camera mode Cloudy
        else if (camMode.equals("3")){
            currentSceneModePath = R.string.SP_scene_mode_cloudy;
            currentWhiteBalancePath = R.string.SP_camera_white_balance_cloudy;
            currentExposureCompensationPath = R.string.SP_camera_exposure_compensation_cloudy;
            currentAntibandingPath = R.string.SP_camera_antibanding_cloudy;
            currentFallingInCaseOfErrorPath = R.string.SP_camera_falling_in_case_of_error_cloudy;
        }
        // camera mode Twilight
        else if (camMode.equals("4")){
            currentSceneModePath = R.string.SP_scene_mode_twilght;
            currentWhiteBalancePath = R.string.SP_camera_white_balance_twilght;
            currentExposureCompensationPath = R.string.SP_camera_exposure_compensation_twilght;
            currentAntibandingPath = R.string.SP_camera_antibanding_twilght;
            currentFallingInCaseOfErrorPath = R.string.SP_camera_falling_in_case_of_error_twilght;
        }
        // camera mode Dark Night
        else if (camMode.equals("5")){
            currentSceneModePath = R.string.SP_scene_mode_dn;
            currentWhiteBalancePath = R.string.SP_camera_white_balance_dn;
            currentExposureCompensationPath = R.string.SP_camera_exposure_compensation_dn;
            currentAntibandingPath = R.string.SP_camera_antibanding_dn;
            currentFallingInCaseOfErrorPath = R.string.SP_camera_falling_in_case_of_error_dn;
        }

        // camera mode City Night
        else if (camMode.equals("6")){
            currentSceneModePath = R.string.SP_scene_mode_cn;
            currentWhiteBalancePath = R.string.SP_camera_white_balance_cn;
            currentExposureCompensationPath = R.string.SP_camera_exposure_compensation_cn;
            currentAntibandingPath = R.string.SP_camera_antibanding_cn;
            currentFallingInCaseOfErrorPath = R.string.SP_camera_falling_in_case_of_error_cn;
        }

        // camera mode Custom
        else if (camMode.equals("7")){
            currentSceneModePath = R.string.SP_scene_mode_custom;
            currentWhiteBalancePath = R.string.SP_camera_white_balance_custom;
            currentExposureCompensationPath = R.string.SP_camera_exposure_compensation_custom;
            currentAntibandingPath = R.string.SP_camera_antibanding_custom;
            currentFallingInCaseOfErrorPath = R.string.SP_camera_falling_in_case_of_error_custom;
        }


    }


    private void initializateMyCameraObj() {
        try {
            myCameraObj = getCameraInstance();

            //Toast.makeText(context, sharedPref.getString(getResources().getString(R.string.SP_camera_mode), "0"), Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            //Toast.makeText(this, "Camera is currently used by another application", Toast.LENGTH_SHORT).show();
        }
    }
}
