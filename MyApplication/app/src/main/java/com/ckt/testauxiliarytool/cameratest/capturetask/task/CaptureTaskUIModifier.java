package com.ckt.testauxiliarytool.cameratest.capturetask.task;

/**
 * Created by asahi on 2017/11/23.
 */

public interface CaptureTaskUIModifier {

    void switchCamera();

    void restartFragment();

    void markTaskBtnStart(int index);

    void markTaskComplete(int index);

    void markTaskNotStart(int index);

    void initCRSCamera();

    void changeTip(String tip);

}
