package com.ckt.testauxiliarytool.cameratest.capturetask.api;

import android.os.Handler;

/**
 * Created by asahi on 2017/11/27.
 */

public interface ICameraApi {

    void openCamera(int width, int height);

    void closeCamera();

    void startBackgroundThread();

    void stopBackgroundThread();

    void initTextureView();

    void setHandler(Handler handler);

    void setReqName(String taskName);

    void takePicture();

    void switchCamera();

    void change2Record();
}
