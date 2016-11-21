package com.damianmuca.hoymm.kamerkasamochodowa;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable {

    private AlertDialog noBackCameraOrInUse_AD, needSystemPermissionAD;

    static private List <Camera.Size> cameraSizesL, photoSizesL;
    ArrayList<File> listOfVideoFiles = null, listOfPictureFiles = null;
    // RUN PAUSE TIME
    private static final int RUN_TIME_PAUSE = 340;
    // PERMISSIONS CONTANTS
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 3;
    private final int MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 4;
    private final int MY_MULTIPLE_PERMISSIONS_REQUEST = 6;

    private LinearLayout llMain;
    private Camera.PictureCallback mPicture;    // picture making way
    public static Context context; // Camera SurfaceView object of class



    private static Camera myCameraObj;

    // Photo Capture Sound
    private MediaPlayer photoCaptureSound;
    LinearLayout modeButtonFromChoosingPanel;
    LinearLayout modeButtonToOpenPanel;
    ImageView modeButtonToOpenPanelIcon;
    private TextView modeButtonToOpenPanelTV, phtCamResolutionTV, videoCamResolutionTV;

    // modes list of choosing MODE PANEL
    LinearLayout [] modesListPanel = new LinearLayout[8];

    // VIDEO OBJECTS
    MediaRecorder mMediaRecorder;
    CameraPreview mPreview;
    private int videoRecordingTimeInSeconds;
    private boolean refreshBottomResolutionsTV = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;



        // in order to make Options Menu transparent on API >= 21, we must make STATUS BAR transparent
        //setStatusBarTranslucent(android.os.Build.VERSION.SDK_INT >= 21);



        setContentView(R.layout.activity_main);
        // initializate objects
        dialogForGetPermissionIsCurShowing = false;
        photoCaptureSound = MediaPlayer.create(this, R.raw.photograpy_capture_sound);
        modeButtonFromChoosingPanel = (LinearLayout) findViewById(R.id.choose_mode_panel_id);
        modeButtonToOpenPanel = (LinearLayout) findViewById(R.id.change_mode_button_id);

        modeButtonToOpenPanelIcon = (ImageView) findViewById(R.id.change_mode_button_img_id);
        modeButtonToOpenPanelTV = (TextView) findViewById(R.id.change_mode_button_tv_id);

        modeButtonToOpenPanelIcon = (ImageView) findViewById(R.id.change_mode_button_img_id);
        modeButtonToOpenPanelTV = (TextView) findViewById(R.id.change_mode_button_tv_id);

        phtCamResolutionTV = (TextView) findViewById(R.id.second_bottom_button_resolution_tv_id);
        videoCamResolutionTV = (TextView) findViewById(R.id.third_bottom_button_resolution_tv_id);

        modesListPanel[0] = (LinearLayout) findViewById(R.id.not_set_button_ll_id);
        modesListPanel[1] = (LinearLayout) findViewById(R.id.daylight_button_ll_id);
        modesListPanel[2] = (LinearLayout) findViewById(R.id.sunny_button_ll_id);
        modesListPanel[3] = (LinearLayout) findViewById(R.id.cloudy_button_ll_id);
        modesListPanel[4] = (LinearLayout) findViewById(R.id.moonlight_button_ll_id);
        modesListPanel[5] = (LinearLayout) findViewById(R.id.dark_night_button_ll_id);
        modesListPanel[6] = (LinearLayout) findViewById(R.id.city_night_button_ll_id);
        modesListPanel[7] = (LinearLayout) findViewById(R.id.custom_button_ll_id);

        // initializate alert dialog when no facing back camera found
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        noBackCameraOrInUse_AD = (builder.setMessage(R.string.no_back_camera_in_device)
                .setCancelable(false)
                .setIcon(android.R.drawable.stat_notify_error)
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })).create();


        // need system permission to modify system settings ALERT DIALOG
        needSystemPermissionAD =
                new AlertDialog.Builder(context).setTitle(R.string.app_needs_perm_to_wrk_properly)
                        .setMessage(R.string.allow_app_to_change_system_settings)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent grantIntent = new   Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                startActivity(grantIntent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogForGetPermissionIsCurShowing = false;
                                finish();
                            }
                        })
                        .create();



        llMain = (LinearLayout) findViewById(R.id.llMain);
        makingPicturesActivated = false;
    }
    int photoFrequency;
    private void readSharedPrefData() {
        // demand whether use DISABLED or ENABLED microphone ICON
        ImageView iv_micro = (ImageView) findViewById(R.id.iv_micro);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            iv_micro.setImageResource(R.mipmap.microphone_disabled);
        else {
            iv_micro.setImageResource(sharedPref.getBoolean(getResources().getString(R.string.SP_is_volume_enable), false)
                    ? R.mipmap.microphone_active : R.mipmap.microphone_disabled);

        }
        // demand whether photography capture sound is ENABLE or DISABLE
        ImageView iv_photoCaptureSound = (ImageView) findViewById(R.id.iv_speaker);
        iv_photoCaptureSound.setImageResource(SP_Data.getIfPhotoCaptureSoundEnabled() ? R.mipmap.speaker_enabled : R.mipmap.speaker_disabled);

        String frequencyData = (sharedPref.getString(getResources().getString(R.string.SP_photo_snapshot_frequency),"30"));
        if (isNumeric(frequencyData))
            photoFrequency = Integer.parseInt(frequencyData);
        else
            photoFrequency = 30;




    }

    public static boolean isNumeric(String str)
    {
        try
        {
            int d = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }


    private void initializatePicturesMaking() {
        final String TAG = "Picture Making: ";
        mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile==null){
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }
                try {

                    // convert byte array into bitmap
                    Bitmap loadedImage = BitmapFactory.decodeByteArray(data, 0,data.length);

                    // rotate Image
                    Matrix rotateMatrix = new Matrix();
                    rotateMatrix.postRotate(getDeviceRotation());
                    loadedImage = Bitmap.createBitmap(loadedImage, 0, 0,loadedImage.getWidth(), loadedImage.getHeight(),rotateMatrix, false);


                    ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                    // save image into gallery
                    loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

                    FileOutputStream fout = new FileOutputStream(pictureFile);
                    fout.write(ostream.toByteArray());
                    fout.close();

                    //tell System that new file was created, so It'll APPEAR INSTANTLY IN GALLERY
                    // scans ALL FILES (Huge Process)
                    //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                    // scan for SINGLE FILE
                    MediaScannerConnection.scanFile(context,
                            new String[] { pictureFile.toString() }, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });

                    deleteOldestFile(SP_Data.getHowManyPicturesCanISave(), MEDIA_TYPE_IMAGE);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listOfPictureFiles==null)
                                listOfPictureFiles = new ArrayList<>();
                            listOfPictureFiles.add(pictureFile);
                            deleteOldestFile(SP_Data.getHowManyPicturesCanISave(), MEDIA_TYPE_IMAGE);
                        }
                    });

                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        };
    }

    private int getDeviceRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;

            default:
                break;
        }

        // rotate saving photo, so it's POSITIONING will be proper
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // frontFacing
            rotation = (info.orientation + degree) % 360;
            rotation = (360 - rotation) % 360;
        } else {
            // Back-facing
            rotation = (info.orientation - degree + 360) % 360;
        }
        return rotation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_button:
                Toast.makeText(this, "Menu button clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings_button:
                startActivity(new Intent(context, SettingsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.access_camera_failed)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    protected void onDestroy() {
        // release CAMERA when close application
        if (myCameraObj != null) {
            myCameraObj.stopPreview();
            myCameraObj.setPreviewCallback(null);
            myCameraObj.release();
            myCameraObj = null;
        }
        super.onDestroy();
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        // if API is <19, then we cannot use this function
        if (android.os.Build.VERSION.SDK_INT < 19)
            return;

        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    // get NEEDED PERMISSIONS WHEN APP STARTS
    private boolean getPermissionsCamera() {
        // Here, thisActivity is the current activity -- CAMERA PERMISSION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                // Camera Permission Explanation
                Toast.makeText(this, R.string.camera_permisssion_explanation, Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_CAMERA is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        }
        else
            return true;
    }
    // get WRITE EXTERNAL STORAGE PERMISSION
    private boolean getPermissionsWriteExternalStorage() {
        // Here, thisActivity is the current activity -- WRITE_EXTERNAL_STORAGE PERMISSION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {


                // Camera Permission Explanation
                Toast.makeText(this, R.string.write_ext_storage_permission_explanation, Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        }
        else
            return true;
    }
    // get RECORD AUDIO PERMISSION
    private boolean getPermissionsRecordAudio() {
        if (ContextCompat.checkSelfPermission(this,  Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            return true;

        // Here, thisActivity is the current activity -- RECORD AUDIO PERMISSION
            // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            // Camera Permission Explanation
            Toast.makeText(this, R.string.rec_audio_permission_explanation, Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}
                    , MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

            // MY_PERMISSIONS_REQUEST_CAMERA is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
        return false;
    }
    // check permission wo WRITE_SETTINGS
    private boolean getPermissionToWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                // Do stuff here
                // Here, thisActivity is the current activity -- CAMERA PERMISSION
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_SETTINGS)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                        // WRITE_SETTINGS permission Explanation
                        Toast.makeText(this, R.string.write_settings_permission_explanation, Toast.LENGTH_LONG).show();

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_SETTINGS},
                                MY_PERMISSIONS_REQUEST_WRITE_SETTINGS);

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_SETTINGS},
                                MY_PERMISSIONS_REQUEST_WRITE_SETTINGS);

                        // MY_PERMISSIONS_REQUEST_WRITE_SETTINGS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                    return false;
                }
                else
                    return true;
            }
            else {
                return true;
            }
        }
        return true;
    }

    private boolean dialogForGetPermissionIsCurShowing;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            // ### ### ### CAMERA PERMISSIONS
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // Permission GRANTED
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initializateMyCameraObj();
                }
                // PERMISSION NOT GRANTED
                else {
                    // Camera Permission Explanation
                    Toast.makeText(this, R.string.camera_permisssion_explanation, Toast.LENGTH_LONG).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // ### ### ### WRITE EXTERNAL STORAGE PERMISSIONS
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // Permission GRANTED
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initializateMyFileListsObjects();
                    initializatePicturesMaking();
                }
                // PERMISSION NOT GRANTED
                else {

                    // WRITE EXTERNAL STORAGE Permission Explanation
                    Toast.makeText(this, R.string.write_external_storage_permission_denied, Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // ### ### ### RECORD AUDIO PERMISSIONS
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // Permission GRANTED
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                }
                // PERMISSION NOT GRANTED
                else {
                    // save to SharedPref that microphone is DISABLED
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(getResources().getString(R.string.SP_is_volume_enable),false);
                    editor.apply();
                    // change micro icon to DISABLE;
                    ImageView iv_micro = (ImageView) findViewById(R.id.iv_micro);
                    iv_micro.setImageResource(R.mipmap.microphone_disabled);
                    // Record Audio Permission Explanation
                    Toast.makeText(this, R.string.audio_permission_not_granted, Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            // ### ### ### WRITE SETTINGS PERMISSIONS
            case MY_PERMISSIONS_REQUEST_WRITE_SETTINGS: {
                // Permission GRANTED
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    useAppBrightness();
                }
                // PERMISSION NOT GRANTED
                else {
                    // Record Audio Permission Explanation
                    Toast.makeText(this, R.string.write_settings_perm_not_granted, Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            // ### ### ### WRITE SETTINGS PERMISSIONS
            case MY_MULTIPLE_PERMISSIONS_REQUEST: {
                // Permission GRANTED
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializateMostImportantObjectForAppWorking();
                }
                // PERMISSION NOT GRANTED
                else {
                    // Record Audio Permission Explanation
                    Toast.makeText(this, R.string.write_settings_perm_not_granted, Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }

    public static List<Camera.Size> getCameraSizesL() {
        return cameraSizesL;
    }

    public static List<Camera.Size> getPhotoSizesL() {
        return photoSizesL;
    }

    private boolean initializateMyCameraObj() {
        try {
            myCameraObj = getCameraInstance();
            // case camera is NOT used by ANOTHER PROCESS
            if (myCameraObj != null) {
                mPreview = new CameraPreview(context, myCameraObj, myCameraObj.getParameters());
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);


                // VIDEO RES BUBBLE SORT
                // Supported Camera Resolutions
                int change = 1;
                cameraSizesL = myCameraObj.getParameters().getSupportedVideoSizes();
                // if getSupportedVideoSizes(), returs null, then according to the Android library
                // that means it has the same sizes as getSupportedPreviewSizes(), so use it instead.
                if (cameraSizesL == null)
                    cameraSizesL = myCameraObj.getParameters().getSupportedPreviewSizes();
                while (change > 0) {
                    change = 0;
                    for (int i = 0; i < cameraSizesL.size()-1; ++i)
                        if (cameraSizesL.get(i).width < cameraSizesL.get(i+1).width) {
                            Collections.swap(cameraSizesL, i, i + 1);
                            ++change;
                        }
                }


                // PHOTO RES BUBBLE SORT
                change = 1;
                photoSizesL = myCameraObj.getParameters().getSupportedPictureSizes();
                while (change > 0) {
                    change = 0;
                    for (int i = 0; i < photoSizesL.size() - 1; ++i)
                        if (photoSizesL.get(i).width > photoSizesL.get(i + 1).width) {
                            Collections.swap(photoSizesL, i, i + 1);
                            ++change;
                        }
                }
                return true;
            }
            // when no CAMERA DEVICE
            else
                noBackCameraOrInUse_AD.show();
        }
        // when unable to get CAMERA OBJECT (probably in use by another app)
        catch (Exception e){
            // display error
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.camera_error)
                    .setMessage(R.string.camera_is_probably_in_use_by_another_app)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.stat_notify_error)
                    .show();
        }
        return false;
    }

    // PICTURE MAKING BUTTON
    boolean makingPicturesActivated, makingVideoActivated;
    public void pictureMakingButtonClicked(View view) {
        // if NO CAMERA PERMISSION GRANTED
        if (!ifAllThreePermissionNeededToRunGranted())
            getThreePermisions();
        // permission GRANTED program can make pictures
        else {

            makingPicturesActivated = !makingPicturesActivated;

            //set background color (gray - inactive / yellow - active)
            RelativeLayout tempRL = (RelativeLayout) findViewById(R.id.photocamera_button_bg_id);
            tempRL.setActivated(makingPicturesActivated);

            ImageView pictureMakingButton = (ImageView) findViewById(R.id.photo_cam_icon_id);
            pictureMakingButton.setActivated(makingPicturesActivated);
            // HIDE TIME over screen that left to TAKE ANOTHER PHOTO SHOT
            if (!makingPicturesActivated) {
                showTimeCounterToNextPhoto(false, 0);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            takingPictureRefresh();
        }
        lastPictureAtSeconds = 0;
    }
    // VIDEO MAKING BUTTON
    public void videoMakingButtonClicked(View view) {
        // if NO CAMERA PERMISSION GRANTED
        if (ifAllThreePermissionNeededToRunGranted()){
            // --- TAKE CARE OF VISUAL EFFECTS ---
            makingVideoActivated = !makingVideoActivated;
            RelativeLayout videoMakingButton = (RelativeLayout) findViewById(R.id.videocamera_button_bg_id);
            // make REC red circle and text, visible/invisible
            // red circle shape
            View vRedCircle = findViewById(R.id.red_record_circle);
            vRedCircle.setVisibility(makingVideoActivated ? View.VISIBLE : View.INVISIBLE);
            // red text
            TextView tvRecText = (TextView) findViewById(R.id.tv_record);
            tvRecText.setVisibility(makingVideoActivated ? View.VISIBLE : View.INVISIBLE);

            // HIDE record TIME ON SCREEN (TextView)
            if (!makingVideoActivated) {
                showTimeRecording(false, 0);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            videoMakingButton.setActivated(makingVideoActivated);
            takingPictureRefresh();
        }
        // permission GRANTED program can make video
        else
            getThreePermisions();
    }

    private boolean prepareVideoRecorder(){
        String TAG = "VIDEO RECORD BUTTON: ";
        mMediaRecorder = new MediaRecorder();



        // Step Hoymm 0: Adjust for orientation
        mMediaRecorder.setOrientationHint(getDeviceRotation());

        // Step 1: Unlock and set camera to MediaRecorder
        myCameraObj.unlock();
        mMediaRecorder.setCamera(myCameraObj);


        // Step 2: Set sources
        //      - Audio
        boolean recordVideoSound  = sharedPref.getBoolean(getResources().getString(R.string.SP_is_volume_enable), true);
        String videoQualityMode = sharedPref.getString(getResources().getString(R.string.SP_video_quality),"0");

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        // HQ
        if ( Integer.valueOf(videoQualityMode) == 0){
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        }
        // LQ
        else if (Integer.valueOf(videoQualityMode) == 1){
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        }
        // HQ, LQ
        if (Integer.valueOf(videoQualityMode) <= 1) {
            // if video enabled sound
            if (recordVideoSound) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
                mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
                mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
                mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
                mMediaRecorder.setAudioChannels(profile.audioChannels);
                mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
                mMediaRecorder.setVideoEncoder(profile.videoCodec);
                mMediaRecorder.setAudioEncoder(profile.audioCodec);

            }
            // if video disabled sound
            else {
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
                mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
                mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
                mMediaRecorder.setVideoEncoder(profile.videoCodec);


            }
        }
        // Resolution or Custom Video Mode
        else{
            String videoFileFormatIndex, frameRateIndex, bitrateIndex, videoEncoderIndex;
            // some RESOLUTION (not-custom)
            if (Integer.valueOf(videoQualityMode) != 2) {
                // Bitrate
                videoFileFormatIndex = sharedPref.getString(getResources().getString(R.string.SP_video_file_format), "0");
                frameRateIndex = sharedPref.getString(getResources().getString(R.string.SP_video_framerate), "0");
                bitrateIndex = sharedPref.getString(getResources().getString(R.string.SP_video_bitrate), "0");
                videoEncoderIndex = sharedPref.getString(getResources().getString(R.string.SP_video_encoder), "0");
            }
            // CUSTOM mode
            else{
                videoFileFormatIndex =
                        String.valueOf(sharedPref.getInt(getResources().getString(R.string.SP_video_custom_file_format), 0));
                frameRateIndex =
                        String.valueOf(sharedPref.getInt(getResources().getString(R.string.SP_video_custom_framerate), 0));
                bitrateIndex =
                        String.valueOf(sharedPref.getInt(getResources().getString(R.string.SP_video_custom_bitrate), 0));
                videoEncoderIndex =
                        String.valueOf(sharedPref.getInt(getResources().getString(R.string.SP_video_custom_encoder), 0));
            }

            int frameRateProfile;

            // Frame Rate
            switch (Integer.valueOf(frameRateIndex)) {
                case 0:
                    frameRateProfile = 30;
                    break;
                case 1:
                    frameRateProfile = 25;
                    break;
                case 2:
                    frameRateProfile = 20;
                    break;
                case 3:
                    frameRateProfile = 15;
                    break;
                case 4:
                    frameRateProfile = 10;
                    break;
                default:
                    frameRateProfile = 30;
                    break;
            }


            int curVideoBitrateProfile;
            int videoEncoderProfile;
            double[] bitrateMultiply = {3.0, 2.5, 2.0, 1.5, 1.0};

            // Encoder
            switch (Integer.valueOf(videoEncoderIndex)) {
                case 0:
                    videoEncoderProfile = MediaRecorder.VideoEncoder.H264;
                    break;
                case 1:
                    videoEncoderProfile = MediaRecorder.VideoEncoder.H263;
                    break;
                case 2:
                    videoEncoderProfile = MediaRecorder.VideoEncoder.MPEG_4_SP;
                    break;
                case 3:
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        videoEncoderProfile = MediaRecorder.VideoEncoder.VP8;
                        break;
                    }
                default:
                    videoEncoderProfile = MediaRecorder.VideoEncoder.H264;
            }


            // Camera Supported Resolution Size
            if (Integer.valueOf(videoQualityMode) >= 3) {
                curVideoBitrateProfile = (int) Math.ceil(cameraSizesL.get(Integer.valueOf(videoQualityMode) - 3).width
                        * cameraSizesL.get(Integer.valueOf(videoQualityMode) - 3).height
                        * frameRateProfile * StaticValues.bitrateMultiplication); // round to ceil
                curVideoBitrateProfile = (int) (curVideoBitrateProfile * bitrateMultiply[Integer.valueOf(bitrateIndex)]);
            }
            // Camera Custom Resolution Size
            if (Integer.valueOf(videoQualityMode) >= 3) {
                curVideoBitrateProfile = (int) Math.ceil(cameraSizesL.get(Integer.valueOf(videoQualityMode) - 3).width
                        * cameraSizesL.get(Integer.valueOf(videoQualityMode) - 3).height
                        * frameRateProfile * StaticValues.bitrateMultiplication); // round to ceil
                curVideoBitrateProfile = (int) (curVideoBitrateProfile * bitrateMultiply[Integer.valueOf(bitrateIndex)]);
            }
            // HQ res
            else if (Integer.valueOf(videoQualityMode) == 0){
                curVideoBitrateProfile = CamcorderProfile.get( CamcorderProfile.QUALITY_HIGH).videoBitRate;
            }
            // LQ res
            else {
                curVideoBitrateProfile = CamcorderProfile.get( CamcorderProfile.QUALITY_HIGH).videoBitRate;
            }


            // enable sound
            if (recordVideoSound) {


                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

                // Output Format
                if (videoFileFormatIndex.equals("0"))
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                else if (videoFileFormatIndex.equals("1"))
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                else

                // Frame Rate
                mMediaRecorder.setVideoFrameRate(frameRateProfile);


                // Video Size (if NO CUSTOM - some DEV SUPPORTED resolution)
                if (Integer.valueOf(videoQualityMode) != 2) {
                    mMediaRecorder.setVideoSize(cameraSizesL.get(Integer.valueOf(videoQualityMode) - 3).width
                            , cameraSizesL.get(Integer.valueOf(videoQualityMode) - 3).height);
                }
                // Video Size (if CUSTOM videoQualityMode == 2)
                else {
                    int customVideoResolution = sharedPref.getInt(getResources().getString(R.string.SP_video_custom_resolution),0);
                    mMediaRecorder.setVideoSize(cameraSizesL.get(customVideoResolution).width
                            , cameraSizesL.get(customVideoResolution).height);
                }


                // Video Bitrate
                mMediaRecorder.setVideoEncodingBitRate(curVideoBitrateProfile);
                mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
                mMediaRecorder.setAudioChannels(profile.audioChannels);
                mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);

                // Video Encoder
                mMediaRecorder.setVideoEncoder(videoEncoderProfile);
                mMediaRecorder.setAudioEncoder(profile.audioCodec);

            }
            // disable sound
            else {
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                // Output Format
                if (videoFileFormatIndex.equals("0"))
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                else if (videoFileFormatIndex.equals("1"))
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

                // Frame Rate
                mMediaRecorder.setVideoFrameRate(frameRateProfile);


                // Video Size (if NO CUSTOM - some DEV SUPPORTED resolution)
                if (Integer.valueOf(videoQualityMode) != 2) {
                    mMediaRecorder.setVideoSize(cameraSizesL.get(Integer.valueOf(videoQualityMode) - 3).width
                            , cameraSizesL.get(Integer.valueOf(videoQualityMode) - 3).height);
                }
                // Video Size (if CUSTOM videoQualityMode == 2)
                else {
                    int customVideoResolution = sharedPref.getInt(getResources().getString(R.string.SP_video_custom_resolution),0);
                    mMediaRecorder.setVideoSize(cameraSizesL.get(customVideoResolution).width
                            , cameraSizesL.get(customVideoResolution).height);
                }

                // Video Bitrate
                mMediaRecorder.setVideoEncodingBitRate(curVideoBitrateProfile);

                // Video Encoder
                mMediaRecorder.setVideoEncoder(videoEncoderProfile);
            }

        }



        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));


        // Step 4: Set output file
        File tempVideoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        assert tempVideoFile != null;
        mMediaRecorder.setOutputFile(tempVideoFile.toString());

        //tell System that new file was created, so It'll APPEAR INSTANTLY IN GALLERY
        // scans ALL FILES (Huge Process)
        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        // scan for SINGLE FILE
        MediaScannerConnection.scanFile(context,
                new String[] { tempVideoFile.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });



        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());


        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare( );
            // add VIDEO TO OUR LIST
            listOfVideoFiles.add(tempVideoFile);
            deleteOldestFile(SP_Data.getHowManyMoviesCanISave(),MEDIA_TYPE_VIDEO);
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    // mute or unmute photography making sound
    public void photoSoundButtonClicked(View view) {
        ImageView photoSoundButton = (ImageView) view;
        // demand whether mic is currently disable or enabled, THEN change ICON and SHARED PREF data
        if (SP_Data.getIfPhotoCaptureSoundEnabled()){
            Toast.makeText(this, R.string.photo_sound_disabled, Toast.LENGTH_SHORT).show();
            photoSoundButton.setImageResource(R.mipmap.speaker_disabled);
            SP_Data.setPhotoCaptureSound(false);
        }
        else {
            Toast.makeText(this, R.string.photo_sound_enabled, Toast.LENGTH_SHORT).show();
            photoSoundButton.setImageResource(R.mipmap.speaker_enabled);
            SP_Data.setPhotoCaptureSound(true);
        }
    }

    // enable or disable microphone recording while video record
    public void microphoneButtonClicked(View view) {
        ImageView microphoneButton = (ImageView) view;
        // demand whether mic is currently disable or enabled, THEN change ICON and SHARED PREF data
        if (sharedPref.getBoolean(getResources().getString(R.string.SP_is_volume_enable), false)){
            Toast.makeText(this, R.string.microphone_disabled, Toast.LENGTH_SHORT).show();
            microphoneButton.setImageResource(R.mipmap.microphone_disabled);
            // save SharedPref
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getResources().getString(R.string.SP_is_volume_enable),false);
            editor.apply();
        }
        // if we want to ENABLE Microphone, we must first check if we have permission to use it
        else {
            // check IF i have permission to use MICROPHONE
            // yes, we have PERMISSION to use MICROPHONE, let's proceed
            if (getPermissionsRecordAudio()) {
                Toast.makeText(this, R.string.microphone_enabled, Toast.LENGTH_SHORT).show();
                microphoneButton.setImageResource(R.mipmap.microphone_active);
                // save SharedPref
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getResources().getString(R.string.SP_is_volume_enable),true);
                editor.apply();
            }
        }
    }


    private boolean CameraIsCurrentlyRecording;
    private Thread myThread = null;
    //SurfaceHolder myHolder;
    private boolean isThatOk = true, switchBetweenTakingPhtAndVideo = false;
    private double lastPictureAtSeconds;
    private double cameraRecordStartedAtSeconds;
    @Override
    public void run() {

        try {
            Thread.sleep(RUN_TIME_PAUSE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        while (isThatOk) {

            // Run enable only if all needed permissions are granted
            if (ifAllThreePermissionNeededToRunGranted()) {


                // change screen color (to normal, after photoshot effect brightnnes screen)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Refresh Current Photo Camera and Video Camera TextView of resolution below buttons
                        if (refreshBottomResolutionsTV) {
                            refreshTextViewResolutionsBelowBottomPanelButtons();
                            refreshBottomResolutionsTV = false;
                        }

                        //stuff that updates ui
                        llMain.setBackgroundColor(getResources().getColor(R.color.transparentColor));
                    }
                });

                // consider THE ORIENTATION
                demandOrientationSettings();

                // switchBetweenTakingPhtAndVideo - you can either make photo or record video in one RUN ROUND
                if (switchBetweenTakingPhtAndVideo)
                    synchronized (this) {
                        // REFRESH PICTURING
                        takingPictureRefresh();
                    }
                else
                    synchronized (this) {
                        // VIDEO RECORD
                        recordVideoRefresh();
                    }
                switchBetweenTakingPhtAndVideo = !switchBetweenTakingPhtAndVideo;

            }


        }

        //Canvas myCanvas = myHolder.lockCanvas();
        //myHolder.unlockCanvasAndPost(myCanvas);
    }

    private void refreshTextViewResolutionsBelowBottomPanelButtons() {


        // Photo Camera Resolution
        String indexOfResolutionPhtCam = sharedPref.getString(getResources().
                getString(R.string.SP_photo_resolution),"0");
        phtCamResolutionTV.setText("("+photoSizesL.get(Integer.valueOf(indexOfResolutionPhtCam)).width + "x"
                + photoSizesL.get(Integer.valueOf(indexOfResolutionPhtCam)).height + ")");


        // Video Camera Resolution
        String indexOfQualityVideoCam = sharedPref.getString(getResources().
                getString(R.string.SP_video_quality),"0");

        // HQ
        if (Integer.valueOf(indexOfQualityVideoCam) == 0){
            videoCamResolutionTV.setText("("+cameraSizesL.get(0).width + "x"
                    + cameraSizesL.get(0).height + ")");
        }
        // LQ
        else if (Integer.valueOf(indexOfQualityVideoCam) == 1){
            videoCamResolutionTV.setText("("+cameraSizesL.get(cameraSizesL.size()-1).width + "x"
                    + cameraSizesL.get(cameraSizesL.size()-1).height + ")");
        }
        // CUSTOM
        else if (Integer.valueOf(indexOfQualityVideoCam) == 2){
            String customResolutionVideoCamera =
                    sharedPref.getString(getResources().getString(R.string.SP_video_resolution),"0");
            videoCamResolutionTV.setText("("+cameraSizesL.get(Integer.valueOf(customResolutionVideoCamera)).width + "x"
                    + cameraSizesL.get(Integer.valueOf(customResolutionVideoCamera)).height + ")");
        }
        else{
            String supportedDeviceResolutionVideoCamera =
                    sharedPref.getString(getResources().getString(R.string.SP_video_quality),"0");
            videoCamResolutionTV.setText("("+cameraSizesL.get(Integer.valueOf(supportedDeviceResolutionVideoCamera)-3).width + "x"
                    + cameraSizesL.get(Integer.valueOf(supportedDeviceResolutionVideoCamera)-3).height + ")");
        }



    }

    private boolean ifAllThreePermissionNeededToRunGranted() {
        boolean first = ContextCompat.checkSelfPermission
                (this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        boolean second = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;

        int third = -1;
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            third = Settings.System.canWrite(context) ? 1 : 0;




        synchronized (this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.System.canWrite(context)
                    && !dialogForGetPermissionIsCurShowing) {
                dialogForGetPermissionIsCurShowing = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        needSystemPermissionAD.show();
                    }
                });
            }
        }

        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED &&


            (ContextCompat.checkSelfPermission
                    (this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ||
                    Settings.System.canWrite(context))) &&   //warrning but line above we check it, so no way to get here API<23

            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED);
    }

    private void takingPictureRefresh() {
        synchronized (this) {
            if (makingPicturesActivated) {
                try {
                    // show TIME over screen that left to TAKE ANOTHER PHOTO SHOT
                    lastPictureAtSeconds = lastPictureAtSeconds == 0 ? System.currentTimeMillis() : lastPictureAtSeconds;
                    showTimeCounterToNextPhoto(SP_Data.getShowTimeOnShotPhoto()
                            , photoFrequency - (int) (System.currentTimeMillis() - lastPictureAtSeconds) / 1000);

                    // check IF take PHOTO
                    if ((System.currentTimeMillis() - lastPictureAtSeconds) / 1000 >= photoFrequency
                            || lastPictureAtSeconds == 0) {
                        // delete OLDEST PICTURE if too many
                        //deleteOldestFile(SP_Data.getHowManyPicturesCanISave(), MEDIA_TYPE_IMAGE);

                        // read DB picture resolution
                        Camera.Parameters localParams = myCameraObj.getParameters();
                        String indexOfSize = sharedPref.getString(getResources().
                                getString(R.string.SP_photo_resolution),"0");
                        localParams.setPictureSize(photoSizesL.get(Integer.valueOf(indexOfSize)).width,
                                photoSizesL.get(Integer.valueOf(indexOfSize)).height);

                        // set Focus Modes
                        String curFocusMode = sharedPref.getString
                                (getResources().getString(R.string.SP_photo_focus_mode),"default");
                        if (!curFocusMode.equals("default"))
                            localParams.setFocusMode
                                    (myCameraObj.getParameters().getSupportedFocusModes().get(Integer.parseInt(curFocusMode)));
                        myCameraObj.setParameters(localParams);

                        // DB has been readen
                        myCameraObj.takePicture(null, null, mPicture);
                        if (SP_Data.getIfPhotoCaptureSoundEnabled()) {
                            if (photoCaptureSound.isPlaying())
                                photoCaptureSound.seekTo(0);
                            else
                                photoCaptureSound.start();
                        }

                        // change screen color (photo visual effect)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //stuff that updates ui
                                llMain.setBackgroundColor(getResources().getColor(R.color.photoTakenColor));
                            }
                        });

                        // save last picture time
                        lastPictureAtSeconds = System.currentTimeMillis();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void recordVideoRefresh() {
        synchronized (this) {
            // if to record
            if (CameraIsCurrentlyRecording || makingVideoActivated) {
                Log.e("Record Time: ", ((System.currentTimeMillis() - cameraRecordStartedAtSeconds) / 1000) + " seconds");
                // show record TIME ON SCREEN (TextView)
                if (makingVideoActivated) {
                    int timeRecording = (int) (System.currentTimeMillis() - cameraRecordStartedAtSeconds) / 1000;
                    // x > y ? : ; - protect against timeRecording HUGE value, when cameraRecordStartedAtSeconds == 0
                    showTimeRecording(SP_Data.getShowRecTimeOnScreen(), timeRecording > videoRecordingTimeInSeconds ? 0 : timeRecording);
                }

                // --- TAKE CARE OF RECORD VIDEO ---
                // if ...STOP RECORDING
                if (!makingVideoActivated
                        || ((System.currentTimeMillis() - cameraRecordStartedAtSeconds) / 1000 >= videoRecordingTimeInSeconds
                        && cameraRecordStartedAtSeconds != 0)) {
                    // stop recording and release camera, TRY prevents CRASH in situation when user immediately STARTS and STOPS recording
                    try {
                        mMediaRecorder.stop();  // stop the recording
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("mMediaRecorder", "unable to call mMediaRecored.stop() method");
                    }
                    releaseMediaRecorder(); // release the MediaRecorder object
                    myCameraObj.lock();         // take camera access back from MediaRecorder
                    CameraIsCurrentlyRecording = false;

                    // reset camera time recording to 0
                    cameraRecordStartedAtSeconds = 0;
                }
                // else... START RECORDING
                else if (cameraRecordStartedAtSeconds == 0) {

                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        // delete OLDEST VIDEO if too many; +1 because we first created NEW file, and then we delete
                        //deleteOldestFile(SP_Data.getHowManyMoviesCanISave()+1, MEDIA_TYPE_VIDEO);
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording


                        mMediaRecorder.start();
                        // save camera start recording time
                        cameraRecordStartedAtSeconds = System.currentTimeMillis();

                        CameraIsCurrentlyRecording = true;
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        CameraIsCurrentlyRecording = false;
                        // inform user
                    }

                }
            }
        }
    }

    private boolean deleteOldestFile(int filesAmountLimit, int mediaType) {
        String TAG = "DELETE OLDEST FILE";

        boolean deleted = false;
        // pictures and videos are ORDERED from 0 index THE OLDEST FILE, to LAST INDEX - NEWEST FILE
        if (mediaType == MEDIA_TYPE_IMAGE) {

            if (listOfPictureFiles != null) {

                while (listOfPictureFiles.size() > filesAmountLimit) {
                    deleted = listOfPictureFiles.get(0).delete();
                    listOfPictureFiles.remove(0);
                }
            }
        }
        // pictures and videos are ORDERED from 0 index THE OLDEST FILE, to LAST INDEX - NEWEST FILE
        else if (mediaType == MEDIA_TYPE_VIDEO){
            if (listOfVideoFiles != null) {
                while (listOfVideoFiles.size() > filesAmountLimit) {
                    final String name = listOfVideoFiles.get(0).toString();
                    deleted = listOfVideoFiles.get(0).delete();
                    listOfVideoFiles.remove(0);

                    // REFRESH media system, to tell PHONE GALLERY that it was deleted
                    //refreshGallery(name);
                }
            }
        }
        else{
            Log.e(TAG, "Wrong mediatype sent into method.");
            return false;
        }
        return deleted;


    }

    public void refreshGallery(String fileUri) {

        // Convert to file Object
        File file = new File(fileUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Write Kitkat version specific code for add entry to gallery database
            // Check for file existence
            if (file.exists()) {
                // Add / Move File
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(new File(fileUri));
                mediaScanIntent.setData(contentUri);
                context.sendBroadcast(mediaScanIntent);
            } else {
                // Delete File
                try {
                    context.getContentResolver().delete(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + "='"
                                    + new File(fileUri).getPath() + "'", null);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    /**
     @param context : it is the reference where this method get called
     @param docPath : absolute path of file for which broadcast will be send to refresh media database
     **/
    public static void refreshSystemMediaScanDataBase(Context context, String docPath){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(docPath));
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private void showTimeCounterToNextPhoto(boolean showTimeOnShotPhoto, final int lastPictureTime) {
        final TextView tv_phtTimeCounter = (TextView) findViewById(R.id.tv_time_to_shot_photo);
        if (showTimeOnShotPhoto){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_phtTimeCounter.setVisibility(View.VISIBLE);
                    tv_phtTimeCounter.setText(lastPictureTime + "");
                }
            });
        }
        else
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_phtTimeCounter.setVisibility(View.INVISIBLE);
                }
            });
    }


    private void showTimeRecording(boolean showTimeRecordin, final int recordingTimeInSeconds) {

        final TextView tv_videoRecordTimeCounter = (TextView) findViewById(R.id.tv_time_recording);
        if (showTimeRecordin){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_videoRecordTimeCounter.setVisibility(View.VISIBLE);
                    String timeRecord = String.valueOf(recordingTimeInSeconds/600) +
                            (recordingTimeInSeconds/60)%10 + ":"
                            + ((recordingTimeInSeconds%60)/10)
                            + (recordingTimeInSeconds - (recordingTimeInSeconds/10)*10);
                    tv_videoRecordTimeCounter.setText(timeRecord);
                }
            });
        }
        else
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_videoRecordTimeCounter.setVisibility(View.INVISIBLE);
                }
            });
    }

    PowerManager.WakeLock wakeLock;
    KeyguardManager.KeyguardLock keyguardLock;
    SharedPreferences sharedPref;
    @Override
    protected void onResume() {
        isThatOk = true;
        refreshBottomResolutionsTV = true;

        // get SharedPref
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        context = this;

        // shen we come back from "Can modify system settings options", let program check again permissions
        dialogForGetPermissionIsCurShowing = false;

        // RUN function objects resume
        myThread = new Thread(this);
        myThread.start();


        // READ SHARED PREFERENCES DATA
        readSharedPrefData();

        // refresh resolution taken by VideoCam and PhotoCam (TextView below buttons)

        // GET GENERAL SETTINGS
        disableOrEnableLockScreen();
        super.onResume();
    }

    @Override
    protected void onStart() {
        if(getThreePermisions()){
            // if there is possibility to get PERMISSION for that particular DEVICE, then do so
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                        Settings.System.canWrite(context) &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
                                != PackageManager.PERMISSION_GRANTED
                        )
                    getPermissionToWriteSettings();
        }
        super.onStart();
    }

    private void initializateMostImportantObjectForAppWorking() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {



            // if API >= M permission granted in Manifest, if canWrite() is false then u cannot even use WRITE_SETTINGS
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                    Settings.System.canWrite(context) &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
                            == PackageManager.PERMISSION_GRANTED
                    )
                useAppBrightness();


            // only if INITIALIZING CAMERA was succesfull
            if (initializateMyCameraObj()) {
                // GET CAMERA SETTINGS
                getCameraSettings();
                initializateMyFileListsObjects();
                // CAPTURE PICTURES INIT
                initializatePicturesMaking();
            }
        }
    }

    private boolean getThreePermisions() {
        // Here, thisActivity is the current activity -- CAMERA PERMISSION
        // Write Settings only needed when API device is >=23
        if (
                ContextCompat.checkSelfPermission
                        (this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||

                        (ContextCompat.checkSelfPermission
                                (this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED
                                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                && Settings.System.canWrite(context)
                                ) ||

                        ContextCompat.checkSelfPermission
                                (this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||

                        ContextCompat.checkSelfPermission
                                (this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED

                ) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    // Camera Permission Explanation
                    Toast.makeText(this, R.string.camera_permisssion_explanation, Toast.LENGTH_LONG).show();

                }
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_SETTINGS},
                        MY_MULTIPLE_PERMISSIONS_REQUEST);

                return false;
            }
        // if PERMISSIONS are allowed from manifest (not dynamically), then use method to initializate objects
        else
            initializateMostImportantObjectForAppWorking();
        // in case API lvl <23 or permission granted, return true
        return true;
    }

    private void getCameraSettings() {

        Camera.Parameters params = myCameraObj.getParameters();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);



        // ##################################################################################################################
        // ######################################### OPTIONS CAMERA SECTION #################################################
        // ##################################################################################################################

        // GET SCENE MODE
        String sceneModeData  = sharedPref.getString(getResources().getString(R.string.SP_camera_mode), "empty");
        if (!sceneModeData.equals("empty")) {

            // if .getSupportedSceneModes == null means there's no support for scene modes
            List <String> supportedSceneModes = params.getSupportedSceneModes();
            if (supportedSceneModes != null)
                params.setSceneMode(supportedSceneModes.get(Integer.parseInt(sceneModeData)));

            // disactivate ALL
            for (LinearLayout item : modesListPanel)
                item.setActivated(false);


            // active particular MODE in MODES PANEL
            modesListPanel[Integer.parseInt(sceneModeData)].setActivated(true);


            switch(sceneModeData){
                case "0":
                    modeButtonToOpenPanelIcon.setImageResource(R.mipmap.not_set);
                    modeButtonToOpenPanelTV.setText(R.string.not_set);
                    break;
                case "1":
                    modeButtonToOpenPanelIcon.setImageResource(R.mipmap.daylight);
                    modeButtonToOpenPanelTV.setText(R.string.daylight);
                    break;
                case "2":
                    modeButtonToOpenPanelIcon.setImageResource(R.mipmap.sunny);
                    modeButtonToOpenPanelTV.setText(R.string.sunny);
                    break;
                case "3":
                    modeButtonToOpenPanelIcon.setImageResource(R.mipmap.cloudy);
                    modeButtonToOpenPanelTV.setText(R.string.cloudy);
                    break;
                case "4":
                    modeButtonToOpenPanelIcon.setImageResource(R.mipmap.moonlight);
                    modeButtonToOpenPanelTV.setText(R.string.moonlight);
                    break;
                case "5":
                    modeButtonToOpenPanelIcon.setImageResource(R.mipmap.dark_night);
                    modeButtonToOpenPanelTV.setText(R.string.dark_night);
                    break;
                case "6":
                    modeButtonToOpenPanelIcon.setImageResource(R.mipmap.city_night);
                    modeButtonToOpenPanelTV.setText(R.string.city_night);
                    break;
                case "7":
                    modeButtonToOpenPanelIcon.setImageResource(R.mipmap.custom);
                    modeButtonToOpenPanelTV.setText(R.string.custom);
                    break;
            }


        }

        // GET WHITE BALANCE
        String whiteBalanceData  = sharedPref.getString(getResources().getString(R.string.SP_camera_white_balance), "empty");
        // invoke when it's not EMPTY and when it's SETTED (0 index means - not set)
        if (!whiteBalanceData.equals("empty") && !whiteBalanceData.equals("0"))
            params.setWhiteBalance(params.getSupportedWhiteBalance()
                    .get(Integer.parseInt(whiteBalanceData)-1)); // - 1 cause "not set"


        // GET EXPOSURE COMPENSATION
        String indexOfExposureCompensationData  = sharedPref.getString(getResources()
                .getString(R.string.SP_camera_exposure_compensation), "empty");
        // invoke when it's not EMPTY and when it's SETTED (0 index means - not set)
        if (!indexOfExposureCompensationData.equals("empty") && !indexOfExposureCompensationData.equals("0")) {
            String exposureCompensationData  = String.valueOf(params.getMinExposureCompensation()
                    + (Integer.valueOf(indexOfExposureCompensationData)-1)); // - 1 cause "not set"
            params.setExposureCompensation(Integer.parseInt(exposureCompensationData));
        }


        // GET ANTIBANDING
        // invoke when it's not EMPTY and when it's SETTED (0 index means - not set)
        String antibandingData  = sharedPref.getString(getResources().getString(R.string.SP_camera_antibanding), "empty");
        if (!antibandingData.equals("empty") && !antibandingData.equals("0"))
            params.setAntibanding(params.getSupportedAntibanding().get(Integer.parseInt(antibandingData)-1));


        // GET FALLING IN CASE OF ERROR
        String fallingInCaseOfErrorsData  = sharedPref.getString(getResources()
                .getString(R.string.SP_camera_falling_in_case_of_error), "empty");
        if (!fallingInCaseOfErrorsData.equals("empty"))
            switch (fallingInCaseOfErrorsData) {
                // Continue with default camera mode
                case "0":
                    // not set
                    break;
                // Try again without scene mode
                case "1":

                    break;
                // Stop with message
                case "2":

                    break;
            }


        // ##################################################################################################################
        // ######################################### OPTIONS VIDEO SECTION #################################################
        // ##################################################################################################################


        // VIDEO CAMERA OPERATION
        /*String videoCameraOperationData  = sharedPref.getString(getResources().getString(R.string.SP_video_camera_operation), "empty");
        if (!videoCameraOperationData.equals("empty"))
            params.setVideoStabilization(videoCameraOperationData);*/

        // GET VIDEO STABILIZATION
        boolean videoStabilizationData  = sharedPref.getBoolean(getResources().getString(R.string.SP_video_stabilization), false);
        params.setVideoStabilization(videoStabilizationData);


        // GET VIDEO FILE LENGTH
        int videoRecordingTimeInSecondsIndex = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.SP_video_length_per_file),"5"));
        videoRecordingTimeInSeconds =
                Integer.valueOf(getResources().getStringArray(R.array.e_video_file_length_in_seconds)[videoRecordingTimeInSecondsIndex]);


        myCameraObj.setParameters(params);

    }


    private void demandOrientationSettings() {
        String currentOrientationSettings = sharedPref.getString(getResources().getString(R.string.SP_orientation), "0");
        switch (currentOrientationSettings){
            // Auto Orientation
            case "0":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                break;
            // Landscape orientation
            case "1":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            // Reverse Landscape orientation
            case "2":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            // Portrait orientation
            case "3":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            // Reverse Portrait orientation
            case "4":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            // External USB (camera)
            case "5":
                break;
            // External USB (camera) 180 degrees
            case "6":
                break;

        }
    }


    private void useAppBrightness() {
        if (sharedPref.getBoolean(getResources().getString(R.string.SP_general_brightness_seek_bar), false)){
            Float appBrightness =sharedPref.getFloat(getResources().getString(R.string.SP_general_brightness_seek_bar_intensity), (float)0.5);
            //System.out.println(appBrightness);
            Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                    Math.round(appBrightness*255)); // brightness 0 to 255
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = appBrightness < 0.01f ? 0.01f : appBrightness;// interval 0.0 to 1.0
            getWindow().setAttributes(lp);
        }
        else{
            int currentSystemBrightness = android.provider.Settings.System.
                    getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS,-1);

            Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, currentSystemBrightness); // brightness 0 to 255

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = (currentSystemBrightness/255.0f) < 0.01f ? 0.01f : (currentSystemBrightness/255.0f);// interval 0.0 to 1.0
            getWindow().setAttributes(lp);
        }
    }

    private void disableOrEnableLockScreen() {
        Boolean syncConnPref = sharedPref.getBoolean(getResources().getString(R.string.SP_general_screen_locked), false);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "INFO");
        wakeLock.acquire();

        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        keyguardLock = km.newKeyguardLock("name");

        if (syncConnPref)
            keyguardLock.reenableKeyguard();
        else
            keyguardLock.disableKeyguard();
    }

    private boolean initializateMyFileListsObjects() {
        File photoDirectory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), context.getString(R.string.car_cam_photos));
        File videoDirectory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), context.getString(R.string.car_cam_videos));

        boolean result = true;
        // check IF folders exists, if no TRY to create
        if (!photoDirectory.exists())
            if (!photoDirectory.mkdirs()){
                Log.d("Get Files From Memory", "failed to create PHOTO directory");
                result = false;
            }
        if (!videoDirectory.exists())
            if (!videoDirectory.mkdirs()){
                Log.d("Get Files From Memory", "failed to create VIDEO directory");
                result = false;
            }

        // if UNABLE TO READ DIRECTORIES, show ALERT and close app
        if (!result)
            new AlertDialog.Builder(context).setTitle(R.string.memory_error)
                .setMessage(R.string.cannot_read_files_folder)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create()
                .show();


        // initializate LISTS of FILES
        listOfPictureFiles = new ArrayList<>(Arrays.asList(photoDirectory.listFiles()));
        // bubble sorting, sort from 0 index OLDEST FILE, last index NEWEST FILE
        for (int i = 0; i < listOfPictureFiles.size(); ++i)
            for (int j = 1; j < listOfPictureFiles.size()-i;++j)
                if (listOfPictureFiles.get(j-1).lastModified()>listOfPictureFiles.get(j).lastModified())
                    Collections.swap(listOfPictureFiles,j-1,j);

        listOfVideoFiles = new ArrayList<>(Arrays.asList(videoDirectory.listFiles()));
        // bubble sorting, sort from 0 index OLDEST FILE, last index NEWEST FILE
        for (int i = 0; i < listOfVideoFiles.size(); ++i)
            for (int j = 1; j < listOfVideoFiles.size()-i;++j)
                if (listOfVideoFiles.get(j-1).lastModified()>listOfVideoFiles.get(j).lastModified())
                    Collections.swap(listOfVideoFiles,j-1,j);

        return result;
    }

    public static Camera getMyCameraObj() {
        return myCameraObj;
    }

    @Override
    protected void onPause() {
        super.onPause();

        keyguardLock.reenableKeyguard();
        wakeLock.release();

        // BROADCAST CRAHSES EMULATOR, BUT WE MUST TURN IT ON WHEN STOP TESTING ON EMULATOR
        // GOOGLE NOW PREVENTS FROM APP SENDING BROADCAST (SINCE 4.4 Android ? )
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));




        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event

        // RUN function objects pause
        isThatOk = false;
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // this line PREVENTS from CAMERA FREEZE after unlock screen
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();


        myThread = null;

    }


    @Override
    public void onBackPressed() {
        String whatToToWhenBackPressed = sharedPref.getString(getResources().getString(R.string.SP_general_back_button),"0");

        // if choose camera mode panel is opened, then onBackPressed just make it disappear and NOTHING MORE
        if (modeButtonToOpenPanel.isActivated()){
            // Inactive button that opens panel
            modeButtonToOpenPanel.setActivated(false);
            // HIDE Panel
            modeButtonFromChoosingPanel.setVisibility(View.GONE);
        }
        else {
            switch (whatToToWhenBackPressed) {
                case "0":
                    chooseWhatToDoWhenBackButtonClicked();
                    break;
                case "1":
                    if (myCameraObj != null)
                        myCameraObj.release();
                    finish();
                    super.onBackPressed();
                    break;
                case "2":
                    if (myCameraObj != null)
                        myCameraObj.release();
                    finish();
                    super.onBackPressed();
                    break;
                case "3":
                    closeAppAndContinueInBackground();
                    super.onBackPressed();
                    break;
            }
        }
    }

    private void chooseWhatToDoWhenBackButtonClicked() {
        View view = findViewById(R.id.activity_main);
        registerForContextMenu(view);
        openContextMenu(view);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_after_backbutton_clicked, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.continue_in_background:
                closeAppAndContinueInBackground();
                break;
            case R.id.exit_the_app:
                finish();
                super.onBackPressed();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void closeAppAndContinueInBackground() {

    }


    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            myCameraObj.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (myCameraObj != null){
            mPreview.getHolder().removeCallback(mPreview);
            myCameraObj.release();        // release the camera for other applications
            myCameraObj = null;
        }
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), context.getString(R.string.car_cam_photos));
        }
        else if(type == MEDIA_TYPE_VIDEO) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), context.getString(R.string.car_cam_videos));
        } else {
            return null;
        }

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void changeModeButtonClicked(View view) {

        // if NO CAMERA PERMISSION GRANTED
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            getPermissionsCamera();

        else {
            modeButtonToOpenPanel.setActivated(!modeButtonToOpenPanel.isActivated());
            // SHOW OR HIDE
            modeButtonFromChoosingPanel.setVisibility(modeButtonToOpenPanel.isActivated() ? View.VISIBLE : View.GONE);
        }
    }

    public void newModeButtonCliked(View view) {
        // SHOW OR HIDE
        modeButtonFromChoosingPanel.setVisibility(View.GONE);

        SharedPreferences.Editor editor = sharedPref.edit();

        // disactivate ALL
        for (LinearLayout item : modesListPanel)
            item.setActivated(false);





        // save SP data and ACTIVATE one selected
        switch(view.getId()) {
            case R.id.not_set_button_ll_id:
                editor.putString(getResources().getString(R.string.SP_camera_mode),"0");
                modesListPanel[0].setActivated(true);
                modeButtonToOpenPanelIcon.setImageResource(R.mipmap.not_set);
                modeButtonToOpenPanelTV.setText(R.string.not_set);
                break;
            case R.id.daylight_button_ll_id:
                editor.putString(getResources().getString(R.string.SP_camera_mode),"1");
                modesListPanel[1].setActivated(true);
                modeButtonToOpenPanelIcon.setImageResource(R.mipmap.daylight);
                modeButtonToOpenPanelTV.setText(R.string.daylight);
                break;
            case R.id.sunny_button_ll_id:
                editor.putString(getResources().getString(R.string.SP_camera_mode),"2");
                modesListPanel[2].setActivated(true);
                modeButtonToOpenPanelIcon.setImageResource(R.mipmap.sunny);
                modeButtonToOpenPanelTV.setText(R.string.sunny);
                break;
            case R.id.cloudy_button_ll_id:
                editor.putString(getResources().getString(R.string.SP_camera_mode),"3");
                modesListPanel[3].setActivated(true);
                modeButtonToOpenPanelIcon.setImageResource(R.mipmap.cloudy);
                modeButtonToOpenPanelTV.setText(R.string.cloudy);
                break;
            case R.id.moonlight_button_ll_id:
                editor.putString(getResources().getString(R.string.SP_camera_mode),"4");
                modesListPanel[4].setActivated(true);
                modeButtonToOpenPanelIcon.setImageResource(R.mipmap.moonlight);
                modeButtonToOpenPanelTV.setText(R.string.moonlight);
                break;
            case R.id.dark_night_button_ll_id:
                editor.putString(getResources().getString(R.string.SP_camera_mode),"5");
                modesListPanel[5].setActivated(true);
                modeButtonToOpenPanelIcon.setImageResource(R.mipmap.dark_night);
                modeButtonToOpenPanelTV.setText(R.string.dark_night);
                break;
            case R.id.city_night_button_ll_id:
                editor.putString(getResources().getString(R.string.SP_camera_mode),"6");
                modesListPanel[6].setActivated(true);
                modeButtonToOpenPanelIcon.setImageResource(R.mipmap.city_night);
                modeButtonToOpenPanelTV.setText(R.string.city_night);
                break;
            case R.id.custom_button_ll_id:
                editor.putString(getResources().getString(R.string.SP_camera_mode),"7");
                modesListPanel[7].setActivated(true);
                modeButtonToOpenPanelIcon.setImageResource(R.mipmap.custom);
                modeButtonToOpenPanelTV.setText(R.string.custom);
                break;
        }
        editor.apply();

        // disactive bottom panel button (Mode List Opening Button)
        modeButtonToOpenPanel.setActivated(false);
    }
}
