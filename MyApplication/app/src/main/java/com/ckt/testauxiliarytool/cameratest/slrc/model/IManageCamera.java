package com.ckt.testauxiliarytool.cameratest.slrc.model;

/**
 * Created by Cc on 2017/11/21.
 */

public interface IManageCamera {

    void onFragmentResume();

    void onFragmentPause();

    boolean isPreview();

    void openCamera();

    void closeCamera();

    void switchCamera();

    void switchToDefaultCamera();

    void switchResolution();

    void captureInHDR();

}
