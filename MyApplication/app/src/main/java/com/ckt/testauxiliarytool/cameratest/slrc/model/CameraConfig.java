package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Range;
import android.util.Size;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.common.CompareSizesByArea;
import com.ckt.testauxiliarytool.utils.BuildUtil;
import com.ckt.testauxiliarytool.utils.CameraUtil;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Cc on 2017/9/7.
 */

public class CameraConfig {

    public static final int FACING_FRONT = 0;

    public static final int FACING_BACK = 1;

    private CameraData.Builder mFrontCamera;

    private CameraData.Builder mBackCamera;

    public CameraConfig(@NonNull Context context) {
        mFrontCamera = new CameraData.Builder();
        mBackCamera = new CameraData.Builder();

        int useApiLevel = SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, context).getInt
                (IConstValue.KEY_USE_API, IConstValue.VALUE_USE_API_DEFAULT);

        if (BuildUtil.isUseCamera2() && useApiLevel == IConstValue.VALUE_USE_API_2) {
            setCameraConfig(context);
        } else {
            setCameraConfig();
        }
    }

    @Nullable
    public CameraData getCameraData(@NonNull Context context, int lensFacing) {
        if (lensFacing == CameraConfig.FACING_FRONT)
            return mFrontCamera.create(context, lensFacing);
        else return mBackCamera.create(context, lensFacing);
    }

    /**
     * Set camera config. It include: The Camera id, ImageFormat sizes, SurfaceTexture sizes,
     * Camera Flash support, Camera HDR mode support and Camera explore range. Attention: if the
     * phone do not support Camera2, it will throwNullPointException.
     */
    private void setCameraConfig(@NonNull Context context) {

        mFrontCamera.clean();
        mBackCamera.clean();

        CameraManager cameraManager = (CameraManager) context.getSystemService(Context
                .CAMERA_SERVICE);

        if (cameraManager == null) return;

        try {

            CameraCharacteristics characteristics;
            StreamConfigurationMap map;

            for (String cameraId : cameraManager.getCameraIdList()) {
                characteristics = cameraManager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (facing == null) {
                    LogUtil.e(IConstValue.TAG, "Lens facing not found!");
                    return;
                }

                if (CameraCharacteristics.LENS_FACING_FRONT == facing) {
                    mFrontCamera.setCameraId(cameraId);

                    if (map == null) {
                        LogUtil.e(IConstValue.TAG, String.format(context.getString(R
                                .string.ct_configuration_map_not_found), "Front"));
                    } else {
                        setUpCameraConfig(mFrontCamera, characteristics, map);
                    }
                } else if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    mBackCamera.setCameraId(cameraId);

                    if (map == null) {
                        LogUtil.e(IConstValue.TAG, String.format(context.getString(R
                                .string.ct_configuration_map_not_found), "Back"));
                    } else {
                        setUpCameraConfig(mBackCamera, characteristics, map);
                    }

                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            LogUtil.e(IConstValue.TAG, "setUpCameraOutputs: " + context.getString(R
                    .string.ct_camera_error), e);
        }
    }

    /**
     * Get Camera ImageFormat sizes, SurfaceTexture sizes, Camera Flash support, Camera HDR mode
     * support and Camera explore range.
     *
     * @param characteristics CameraCharacteristics get from {@link CameraManager}, by using
     *                        camera id.
     * @param map             get from characteristics.
     */
    private void setUpCameraConfig(CameraData.Builder cameraData, @NonNull CameraCharacteristics
            characteristics, @NonNull StreamConfigurationMap map) {

        cameraData.setImageSizes(formatSizes(map.getOutputSizes(ImageFormat.JPEG)));
        cameraData.setSurfaceSizes(map.getOutputSizes(SurfaceTexture.class));

        // Check if the flash is supported.
        Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        cameraData.setFlashSupported(available == null ? false : available);

        cameraData.setExposureRange(characteristics.get(CameraCharacteristics
                .CONTROL_AE_COMPENSATION_RANGE));

        // Check if the HDR mode is supported.
        int[] scene_modes = characteristics.get(CameraCharacteristics
                .CONTROL_AVAILABLE_SCENE_MODES);

        if (scene_modes == null) {
            LogUtil.e(IConstValue.TAG, "setUpCameraOutputs: This device doesn't support " +
                    "" + "any " + "scene mode.");
        } else {
            for (int mode : scene_modes) {
                if (CaptureRequest.CONTROL_SCENE_MODE_HDR == mode) {
                    cameraData.setHdrSupported(true);
                }
            }
        }
    }


    /**
     * Set camera config. It include: The Camera id, ImageFormat sizes, SurfaceTexture sizes,
     * Camera Flash support, Camera HDR mode support and Camera explore range.
     */
    private void setCameraConfig() {
        mFrontCamera.clean();
        mBackCamera.clean();

        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBackCamera.setCameraId(String.valueOf(cameraInfo.facing));
                setUpCameraConfig(mBackCamera, cameraInfo.facing);
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCamera.setCameraId(String.valueOf(cameraInfo.facing));
                setUpCameraConfig(mFrontCamera, cameraInfo.facing);
            }
        }
    }

    /**
     * Get camera parameters and set up CameraData.
     */
    private void setUpCameraConfig(CameraData.Builder cameraData, int facing) {
        Camera camera = CameraUtil.getCameraInstance(facing);
        if (camera == null) return;

        Camera.Parameters parameters = camera.getParameters();
        cameraData.setSurfaceSizes(parseSizes(parameters.getSupportedPreviewSizes()))
                .setImageSizes(formatSizes(parseSizes(parameters.getSupportedPictureSizes())))
                .setFlashSupported(parameters.getSupportedFlashModes().contains(Camera.Parameters
                        .FLASH_MODE_AUTO)).setHdrSupported(parameters.getSupportedSceneModes()
                .contains(Camera.Parameters.SCENE_MODE_HDR));

        Range<Integer> exposureRange = new Range<>(parameters.getMinExposureCompensation(),
                parameters.getMaxExposureCompensation());

        cameraData.setExposureRange(exposureRange);

        camera.release();
    }

    private Size[] parseSizes(List<Camera.Size> cameraSizeList) {
        Camera.Size[] cameraSizes = new Camera.Size[cameraSizeList.size()];
        Size[] sizes = new Size[cameraSizeList.size()];
        cameraSizeList.toArray(cameraSizes);
        for (int i = 0; i < cameraSizes.length; i++) {
            sizes[i] = new Size(cameraSizes[i].width, cameraSizes[i].height);
        }

        return sizes;
    }

    /**
     * Format sizes. It first sort by aspect ratio, then sort by size area.
     *
     * @param sizes the sizes need to be formatted.
     * @return the format sizes.
     */
    private Size[] formatSizes(Size[] sizes) {

        if (sizes == null || sizes.length < 2) {
            return sizes;
        }

        Arrays.sort(sizes, new CompareSizesByArea());

        LinkedList<Size> linkedList = new LinkedList<>();

        linkedList.addLast(sizes[0]);
        linkedList.addLast(sizes[1]);

        boolean insert;

        for (int i = 2; i < sizes.length; i++) {

            insert = false;

            for (int j = 0; j < linkedList.size(); j++) {

                if (compare(linkedList.get(j), sizes[i]) > 0) {
                    linkedList.add(j, sizes[i]);
                    insert = true;
                    break;
                }
            }

            if (!insert) {
                linkedList.addLast(sizes[i]);
            }
        }

        return linkedList.toArray(sizes);
    }

    private int compare(Size lSize, Size rSize) {
        if (lSize.getWidth() < lSize.getHeight() * rSize.getWidth() / rSize.getHeight()) {
            return -1;
        } else if (lSize.getWidth() == lSize.getHeight() * rSize.getWidth() / rSize.getHeight()) {
            return 0;
        } else {
            return 1;
        }
    }
}
