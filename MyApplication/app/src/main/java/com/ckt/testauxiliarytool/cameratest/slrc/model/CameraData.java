package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Range;
import android.util.Size;

import com.ckt.testauxiliarytool.utils.LogUtil;

/**
 * Created by Cc on 2017/10/17.
 */

public class CameraData {

    private String mCameraId;

    private Size[] mImageSizes;

    private Size[] mSurfaceSizes;

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    /**
     * Maximum and minimum exposure compensation values for android.control
     * .aeExposureCompensation, in counts of android.control.aeCompensationStep, that are
     * supported by this camera device.
     */
    private Range<Integer> mExposureRange;

    /**
     * Whether the current camera device supports HDR mode or not.
     */
    private boolean mHdrSupported;

    public String getCameraId() {
        return mCameraId;
    }

    public Size[] getImageSizes() {
        return mImageSizes;
    }

    public Size[] getSurfaceSizes() {
        return mSurfaceSizes;
    }

    public boolean isFlashSupported() {
        return mFlashSupported;
    }

    public Range<Integer> getExposureRange() {
        return mExposureRange;
    }

    public boolean isHdrSupported() {
        return mHdrSupported;
    }

    private CameraData(String cameraId, Size[] imageSizes, Size[] surfaceSizes, boolean
            flashSupported, Range<Integer> exposureRange, boolean hdrSupported) {
        mCameraId = cameraId;
        mImageSizes = imageSizes;
        mSurfaceSizes = surfaceSizes;
        mFlashSupported = flashSupported;
        mExposureRange = exposureRange;
        mHdrSupported = hdrSupported;
    }

    public static class Builder {

        private String mCameraId;

        private Size[] mImageSizes;

        private Size[] mSurfaceSizes;

        private boolean mFlashSupported;

        private Range<Integer> mExposureRange;

        private boolean mHdrSupported;

        public Builder() {
        }

        public Builder clean() {
            mCameraId = null;
            mImageSizes = null;
            mSurfaceSizes = null;
            mFlashSupported = false;
            mExposureRange = null;
            mHdrSupported = false;
            return this;
        }

        public Builder setCameraId(String cameraId) {
            mCameraId = cameraId;
            return this;
        }

        public Builder setImageSizes(Size[] imageSizes) {
            mImageSizes = imageSizes;
            return this;
        }

        public Builder setSurfaceSizes(Size[] surfaceSizes) {
            mSurfaceSizes = surfaceSizes;
            return this;
        }

        public Builder setFlashSupported(boolean flashSupported) {
            mFlashSupported = flashSupported;
            return this;
        }

        public Builder setExposureRange(Range<Integer> exposureRange) {
            mExposureRange = exposureRange;
            return this;
        }

        public Builder setHdrSupported(boolean hdrSupported) {
            mHdrSupported = hdrSupported;
            return this;
        }

        @Nullable
        public CameraData create(Context context, int lensFacing) {
            if (mCameraId == null || mImageSizes == null || mSurfaceSizes == null) {

                StringBuilder stringBuilder = new StringBuilder();
                if (mCameraId == null) {
                    stringBuilder.append("Front camera id not found!");
                }
                if (mImageSizes == null) {
                    stringBuilder.append("JPEG out put sizes is null!");
                }
                if (mSurfaceSizes == null) {
                    stringBuilder.append("SurfaceTexture out put sizes is null!");
                }

                LogUtil.e(IConstValue.TAG, stringBuilder.toString());

                return null;
            }

            String cameraName;
            if (lensFacing == CameraConfig.FACING_FRONT) {
                cameraName = "Front camera";
            } else {
                cameraName = "Back camera";
            }

            LogUtil.i(IConstValue.TAG, cameraName + ": flashSupported is " + (mFlashSupported
                    ? "support" : "not support") + ", HDR mode is " + (mHdrSupported ? "support"
                    : "not support"));

            return new CameraData(mCameraId, mImageSizes, mSurfaceSizes, mFlashSupported,
                    mExposureRange, mHdrSupported);
        }

    }

}
