package com.ckt.testauxiliarytool.cameratest.slrc.model;

/**
 * Created by Cc on 2017/12/12.
 */

public interface IConstValue {
    /**
     * Current using camera api level's save key
     */
    String KEY_USE_API = "use_api";

    String KEY_REPEAT_TIMES = "repeat_times";

    /**
     * Current using camera api level's save and default value
     */
    int VALUE_USE_API_1 = 1;
    int VALUE_USE_API_2 = 2;
    int VALUE_USE_API_DEFAULT = 2;

    /**
     * Repeat times default value
     */
    int VALUE_REPEAT_TIMES_DEFAULT = 20;

    String CAMERA_PREFERENCE = "camera_preference";

    String TAG = "CameraTest";

    int WHAT_START_PREVIEW = 0;
    int WHAT_SHOW_TOAST = 1;
    int WHAT_CAPTURE_HDR_PICTURE = 2;
}
