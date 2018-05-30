package com.ckt.testauxiliarytool.cameratest.fb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.utils.MyConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.ckt.testauxiliarytool.cameratest.fb.Tool.createFileDirectory;

/**
 * Created by D22431 on 2017/12/11.
 */

class CameraHelper extends BaseHelper {
    private static final int MSG_OPEN_CAMERA = 2;
    private static final int MSG_TAKE_PICTURE = 3;
    private static final int MSG_TAKE_PICTURE_WITH_FLASH = 4;

    private static final int FACING_BACK_CAMERA_ID = 0;
    private static final int FACING_FRONT_CAMERA_ID = 1;

    private Camera mCamera;
    private int mCameraId = FACING_BACK_CAMERA_ID;
    private boolean mIsPreviewing = false;
    private Camera.Parameters mParameters;
    private String mFlashMode;
    private MySurfaceTextureListener mTextureListener;
    private OrientationListener mOrientationListener;

    CameraHelper(Activity activity, AutoFitTextureView autoFitTextureView) {
        this.mActivity = activity;
        mTextureView = autoFitTextureView;

        mOrientationListener = new OrientationListener(mActivity);
        mTextureListener = new MySurfaceTextureListener();
        mTextureView.setSurfaceTextureListener(mTextureListener);
        mRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
    }

