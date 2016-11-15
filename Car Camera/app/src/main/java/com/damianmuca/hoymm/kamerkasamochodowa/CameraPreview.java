package com.damianmuca.hoymm.kamerkasamochodowa;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.nfc.Tag;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Hoymm on 2016-08-30.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private boolean isPreviewRunning = false;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private final String TAG = "Camera Preview Class: ";
    private Camera.Parameters l_parameters;

    public CameraPreview(Context context, Camera mCamera, Camera.Parameters cameraParams) {
        super(context);
        this.mCamera = mCamera;

        // cpy camara parameters
        l_parameters = cameraParams;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android version prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }


        // ADJUST Orientation for ALL DEVICES
        Display display = ((WindowManager)MainActivity.context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0)
        {
            l_parameters.setPreviewSize(getHeight(), getWidth());
            mCamera.setDisplayOrientation(90);
        }

        else if(display.getRotation() == Surface.ROTATION_90)
        {
            l_parameters.setPreviewSize( getWidth(), getHeight());
        }

        else if(display.getRotation() == Surface.ROTATION_180)
        {
            l_parameters.setPreviewSize(getHeight(),  getWidth());
        }

        else if(display.getRotation() == Surface.ROTATION_270)
        {
            l_parameters.setPreviewSize(getWidth(), getHeight());
            mCamera.setDisplayOrientation(180);
        }

        mCamera.setParameters(l_parameters);
        previewCamera();

    }


    public void previewCamera()
    {


        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try
        {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            isPreviewRunning = true;
        }
        catch(Exception e)
        {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (mCamera != null) {
            mCamera.release();
        }
    }


}


