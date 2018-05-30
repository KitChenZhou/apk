package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.support.annotation.NonNull;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.ImageSaver;
import com.ckt.testauxiliarytool.cameratest.common.SoundPlayer;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.CameraFragment;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.SLRCFragment;
import com.ckt.testauxiliarytool.utils.BuildUtil;
import com.ckt.testauxiliarytool.utils.DateTimeUtils;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.MyConstants;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

import java.io.File;

/**
 * Created by Cc on 2017/11/22.
 */

public class SLRCCamera2Manager extends Camera2Manager {

    public SLRCCamera2Manager(CameraFragment cameraFragment, AutoFitTextureView textureView) {
        super(cameraFragment, textureView);
    }

    @Override
    void startBackgroundThread() {
        super.startBackgroundThread();
        mBackgroundHandler = new MessageHandler(mBackgroundThread.getLooper(), this);
    }

    @Override
    CameraCaptureSession.CaptureCallback setMCaptureCallback() {
        return mCaptureCallback;
    }

    @Override
    ImageReader.OnImageAvailableListener setMOnImageAvailableListener() {
        return mOnImageAvailableListener;
    }

    @Override
    CameraDevice.StateCallback setMStateCallback() {
        return mStateCallback;
    }

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new
            ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            File folder = new File(MyConstants.getStorageRootDir(mFragment.getContext()),
                    MyConstants.ROOT_DIR + "/" + MyConstants.CAMERA_DIR + "/CaptureHDR");
            if (!folder.exists() && !folder.mkdirs()) return;

            File file = new File(folder, "/IMG_" + DateTimeUtils.detailPictureFormat() + ".jpg");

            mBackgroundHandler.post(new ImageSaver(mFragment.getContext(), reader
                    .acquireLatestImage(), file));

            showToast("Saved: " + file);
            SoundPlayer.shoot();
        }

    };

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession
            .CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull TotalCaptureResult result) {
        }

    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened. We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;

            // The mTextureView is no time to change, so we need to wait 100ms.
            mBackgroundHandler.sendEmptyMessageDelayed(IConstValue.WHAT_START_PREVIEW, 100);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;

            mIsPreview = false;

            LogUtil.e(IConstValue.TAG, mFragment.getString(R.string.ct_open_camera_disconnected));

            SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, mFragment.getContext()).putInt
                    (IConstValue.KEY_USE_API, IConstValue.VALUE_USE_API_1);

            showToast(mFragment.getString(R.string.ct_open_camera_disconnected));

            mFragment.getActivity().finish();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;

            mIsPreview = false;

            LogUtil.e(IConstValue.TAG, "onError:error code " + error);

            SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, mFragment.getContext()).putInt
                    (IConstValue.KEY_USE_API, IConstValue.VALUE_USE_API_1);

            showToast(mFragment.getString(R.string.ct_open_camera_error));

            mFragment.getActivity().finish();

        }

    };

    /**
     * Capture a still picture in HDR mode. This method should be called when we get a response in
     * {@link #mCaptureCallback}.
     */
    @Override
    void captureStillHDRPicture() {
        try {
            if (null == mCameraDevice) {
                LogUtil.e(IConstValue.TAG, "Camera device is null!");
                return;
            }

            if (mBackgroundThread == null || mCameraSelect.getUsingCamera() == null) return;

            if (mFragment.getActivity() == null) {
                LogUtil.e(IConstValue.TAG, "getActivity() == null in SLRCFragment!");
                return;
            }

            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest
                    (CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same HDR modes as the capture.
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest
                    .CONTROL_MODE_USE_SCENE_MODE);

            captureBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest
                    .CONTROL_CAPTURE_INTENT_STILL_CAPTURE);

            setHdrMode(captureBuilder);

            // Orientation
            int rotation = mFragment.getActivity().getWindowManager().getDefaultDisplay()
                    .getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession
                    .CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                        CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    Integer mode = result.get(TotalCaptureResult.CONTROL_SCENE_MODE);

                    if (mode == null) {
                        LogUtil.i(IConstValue.TAG, "onCaptureCompleted: mode == null");
                    } else if (mode == TotalCaptureResult.CONTROL_SCENE_MODE_HDR) {
                        LogUtil.i(IConstValue.TAG, "onCaptureCompleted: mode == " +
                                "CONTROL_SCENE_MODE_HDR");
                    } else {
                        LogUtil.i(IConstValue.TAG, "onCaptureCompleted: mode == " + mode);
                    }

                    unlockFocus();
                }

            };

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setHdrMode(CaptureRequest.Builder requestBuilder) {
        if (mCameraSelect.getUsingCamera().isHdrSupported() && BuildUtil.isUseCamera2()) {
            requestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest
                    .CONTROL_SCENE_MODE_HDR);

            LogUtil.i(IConstValue.TAG, "setHdrMode: ");
        }
    }

    private void setExposureCompensation(CaptureRequest.Builder requestBuilder, Integer integer) {
        if (mCameraSelect.getUsingCamera().getExposureRange() != null) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, integer);
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata
                    .CONTROL_AF_TRIGGER_CANCEL);

            setAutoFlash(mPreviewRequestBuilder);

            /*setExposureCompensation(mPreviewRequestBuilder, mExposureRange.getUpper());*/

            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);

            mIsPreview = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch the resolution. It first switch back camera, then change front camera. Attention
     * the camera id and camera sizes must exist.
     */
    @Override
    public void switchResolution() {

        if (mCameraSelect.nextResolution()) {
            closeCamera();
            openCamera();
        } else {
            if (mCameraDevice == null) return;

            mBackgroundHandler.sendEmptyMessageDelayed(IConstValue.WHAT_START_PREVIEW, 100);
        }

        ((SLRCFragment) mFragment).setCurrentResolutionData(true);
    }

}