    private void initHandler() {
        super.startThread();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_OPEN_CAMERA:
                        openCameraInner();
                        break;
                    case MSG_TAKE_PICTURE:
                        takePictureInner();
                        break;
                    case MSG_TAKE_PICTURE_WITH_FLASH:
                        takePictureWithFlashInner();
                        break;
                }
            }
        };
    }

    //Check if this device has a camera
    private boolean hasCameraHardware() {
        return mActivity.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    @Override
    void openCamera() {
        mCameraOpenCloseLock.release();
        if (mHandlerThread == null) {
            initHandler();
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_OPEN_CAMERA);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void openCameraInner() {
        if (!hasCameraHardware()) return;
        if (mCamera != null) {
            if (mIsPreviewing) {
                mCamera.stopPreview();
                mIsPreviewing = false;
            }
            mCamera.release();
        }
        mCamera = Camera.open(mCameraId);
        mParameters = mCamera.getParameters();
        if (!isFlashSupported() && mActivity.getLocalClassName()
                .equals(LOW_POWER_ACTIVITY_NAME)) {
            Toast.makeText(mActivity.getApplication(),
                    mActivity.getString(R.string.ct_flashlight_not_support),
                    Toast.LENGTH_SHORT).show();
        }
        //set picture format
        mParameters.setPictureFormat(PixelFormat.JPEG);
        //preview size
        List<Camera.Size> previewSizeList = mParameters.getSupportedPreviewSizes();
        mActivity.getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
        Double targetRatio = findFullscreenRatio(mActivity);
        Camera.Size size = getOptimalPreviewSize(mActivity, previewSizeList, targetRatio);
        mPreviewSize = new Size(size.width, size.height);
        //picture size
        List<Camera.Size> pictureSizeList = mParameters.getSupportedPictureSizes();
        //use largest size
        Camera.Size pictureSize = Tool.getMaxSize(pictureSizeList);
        mParameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mParameters.setPictureSize(pictureSize.width, pictureSize.height);
        //
        setFocus();
        followScreenOrientation(mActivity, mCamera);
        mOrientationListener.enable();
        mCamera.setParameters(mParameters);
        //configureTransform(mSurfaceWidth, mSurfaceHeight);

        setUpPreview();
        startPreview(mCamera);
        mIsPreviewing = true;
        //camera opened
        Intent intent = new Intent(ConstVar.CAMERA_OPENED);
        mActivity.sendBroadcast(intent);
    }

    @Override
    void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (mCamera != null) {
                if (mIsPreviewing) {
                    mCamera.stopPreview();
                    mIsPreviewing = false;
                }
                releaseCamera();
                //warning!!! if don't disable it, preview will be not fluent
                mOrientationListener.disable();
            }
            super.stopThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }

    }

    private void startPreview(final Camera camera) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.startPreview();
                }
            }
        }).start();
    }

    /**
     * switch camera
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    void switchCamera() {
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount > 1) {
            mCameraId = mCameraId == FACING_BACK_CAMERA_ID ?
                    FACING_FRONT_CAMERA_ID : FACING_BACK_CAMERA_ID;
            closeCamera();
            checkTexture();
        } else if (cameraCount == 1) {
            Toast.makeText(mActivity.getApplication(),
                    "sorry, only one camera is available",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    void checkTexture() {
        if (!mTextureView.isAvailable()) {
            mTextureView.setSurfaceTextureListener(mTextureListener);
        } else if (mCamera == null) {
            openCamera();
        }
    }

    private void setUpPreview() {
        if (mIsPreviewing) {
            return;
        }
        if (mCamera != null) {
            try {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                if (null == texture) return;
                mCamera.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    boolean isBackCamera() {
        return mCameraId == FACING_BACK_CAMERA_ID;
    }


    private void followScreenOrientation(Context context, Camera camera) {
        final int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(180);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(90);
        }
    }

    @Override
    void takePicture() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_TAKE_PICTURE);
        }
    }

    private void takePictureInner() {
        if (mCamera == null) {
            return;
        }
        setFlash(Camera.Parameters.FLASH_MODE_OFF);
        //take picture
        mCamera.takePicture(shutterCallback, null, pictureCallback);
    }

    @Override
    void takePictureWithFlash() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_TAKE_PICTURE_WITH_FLASH);
        }
    }

    private void takePictureWithFlashInner() {
        if (mCamera == null) {
            return;
        }
        setFlash(Camera.Parameters.FLASH_MODE_ON);
        //take picture
        mCamera.takePicture(shutterCallback, null, pictureCallback);
    }

    private Camera.ShutterCallback shutterCallback =
            new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                }
            };

    private Camera.PictureCallback pictureCallback =
            new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] data, Camera camera) {
                    storePictureData(data);
                    startPreview(camera);
                }
            };

    //release camera
    private void releaseCamera() {
        mCamera.release();
        mCamera = null;
    }

    //texture view listener
    private class MySurfaceTextureListener implements
            TextureView.SurfaceTextureListener {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onSurfaceTextureAvailable(
                SurfaceTexture surface, int width, int height) {
            mWidth = width;
            mHeight = height;
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(
                SurfaceTexture surface, int width, int height) {
            mWidth = width;
            mHeight = height;
            configureTransform(mWidth, mHeight);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            closeCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

    //set focus mode
    private void setFocus() {
        final List<String> modes = mParameters.getSupportedFocusModes();
        if (modes == null) return;
        if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            mParameters.setFocusMode(modes.get(0));
        }
    }

    // set flash mode
    private void setFlash(String flashMode) {
        if (mCamera != null) {
            mParameters = mCamera.getParameters();
            List<String> modes = mParameters.getSupportedFlashModes();
            if (modes != null && modes.contains(flashMode)) {
                mParameters.setFlashMode(flashMode);
                mFlashMode = flashMode;
            }

            if (modes == null || !modes.contains(mFlashMode)) {
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
            }
            mCamera.setParameters(mParameters);
        }
    }

    private boolean isFlashSupported() {
        List<String> modes = mParameters.getSupportedFlashModes();
        if (modes == null || !modes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            return false;
        }
        return true;
    }

    //set photo orientation
    private class OrientationListener extends OrientationEventListener {

        OrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (ORIENTATION_UNKNOWN == orientation) {
                return;
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            orientation = (orientation + 45) / 90 * 90;
            int rotation;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
            if (null != mCamera) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setRotation(rotation);
                mCamera.setParameters(parameters);
            }
        }
    }

    //start a thread to store picture
    private void storePictureData(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //save picture
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format
                        = new SimpleDateFormat("yyyyMMdd_HHmmSS");
                String path = createFileDirectory(MyConstants.getStorageRootDir(mActivity)
                        , getStoragePath());
                if (path != null) {
                    File mPictureFile = new File(path, "IMG_"
                            + format.format(new Date()) + ".jpg");
                    FileOutputStream outputStream = null;
                    if (!mPictureFile.exists()) {
                        try {
                            if (!mPictureFile.createNewFile()) {
                                Log.e(TAG, "create file failed");
                                return;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        outputStream = new FileOutputStream(mPictureFile);
                        outputStream.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //Send broadcast to tell activity ct_capture is completed
                    Intent intent = new Intent();
                    intent.putExtra(ConstVar.PICTURE_PATH, mPictureFile.getPath());
                    intent.setAction(ConstVar.CAPTURE_COMPLETED);
                    mActivity.sendBroadcast(intent);
                    //Tell system to refresh file
                    Uri uri = Uri.fromFile(mPictureFile);
                    mActivity.sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                } else {
                    Toast.makeText(mActivity, "storage err",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }
}
