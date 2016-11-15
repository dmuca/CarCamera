package com.damianmuca.hoymm.kamerkasamochodowa;

import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Hoymm on 2016-09-02.
 */

public class SP_Data {
    public static final
            // SharedPreferences KEYS
    String  PREF_FILE = "com.damianmuca.hoymm.kamerkasamochodowa.shared_pref_file",
                // Setting CAPTURE PHOTO
            SP_PHOTO_CAPTURE_RESOLUTION = "com.damianmuca.hoymm.kamerkasamochodowa.SP_PHOTO_CAPTURE_RESOLUTION",
            SHOW_PHOTO_TIME_TO_SHOT_ON_SCREEN = "com.damianmuca.hoymm.kamerkasamochodowa.SHOW_REC_TIME_ON_SCREEN",
            SHOW_DATA_PHOTO_DATA_TAKEN = "com.damianmuca.hoymm.kamerkasamochodowa.SHOW_DATA_PHOTO_DATA_TAKEN",
            HOW_MANY_PICTURES_CAN_I_SAVE = "com.damianmuca.hoymm.kamerkasamochodowa.HOW_MANY_PICTURES_CAN_I_SAVE",
                // Setting REC VIDEO
            SP_VIDEO_RECORD_RESOLUTION = "com.damianmuca.hoymm.kamerkasamochodowa.SP_VIDEO_RECORD_RESOLUTION",
            SHOW_REC_TIME_ON_SCREEN = "com.damianmuca.hoymm.kamerkasamochodowa.SHOW_REC_TIME_ON_SCREEN",
            HOW_MANY_MOVIES_CAN_I_SAVE = "com.damianmuca.hoymm.kamerkasamochodowa.HOW_MANY_MOVIES_CAN_I_SAVE",
            SHOW_DATA_VIDEO_TAKEN = "com.damianmuca.hoymm.kamerkasamochodowa.SHOW_DATA_VIDEO_TAKEN",
                // Sound / Microphone / Flash DISABLE/ENABLE
            SP_PHOTO_CAPTURE_SOUND_ENABLED = "com.damianmuca.hoymm.kamerkasamochodowa.photo_capture_sound_is_enabled";

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    // constructor
    public SP_Data() {}
    // ########################################## Setting for RECORD VIDEO ##########################################
        // VIDEO RESOLUTION
    public static void setVideoResolution (int resolution){
        setSPPrepare();
        editor.putInt(SP_VIDEO_RECORD_RESOLUTION, resolution);
        editor.apply();
    }
    public static int getVideoResolution() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getInt(SP_VIDEO_RECORD_RESOLUTION, 0);
    }
    // VIDEO RECORDING TIME SHOWING ON THE SCREEN
    public static void setShowRecTimeOnScreen(boolean ifShow){
        setSPPrepare();
        editor.putBoolean(SHOW_REC_TIME_ON_SCREEN, ifShow);
        editor.apply();
    }
    public static boolean getShowRecTimeOnScreen() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getBoolean(SHOW_REC_TIME_ON_SCREEN, true);
    }

    // HOW MANY VIDEOS CAN WE SAVE INTO MEMORY
    public static void setHowManyMoviesCanISave(int howMany){
        setSPPrepare();
        editor.putInt(HOW_MANY_MOVIES_CAN_I_SAVE, howMany);
        editor.apply();
    }
    public static int getHowManyMoviesCanISave() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getInt(HOW_MANY_MOVIES_CAN_I_SAVE, 5);
    }

    // IF DRAW DATA RECORDER OVER VIDEO CLIP
    public static void setIfShowingDataVideoRecorded(boolean show){
        setSPPrepare();
        editor.putBoolean(SHOW_DATA_VIDEO_TAKEN, show);
        editor.apply();
    }
    public static boolean getIfShowingDataVideoRecorded() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getBoolean(SHOW_DATA_VIDEO_TAKEN, true);
    }


    // ########################################## Setting for CAPTURE PHOTO ##########################################
        // PHOTO RESOLUTION
    public static void setPhotoResolution (int resolution){
        setSPPrepare();
        editor.putInt(SP_PHOTO_CAPTURE_RESOLUTION, resolution);
        editor.apply();
    }
    public static int getPhotoResolution() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getInt(SP_PHOTO_CAPTURE_RESOLUTION, 0);
    }
    // PHOTO TIME TO SHOT
    public static void setShowTimeOnShotPhoto(boolean ifShow){
        setSPPrepare();
        editor.putBoolean(SHOW_PHOTO_TIME_TO_SHOT_ON_SCREEN, ifShow);
        editor.apply();
    }
    public static boolean getShowTimeOnShotPhoto() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getBoolean(SHOW_PHOTO_TIME_TO_SHOT_ON_SCREEN, true);
    }

    // HOW MANY PICTURES CAN WE SAVE INTO MEMORY
    public static void setHowManyPicturesCanISave(int howMany){
        setSPPrepare();
        editor.putInt(HOW_MANY_PICTURES_CAN_I_SAVE, howMany);
        editor.apply();
    }
    public static int getHowManyPicturesCanISave() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getInt(HOW_MANY_PICTURES_CAN_I_SAVE, 5);
    }

    // IF DRAW DATA OF PICTURE TAKEN OVER PHOTO
    public static void setIfShowingDataPhotographyTaken(boolean show){
        setSPPrepare();
        editor.putBoolean(SHOW_DATA_PHOTO_DATA_TAKEN, show);
        editor.apply();
    }
    public static boolean getIfShowingDataPhotographyTaken() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getBoolean(SHOW_DATA_PHOTO_DATA_TAKEN, true);
    }





    // ########################################## Sound / Microphone / Flash DISABLE/ENABLE ##########################

        // IF PHOTO CAPTURING SOUND ENABLED
    public static void setPhotoCaptureSound(boolean isEnabled) {
        setSPPrepare();
        editor.putBoolean(SP_PHOTO_CAPTURE_SOUND_ENABLED, isEnabled);
        editor.apply();
    }

    public static boolean getIfPhotoCaptureSoundEnabled() {
        getSPPrepare();
        // second arg is a DEFAULT VALUE (if nothing is in memory)
        return sharedPreferences.getBoolean(SP_PHOTO_CAPTURE_SOUND_ENABLED, false);
    }

    // -----------------------------------------------------------------------------   MAIN FUNCTIONS ----------------
    // CLEAR ALL DATA
    public static void clearSettings() {
        SharedPreferences sharedPreferences = MainActivity.context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // prepare functions before GETTING and SETTING the SP (SharedPreferences) data
    private static void setSPPrepare(){
        sharedPreferences = MainActivity.context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    private static void getSPPrepare (){
        sharedPreferences = MainActivity.context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
    }
}
