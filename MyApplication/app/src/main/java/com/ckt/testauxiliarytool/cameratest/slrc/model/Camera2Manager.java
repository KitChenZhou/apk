package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Surface;

import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.CameraFragment;
import com.ckt.testauxiliarytool.utils.LogUtil;

import java.util.Arrays;

/**
 * Created by Cc on 2017/11/21.
 */

public abstract class Camera2Manager extends ManageCamera {

    /**
     * A ManageCamera to manage Camera.
     */
    private CameraManager mCameraManager;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    CameraCaptureSession mCaptureSession;

    private Surface mSurface;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    CameraDevice mCameraDevice;

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    CaptureRequest mPreviewRequest;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    ImageReader mImageReader;

    Camera2Manager(CameraFragment cameraFragment, AutoFitTextureView textureView) {
        super(cameraFragment, textureView);

        mCameraManager = (CameraManager) mFragment.getActivity().getSystemService(Context
                .CAMERA_SERVICE);

        if (mCameraManager == null) {
            LogUtil.e(IConstValue.TAG, "Can't get camera service!");
        }
    }

    abstract CameraCaptureSession.CaptureCallback setMCaptureCallback();

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    abstract CameraDevice.StateCallback setMStateCallback();

    @Override
    void startCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(mFragment.getActivity(), Manifest.permission
                    .CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraManager.openCamera(mCameraSelect.getUsingCamera().getCameraId(),
                    setMStateCallback(), mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    int getCameraDefaultSensorOrientation() {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics
                    (mCameraSelect.getUsingCamera().getCameraId());
            Integer integer = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            return integer == null ? 90 : integer;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    @Override
    void startPreview() {
        try {
            if (mCameraDevice == null) return;

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            if (texture == null) return;

            // Use a new ImageReader to save last one. After the new one create, close the older.
            if (mImageReader != null) mImageReader.close();
            mImageReader = ImageReader.newInstance(mCameraSelect.getImageSize().getWidth(),
                    mCameraSelect.getImageSize().getHeight(), ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(setMOnImageAvailableListener(),
                    mBackgroundHandler);

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            if (mSurface != null) mSurface.release();
            mSurface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice
                    .TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mSurface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface())
                    , new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                    if (null == mCameraDevice) return;

                    // When we switch resolution, mCaptureSession perhaps not close, so we close it.
                    if (mCaptureSession != null) mCaptureSession.close();

                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = cameraCaptureSession;
                    try {
                        // Auto focus should be continuous for camera preview.
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest
                                .CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        // Flash is automatically enabled when necessary.
                        setAutoFlash(mPreviewRequestBuilder);

                       /* setExposureCompensation(mPreviewRequestBuilder, mExposureRange.getUpper
                       ());*/

                        LogUtil.i(IConstValue.TAG, "startPreview, imageWidth:" + mCameraSelect
                                .getImageSize().getWidth() + ", imageHeight:" + mCameraSelect
                                .getImageSize().getHeight());

                        LogUtil.i(IConstValue.TAG, "startPreview, previewWidth:" + mPreviewSize
                                .getWidth() + ", previewHeight:" + mPreviewSize.getHeight());

                        LogUtil.i(IConstValue.TAG, "startPreview, TextureViewWidth:" +
                                mTextureView.getWidth() + ", TextureViewHeight:" + mTextureView
                                .getHeight());

                        // Finally, we start displaying the camera preview.
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, setMCaptureCallback
                                (), mBackgroundHandler);

                        //In preview, we set the status in true.
                        mIsPreview = true;

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        LogUtil.e(IConstValue.TAG, "Please don't press HOME button on " +
                                "camera" + " switch!", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    showToast("createCaptureSession Failed");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mCameraSelect.getUsingCamera().isFlashSupported()) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest
                    .CONTROL_AE_STATE_PRECAPTURE);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    @Override
    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mSurface) {
                mSurface.release();
                mSurface = null;
            }
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
            mIsPreview = false;
        }
    }

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    abstract ImageReader.OnImageAvailableListener setMOnImageAvailableListener();

}
