package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.CameraFragment;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.ErrorDialog;
import com.ckt.testauxiliarytool.utils.LogUtil;

import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Cc on 2017/11/21.
 */

public abstract class ManageCamera implements IManageCamera {

    private static final String ERROR_DIALOG = "dialog_no_camera";

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    CameraFragment mFragment;

    /**
     * A camera selector, we can use it to select the camera.
     */
    CameraSelect mCameraSelect;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    AutoFitTextureView mTextureView;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    Handler mBackgroundHandler;

    private Handler mUiHandler;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Orientation of the camera sensor
     */
    int mSensorOrientation;

    /**
     * The {@link Size} of camera preview.
     */
    Size mPreviewSize;

    /**
     * Whether the application in preview.
     */
    boolean mIsPreview;

    ManageCamera(CameraFragment cameraFragment, AutoFitTextureView textureView) {
        mFragment = cameraFragment;
        mTextureView = textureView;

        // Set camera config message.
        if (checkCameraHardware(mFragment.getContext())) {
            mCameraSelect = new CameraSelect(mFragment.getContext());
        }

    }

    /**
     * When the screen is turned off and turned back on, the SurfaceTexture is already
     * available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
     * a camera and start preview from here (otherwise, we wait until the surface is ready in
     * the SurfaceTextureListener).
     */
    @Override
    public void openCamera() {

        if (mCameraSelect == null) {
            return;
        } else if (mCameraSelect.getUsingCamera() == null) {
            // do not get data from current api

            return;
        }

        if (mTextureView.isAvailable()) {
            onOpenCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView
            .SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            LogUtil.i(IConstValue.TAG, "onSurfaceTextureAvailable, width:" + width + ", " + "" +
                    "" + "height:" + height);

            onOpenCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            LogUtil.i(IConstValue.TAG, "onSurfaceTextureSizeChanged, width:" + width + ", " +
                    "height:" + height);

            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**
     * Opens the camera specified by
     * {@link CameraData#mCameraId}.
     */
    private void onOpenCamera(int width, int height) {

        if (mBackgroundThread == null || mCameraSelect.getUsingCamera() == null) return;

        setUpCameraOutputs(width, height);
        configureTransform(width, height);

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            startCamera();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up member variables related to camera.
     */
    private void setUpCameraOutputs(int width, int height) {
        // Find out if we need to swap dimension to get the preview size relative to sensor
        // coordinate.
        int displayRotation = mFragment.getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();
        //no inspection Constant Conditions
        mSensorOrientation = getCameraDefaultSensorOrientation();
        boolean swappedDimensions = false;
        switch (displayRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                    swappedDimensions = true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                    swappedDimensions = true;
                }
                break;
            default:
                LogUtil.e(IConstValue.TAG, "Display rotation is invalid: " + displayRotation);
        }

        Point displaySize = new Point();
        mFragment.getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = swappedDimensions ? height : width;
        int rotatedPreviewHeight = swappedDimensions ? width : height;
        int maxPreviewWidth = swappedDimensions ? displaySize.y : displaySize.x;
        int maxPreviewHeight = swappedDimensions ? displaySize.x : displaySize.y;

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
            maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }

        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
            maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }

        // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
        // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
        // garbage capture data.
        mPreviewSize = mCameraSelect.getPreviewSize(rotatedPreviewWidth, rotatedPreviewHeight,
                maxPreviewWidth, maxPreviewHeight);

        // We fit the aspect ratio of TextureView to the size of preview we picked.
        int orientation = mFragment.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        } else {
            mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        }

    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize || null == mFragment.getActivity()) {
            return;
        }
        int rotation = mFragment.getActivity().getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float)
                    viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    @CallSuper
    void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraTestBackground");
        mBackgroundThread.start();
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            ErrorDialog.newInstance(mFragment.getString(R.string.ct_no_camera)).show(mFragment
                    .getChildFragmentManager(), ERROR_DIALOG);
            return false;
        }
    }

    public int getImageSizesLength() {
        return mCameraSelect == null ? 0 : mCameraSelect.getImageSizesLength();
    }

    public Size getImageSize() {
        return mCameraSelect == null ? new Size(0, 0) : mCameraSelect.getImageSize();
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    void showToast(String text) {
        if (mUiHandler == null) mUiHandler = new MessageHandler(Looper.getMainLooper(), this);
        mUiHandler.sendMessage(mUiHandler.obtainMessage(IConstValue.WHAT_SHOW_TOAST, text));
    }

    private void makeToast(String text, int duration) {
        Toast.makeText(mFragment.getContext().getApplicationContext(), text, duration).show();
    }

    @Override
    public void onFragmentResume() {
        startBackgroundThread();
        openCamera();
    }

    @Override
    public void onFragmentPause() {
        closeCamera();
        stopBackgroundThread();
    }

    /**
     * Get {@link #mIsPreview}
     *
     * @return true, when is previewing .Or false.
     */
    @Override
    public boolean isPreview() {
        return mIsPreview;
    }

    @Override
    public void closeCamera() {

    }

    /**
     * Switch front and back camera,Then set to default Camera size, Finally
     * call{@link #openCamera()}
     */
    @Override
    public void switchCamera() {
        closeCamera();
        mCameraSelect.switchCamera();
        openCamera();
    }

    /**
     * Switch to default camera.
     */
    @Override
    public void switchToDefaultCamera() {
        closeCamera();
        mCameraSelect.setToDefaultCamera();
        openCamera();
    }

    @Override
    public void switchResolution() {

    }

    /**
     * Capture a picture in HDR mode.
     */
    @Override
    public void captureInHDR() {
        if (mCameraSelect.getUsingCamera().isHdrSupported()) {
            makeToast("HDR模式拍照期间请勿移动相机！", Toast.LENGTH_SHORT);
            mBackgroundHandler.sendEmptyMessage(IConstValue.WHAT_CAPTURE_HDR_PICTURE);
        } else {
            makeToast("该摄像头不支持HDR模式！", Toast.LENGTH_SHORT);
        }
    }

    /**
     * After prepare, this method is truly open camera.
     */
    abstract void startCamera();

    abstract int getCameraDefaultSensorOrientation();

    abstract void startPreview();

    void captureStillHDRPicture() {
    }

    static class MessageHandler extends Handler {
        private final WeakReference<ManageCamera> mWeakManageCamera;

        MessageHandler(Looper looper, ManageCamera manageCamera) {
            super(looper);
            mWeakManageCamera = new WeakReference<>(manageCamera);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ManageCamera manageCamera = mWeakManageCamera.get();
            if (manageCamera == null) return;
            switch (msg.what) {
                case IConstValue.WHAT_START_PREVIEW:
                    if (manageCamera.mFragment.isResumed()) manageCamera.startPreview();
                    break;
                case IConstValue.WHAT_SHOW_TOAST:
                    // It is run in ui thread.
                    Activity activity = manageCamera.mFragment.getActivity();
                    if (activity != null)
                        Toast.makeText(activity.getApplicationContext(), (String) msg.obj, Toast
                                .LENGTH_SHORT).show();
                    break;
                case IConstValue.WHAT_CAPTURE_HDR_PICTURE:
                    if (manageCamera.mFragment.isResumed()){
                        manageCamera.captureStillHDRPicture();
                        manageCamera.mIsPreview = false;
                    }
                    break;
            }
        }
    }


}
