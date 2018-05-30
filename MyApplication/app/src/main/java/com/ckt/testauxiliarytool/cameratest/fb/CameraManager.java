package com.ckt.testauxiliarytool.cameratest.fb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;

import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;

class CameraManager {
    private CameraHelper mCameraHelper;
    private Camera2Helper mCamera2Helper;

    CameraManager(Activity mActivity, AutoFitTextureView mTextureView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera2Helper = new Camera2Helper(mActivity, mTextureView);
            return;
        }
        mCameraHelper = new CameraHelper(mActivity, mTextureView);
    }

    //check texture
    void checkTexture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera2Helper.checkTexture();
            return;
        }
        mCameraHelper.checkTexture();
    }

    //switch camera
    void switchCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera2Helper.switchCamera();
            return;
        }
        mCameraHelper.switchCamera();
    }

    // take picture
    void takePicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera2Helper.takePicture();
            return;
        }
        mCameraHelper.takePicture();
    }

    //take picture with flash
    void takePictureWithFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera2Helper.takePictureWithFlash();
            return;
        }
        mCameraHelper.takePictureWithFlash();
    }

    //close camera
    void closeCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera2Helper.closeCamera();
            return;
        }
        mCameraHelper.closeCamera();
    }

    boolean isBackCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mCamera2Helper.isBackCamera();
        }
        return mCameraHelper.isBackCamera();
    }

    // Register BroadcastReceiver
    void registerReceiver(BroadcastReceiver broadcastReceiver, Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConstVar.CAPTURE_COMPLETED);
        intentFilter.addAction(ConstVar.CAMERA_OPENED);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }
}
