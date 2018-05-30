package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Size;

import com.ckt.testauxiliarytool.cameratest.common.CompareSizesByArea;
import com.ckt.testauxiliarytool.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Cc on 2017/9/7.
 * <p>
 * The Camera configuration.
 */

public class CameraSelect {

    @Nullable
    private CameraData mFrontCamera;

    @Nullable
    private CameraData mBackCamera;

    @Nullable
    private Size mFrontDefaultSize;

    @Nullable
    private Size mBackDefaultSize;

    private CameraData mUsingCamera;

    private Size mImageSize;

    public CameraSelect(Context context) {
        CameraConfig cameraConfig = new CameraConfig(context);

        mFrontCamera = cameraConfig.getCameraData(context, CameraConfig.FACING_FRONT);
        mBackCamera = cameraConfig.getCameraData(context, CameraConfig.FACING_BACK);

        if (mFrontCamera != null)
            mFrontDefaultSize = Collections.max(Arrays.asList(mFrontCamera.getImageSizes()), new
                    CompareSizesByArea());
        if (mBackCamera != null)
            mBackDefaultSize = Collections.max(Arrays.asList(mBackCamera.getImageSizes()), new
                    CompareSizesByArea());

        setToDefaultCamera();
    }

    /**
     * Switch to default camera. We decide the back camera and it's maximum resolution as the
     * default.
     */
    public void setToDefaultCamera() {
        if (mBackCamera != null) {
            mUsingCamera = mBackCamera;
            mImageSize = mBackDefaultSize;
        } else if (mFrontCamera != null) {
            mUsingCamera = mFrontCamera;
            mImageSize = mFrontDefaultSize;
        }

        ResolutionChange.refreshIndex();
    }

    public CameraData getUsingCamera() {
        return mUsingCamera;
    }

    public Size getImageSize() {
        return mImageSize;
    }

    /**
     * Switch to next resolution. It first switch from back camera, then front camera.
     * <br/>
     * Attention: Using camera can't be null.
     *
     * @return True, means next resolution is different ratio from using one. Then we need to
     * start a new MainFragment.
     */
    public boolean nextResolution() {

        if (mUsingCamera == null) return true;

        if (ResolutionChange.nextImageSize(mUsingCamera)) {
            switchCamera();
            ResolutionChange.nextImageSize(mUsingCamera);
            mImageSize = ResolutionChange.getImageSize();
            return true;
        }

        mImageSize = ResolutionChange.getImageSize();

        return ResolutionChange.isDifResolutionRatio();
    }

    /**
     * Switch camera, if using camera is back camera, we switch to front camera. Then we set the
     * switch camera's default image size as using image size.
     */
    public boolean switchCamera() {
        if (mBackCamera != null && mFrontCamera != null) {
            mImageSize = mUsingCamera == mBackCamera ? mFrontDefaultSize : mBackDefaultSize;
            mUsingCamera = mUsingCamera == mBackCamera ? mFrontCamera : mBackCamera;
            return true;
        }
        return false;
    }

    /**
     * Get front and back(if we get them config messages) image sizes length.
     *
     * @return camera all image sizes length.
     */
    public int getImageSizesLength() {
        if (mFrontCamera != null && mBackCamera != null) {
            return mFrontCamera.getImageSizes().length + mBackCamera.getImageSizes().length;
        } else if (mFrontCamera != null) {
            return mFrontCamera.getImageSizes().length;
        } else if (mBackCamera != null) {
            return mBackCamera.getImageSizes().length;
        } else {
            return 0;
        }
    }

    @Nullable
    public Size getPreviewSize(int textureViewWidth, int textureViewHeight, int maxWidth, int
            maxHeight) {
        return chooseOptimalSize(mUsingCamera.getSurfaceSizes(), textureViewWidth,
                textureViewHeight, maxWidth, maxHeight, mImageSize);
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    @Nullable
    private Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight,
                                   int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option
                    .getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >=
                        textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            LogUtil.e(IConstValue.TAG, "Couldn't find any suitable preview size");

            Arrays.sort(choices, new CompareSizesByArea());

            Size choice = null;
            for (Size option : choices) {
                if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight) {
                    choice = option;
                    break;
                }
            }

            if (choice == null) {
                LogUtil.e(IConstValue.TAG, "Minimal preview size is larger than the size " +
                        "guaranteed by Camera2 API!");
                return null;
            }

            for (Size option : choices) {
                if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && Math.abs
                        (option.getWidth() * 1.0 / option.getHeight() - w * 1.0 / h) <= Math.abs
                        (choice.getWidth() * 1.0 / choice.getHeight() - w * 1.0 / h)) {
                    choice = option;
                }
            }

            return choice;
        }
    }

    private static class ResolutionChange {

        private static int sIndex;

        private static Size sImageSize;

        /**
         * If true, it means the using ImageSize is different resolution ratio from the next
         * ImageSize.
         */
        private static boolean sDifResolutionRatio;

        private static boolean isDifResolutionRatio() {
            return sDifResolutionRatio;
        }

        private static Size getImageSize() {
            return sImageSize;
        }

        private static int getIndex() {
            return sIndex;
        }

        public static void refreshIndex() {
            sIndex = 0;
        }

        /**
         * Set {@link #mImageSize} to next ImageSize from {@link CameraData}. And judge whether
         * next ImageSize is different resolution ratio from this ImageSize.
         *
         * @return Whether ImageSize is latest one. True means latest one.
         */
        private static boolean nextImageSize(@NonNull CameraData cameraData) {
            Size next;
            if (sIndex < cameraData.getImageSizes().length) {
                next = cameraData.getImageSizes()[sIndex++];
            } else {
                sIndex = 0;
                return true;
            }

            if (sImageSize == null) sImageSize = cameraData.getImageSizes()[0];

            sDifResolutionRatio = sImageSize.getWidth() != sImageSize.getHeight() * next.getWidth
                    () / next.getHeight();

            sImageSize = next;

            return false;
        }

    }


}
