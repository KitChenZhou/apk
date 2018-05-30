package com.ckt.testauxiliarytool.utils;

import android.hardware.Camera;

import com.ckt.testauxiliarytool.cameratest.slrc.model.CameraSelect;

/**
 * Created by Cc on 2017/11/23.
 */

public class CameraUtil {

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(int facing) {
        Camera c = null;
        try {
            c = Camera.open(facing); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(CameraSelect cameraSelect) {

        if (cameraSelect == null || cameraSelect.getUsingCamera() == null) {
            return null;
        }

        return getCameraInstance(Integer.parseInt(cameraSelect.getUsingCamera().getCameraId()));
    }

}
