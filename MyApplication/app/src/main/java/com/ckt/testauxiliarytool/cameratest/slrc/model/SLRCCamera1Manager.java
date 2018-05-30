package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.hardware.Camera;

import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.SoundPlayer;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.CameraFragment;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.SLRCFragment;
import com.ckt.testauxiliarytool.utils.DateTimeUtils;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.MyConstants;

import java.io.File;

/**
 * Created by Cc on 2017/11/24.
 */

public class SLRCCamera1Manager extends Camera1Manager {

    public SLRCCamera1Manager(CameraFragment cameraFragment, AutoFitTextureView textureView) {
        super(cameraFragment, textureView);
    }

    @Override
    void startBackgroundThread() {
        super.startBackgroundThread();
        mBackgroundHandler = new MessageHandler(mBackgroundThread.getLooper(), this);
    }

    private final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File folder = new File(MyConstants.getStorageRootDir(mFragment.getContext()),
                    MyConstants.ROOT_DIR + "/" + MyConstants.CAMERA_DIR + "/CaptureHDR");
            if (!folder.exists() && !folder.mkdirs()) return;

            File file = new File(folder, "/IMG_" + DateTimeUtils.detailPictureFormat() + ".jpg");

            int rotation = mFragment.getActivity().getWindowManager().getDefaultDisplay()
                    .getRotation();

            mBackgroundHandler.post(new BitmapSaver(mFragment.getContext(), data, file).preRotate
                    (getOrientation(rotation)));

            showToast("Saved: " + file);
            SoundPlayer.shoot();

            mCamera.startPreview();

            mIsPreview = true;
        }
    };

    @Override
    void captureStillHDRPicture() {
        if (mCamera == null) return;
        Camera.Parameters parameters = mCamera.getParameters();

        if (parameters.getSupportedSceneModes().contains(Camera.Parameters.SCENE_MODE_HDR)) {
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
        }

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        parameters.setPictureSize(mCameraSelect.getImageSize().getWidth(), mCameraSelect
                .getImageSize().getHeight());

        mCamera.setParameters(parameters);
        mCamera.takePicture(null, null, mPictureCallback);
    }

    /**
     * Switch the resolution. It first switch back camera, then change front camera. Attention
     * the camera id and camera sizes must exist.
     * <P/>
     * Attention: If last image size's aspect ratio is the same as this one, we do not do anything.
     */
    @Override
    public void switchResolution() {

        if (mCameraSelect.nextResolution()) {
            closeCamera();
            openCamera();
        } else {
            LogUtil.i(IConstValue.TAG, "startPreview, imageWidth:" + mCameraSelect
                    .getImageSize().getWidth() + ", imageHeight:" + mCameraSelect
                    .getImageSize().getHeight());

            LogUtil.i(IConstValue.TAG, "startPreview, previewWidth:" + mPreviewSize.getWidth
                    () + ", previewHeight:" + mPreviewSize.getHeight());

            LogUtil.i(IConstValue.TAG, "startPreview, TextureViewWidth:" + mTextureView
                    .getWidth() + ", TextureViewHeight:" + mTextureView.getHeight());
        }

        ((SLRCFragment) mFragment).setCurrentResolutionData(true);
    }

}
