package com.ckt.testauxiliarytool.cameratest.fb;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.CompareSizesByArea;
import com.ckt.testauxiliarytool.utils.MyConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by D22431 on 2017/12/13.
 */

abstract class BaseHelper {
    static final String LOW_POWER_ACTIVITY_NAME =
            "cameratest.fb.LowPowerTakePhoto";
    static final String START_CAMERA_ACTIVITY_NAME =
            "cameratest.fb.StartCameraActivity";
    static final String EXIT_CAMERA_ACTIVITY_NAME =
            "cameratest.fb.UnusuallyExitCamera";
    static final String TAG = "BaseHelper";
    private static final double[] RATIOS =
            new double[]{1.3333, 1.5, 1.6667, 1.7778};
    static final SparseIntArray ORIENTATIONS;
    private static final double ASPECT_TOLERANCE = 0.02;

    static {
        ORIENTATIONS = new SparseIntArray();
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    Semaphore mCameraOpenCloseLock = new Semaphore(1);
    HandlerThread mHandlerThread;
    Handler mHandler;
    Size mPreviewSize;
    AutoFitTextureView mTextureView;
    int mRotation = Surface.ROTATION_0;
    int mWidth = 0, mHeight = 0;
    Point mDisplaySize = new Point();
    String mCameraId;
    Activity mActivity;

    //open camera
    abstract void openCamera();

    //
    abstract void checkTexture();

    //switch camera
    abstract void switchCamera();

    //take picture
    abstract void takePicture();

    //take picture with flash
    abstract void takePictureWithFlash();

    //
    abstract boolean isBackCamera();

    //close camera
    abstract void closeCamera();

    //start thread
    void startThread() {
        mHandlerThread = new HandlerThread("CameraHelper");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    //stop thread
    void stopThread() {
        if (mHandlerThread != null) {
            try {
                mHandlerThread.quitSafely();
                mHandlerThread.quit();
                mHandlerThread.join();
                mHandlerThread = null;
                mHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getStoragePath() {
        String path = "";
        path += "/" + MyConstants.ROOT_DIR + "/" + MyConstants.CAMERA_DIR;
        switch (mActivity.getLocalClassName()) {
            case START_CAMERA_ACTIVITY_NAME:
                path += "/StartCamera";
                break;
            case EXIT_CAMERA_ACTIVITY_NAME:
                path += "/AbnormalExitCamera";
                break;
            case LOW_POWER_ACTIVITY_NAME:
                path += "/LowPowerTakePhotoWithFlash";
                break;
        }
        return path;
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0,
                mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == mRotation || Surface.ROTATION_270 == mRotation) {
            bufferRect.offset(centerX - bufferRect.centerX(),
                    centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (mRotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == mRotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    double findFullscreenRatio(Context context) {
        double find = 4d / 3;
        WindowManager wm = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        double fullscreen;
        if (point.x > point.y) {
            fullscreen = (double) point.x / point.y;
        } else {
            fullscreen = (double) point.y / point.x;
        }
        Log.i(TAG, "fullscreen = " + fullscreen +
                " x = " + point.x + " y = " + point.y);
        for (double RATIO : RATIOS) {
            if (Math.abs(RATIO - fullscreen) < Math.abs(fullscreen - find)) {
                find = RATIO;
            }
        }
        Log.d(TAG, "findFullscreenRation, return ratio:" + find);
        return find;
    }


    Camera.Size getOptimalPreviewSize(Context context,
                                      List<Camera.Size> sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        // final double EXACTLY_EQUAL = 0.001;
        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        double minDiffWidth = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of preview surface. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size.
        WindowManager wm = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int panelHeight = Math.min(point.x, point.y);
        int panelWidth = Math.max(point.x, point.y);

        Camera.Size bestMatchPanelSize =
                findBestMatchPanelSize(sizes, targetRatio, panelWidth, panelHeight);
        if (bestMatchPanelSize != null) {
            return bestMatchPanelSize;
        }

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - panelHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - panelHeight);
                minDiffWidth = Math.abs(size.width - panelWidth);
            } else if ((Math.abs(size.height - panelHeight) == minDiff)
                    && Math.abs(size.width - panelWidth) < minDiffWidth) {
                optimalSize = size;
                minDiffWidth = Math.abs(size.width - panelWidth);
            }
        }
        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        // M: This will happen when native return video size and wallpaper
        // want to get specified ratio.
        if (optimalSize == null) {
            Log.w(TAG, "No preview size match the aspect ratio" + targetRatio + ","
                    + "then use the standard(4:3) preview size");
            minDiff = Double.MAX_VALUE;
            targetRatio = 4d / 3;
            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                    continue;
                }
                if (Math.abs(size.height - panelHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - panelHeight);
                }
            }
        }
        return optimalSize;
    }

    Camera.Size findBestMatchPanelSize(
            List<Camera.Size> sizes, double targetRatio,
            int panelWidth, int panelHeight) {
        double minDiff = Double.MAX_VALUE;
        double minDiffWidth = Double.MAX_VALUE;
        double panelAspectRatio = (double) panelWidth / panelHeight;
        Camera.Size bestMatchSize = null;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            // filter out the size which not tolerated by target ratio
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            // when target aspect ratio is the same with panel size aspect ratio
            // find no less than panel size's preview size
            if (Math.abs(panelAspectRatio - targetRatio) <= ASPECT_TOLERANCE &&
                    (panelHeight > size.height || panelWidth > size.width)) {
                // filter out less than panel size
                continue;
            }
            // find the size closest to panel size
            if (Math.abs(size.height - panelHeight) < minDiff) {
                bestMatchSize = size;
                minDiff = Math.abs(size.height - panelHeight);
                minDiffWidth = Math.abs(size.width - panelWidth);
            } else if ((Math.abs(size.height - panelHeight) == minDiff)
                    && Math.abs(size.width - panelWidth) < minDiffWidth) {
                bestMatchSize = size;
                minDiffWidth = Math.abs(size.width - panelWidth);
            }
        }
        return bestMatchSize;
    }

    static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int
            textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
        if (choices == null) return null;
        // Collect the supported resolutions that
        // are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions
        // that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth &&
                    option.getHeight() <= maxHeight && option
                    .getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth
                        && option.getHeight() >= textureViewHeight) {
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
            Log.e("mPreviewSize", "Couldn't find any suitable preview size");
            Arrays.sort(choices, new CompareSizesByArea());

            Size choice = null;
            for (Size option : choices) {
                if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight) {
                    choice = option;
                    break;
                }
            }

            for (Size option : choices) {
                assert choice != null;
                if (option.getWidth() <= maxWidth &&
                        option.getHeight() <= maxHeight && Math.abs
                        (option.getWidth() * 1.0 /
                                option.getHeight() - w * 1.0 / h)
                        <= Math.abs(choice.getWidth() * 1.0 /
                        choice.getHeight() - w * 1.0 / h)) {
                    choice = option;
                }
            }

            if (choice != null) {
                return choice;
            } else {
                return choices[0];
            }
        }
    }
}
