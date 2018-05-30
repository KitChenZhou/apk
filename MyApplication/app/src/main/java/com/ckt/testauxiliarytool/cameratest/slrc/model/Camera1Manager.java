package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.CameraFragment;
import com.ckt.testauxiliarytool.utils.CameraUtil;
import com.ckt.testauxiliarytool.utils.LogUtil;

import java.io.IOException;

/**
 * Created by Cc on 2017/11/23.
 */

public abstract class Camera1Manager extends ManageCamera {

    Camera mCamera;

    Camera1Manager(CameraFragment cameraFragment, AutoFitTextureView textureView) {
        super(cameraFragment, textureView);
    }

    @Override
    int getCameraDefaultSensorOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Integer.parseInt(mCameraSelect.getUsingCamera().getCameraId()), info);
        return info.orientation;
    }

    @Override
    void startCamera() {
        mCamera = CameraUtil.getCameraInstance(mCameraSelect);
        mCameraOpenCloseLock.release();

        mBackgroundHandler.sendEmptyMessageDelayed(IConstValue.WHAT_START_PREVIEW, 100);
    }

    @Override
    void startPreview() {
        if (mCamera == null) return;

        Camera.Parameters parameters = mCamera.getParameters();

        LogUtil.i(IConstValue.TAG, "startPreview, imageWidth:" + mCameraSelect.getImageSize()
                .getWidth() + ", imageHeight:" + mCameraSelect.getImageSize().getHeight());

        LogUtil.i(IConstValue.TAG, "startPreview, previewWidth:" + mPreviewSize.getWidth() + ", "
                + "previewHeight:" + mPreviewSize.getHeight());

        LogUtil.i(IConstValue.TAG, "startPreview, TextureViewWidth:" + mTextureView.getWidth() +
                ", TextureViewHeight:" + mTextureView.getHeight());

        parameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        if (mCameraSelect.getUsingCamera().isFlashSupported()) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }

        mCamera.setParameters(parameters);

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (texture == null) return;

        // We configure the size of default buffer to be the size of camera preview we want.
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        final int rotation = mFragment.getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();

        int orientation;

        if (Integer.parseInt(mCameraSelect.getUsingCamera().getCameraId()) == Camera.CameraInfo
                .CAMERA_FACING_BACK) {
            orientation = (mSensorOrientation - rotation * 90 + 360) % 360;
        } else {
            orientation = (mSensorOrientation + rotation * 90) % 360;
            orientation = (360 - orientation) % 360;
        }

        mCamera.setDisplayOrientation(orientation);

        try {
            mCamera.setPreviewTexture(texture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();

        mIsPreview = true;
    }

    @Override
    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
            mIsPreview = false;
        }
    }
}
