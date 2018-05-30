package com.ckt.testauxiliarytool.cameratest.fb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.CompareSizesByArea;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.cameratest.common.ImageSaver;
import com.ckt.testauxiliarytool.cameratest.common.SoundPlayer;
import com.ckt.testauxiliarytool.utils.MyConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ckt.testauxiliarytool.cameratest.fb.Tool.createFileDirectory;
import static com.ckt.testauxiliarytool.cameratest.fb.Tool.showToast;

/**
 * Created by D22431 on 2017/8/25.
 * <p>
 * Contains methods that help to take photo
 */
class Camera2Helper extends BaseHelper {
    //Max preview mWidth that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_WIDTH = 1920;
    //Max preview mHeight that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    //Shows the orientation of photo
    private CameraDevice mCamera;
    private CameraManager mCameraManager;
    private CameraCaptureSession mCameraCaptureSession;
    private ImageReader mImageReader;
    private int mSensorOrientation = 0;
    //List of camera faces front
    private List<String> mFacingFrontCameraId;
    //List of camera faces back
    private List<String> mFacingBackCameraId;
    private int mRotation = Surface.ROTATION_0;
    private int mOrientation = 0;
    private File mPictureFile;
    private Surface mSurface;
    //if flash need
    private boolean mIsFlashOn = false;
    private boolean mFlashSupported = false;
    //Camera state: Showing camera preview.
    private static final int STATE_PREVIEW = 0;
    //Camera state: Waiting for the focus to be locked.
    private static final int STATE_WAITING_LOCK = 1;
    //Camera state: Waiting for the exposure to be pre capture state.
    private static final int STATE_WAITING_PRE_CAPTURE = 2;
    //Camera state: Waiting for the exposure state
    // to be something other than prepare capture.
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;
    //Camera state: Picture was taken.
    private static final int STATE_PICTURE_TAKEN = 4;
    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(
                SurfaceTexture texture, int width, int height) {
            mWidth = width;
            mHeight = height;
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(
                SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    /**
     * {@link CameraDevice.StateCallback} is called
     * when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback
            = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.
            // We start camera preview here.
            mCameraOpenCloseLock.release();
            mCamera = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCamera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCamera = null;
        }
    };

    /**
     * This a callback object for the {@link ImageReader}.
     * "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format =
                    new SimpleDateFormat("yyyyMMdd_HHmmSS");
            String path = createFileDirectory(MyConstants.getStorageRootDir(mActivity)
                    , getStoragePath());
            if (path != null) {
                mPictureFile = new File(path, "IMG_" +
                        format.format(new Date()) + ".jpg");
                mHandler.post(new ImageSaver(mActivity,
                        reader.acquireNextImage(), mPictureFile));
                //Tell system to refresh file
                Uri uri = Uri.fromFile(mPictureFile);
                mActivity.sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            } else {
                showToast(mActivity.getApplication(), mActivity.getResources().getString(R.string.ct_storage_not_available));
            }
        }
    };

    //{@link CaptureRequest.Builder} for the camera preview
    private CaptureRequest.Builder mPreviewRequestBuilder;

    //{@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
    private CaptureRequest mPreviewRequest;

    //The current state of camera state for taking pictures.
    private int mState = STATE_PREVIEW;

    //to prevent the app from exiting before closing the camera.
    Camera2Helper(Activity mActivity, AutoFitTextureView mTextureView) {
        this.mActivity = mActivity;
        this.mTextureView = mTextureView;
        initData();
        getCameraList(mCameraManager);
        //open camera
        openCamera(mFacingBackCameraId.get(0));
    }

    //write initial values for some global variables
    private void initData() {
        mCameraManager = (CameraManager)
                mActivity.getSystemService(Context.CAMERA_SERVICE);
        mOrientation = mActivity.getResources().getConfiguration().orientation;
        mRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        mDisplaySize = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
    }

    private void getCameraList(CameraManager cameraManager) {
        //Get the front and back camera List
        try {
            mFacingBackCameraId = new ArrayList<>();
            mFacingFrontCameraId = new ArrayList<>();
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics =
                        cameraManager.getCameraCharacteristics
                                (cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //Get the back camera List
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    mFacingBackCameraId.add(cameraId);
                }
                //Get the front camera List
                else if (facing != null && facing ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                    mFacingFrontCameraId.add(cameraId);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * A {@link CameraCaptureSession.CaptureCallback}
     * that handles events related to JPEG ct_capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult result) {
                    switch (mState) {
                        case STATE_PREVIEW: {
                            // We have nothing to do when the
                            // camera preview is working normally.
                            break;
                        }
                        case STATE_WAITING_LOCK: {
                            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == null) {
                                captureStillPicture();
                            } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                                    == afState || CaptureResult.
                                    CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                                // CONTROL_AE_STATE can be null on some devices
                                Integer aeState = result.get(
                                        CaptureResult.CONTROL_AE_STATE);
                                if (aeState == null || aeState == CaptureResult
                                        .CONTROL_AE_STATE_CONVERGED) {
                                    mState = STATE_PICTURE_TAKEN;
                                    captureStillPicture();
                                } else {
                                    runPrepareCaptureSequence();
                                }
                            } else {
                                mState = STATE_PICTURE_TAKEN;
                                captureStillPicture();
                            }
                            break;
                        }
                        case STATE_WAITING_PRE_CAPTURE: {
                            // CONTROL_AE_STATE can be null on some devices
                            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            if (aeState == null || aeState ==
                                    CaptureResult.CONTROL_AE_STATE_PRECAPTURE
                                    || aeState == CaptureRequest.
                                    CONTROL_AE_STATE_FLASH_REQUIRED) {
                                mState = STATE_WAITING_NON_PRE_CAPTURE;
                            }
                            break;
                        }
                        case STATE_WAITING_NON_PRE_CAPTURE: {
                            // CONTROL_AE_STATE can be null on some devices
                            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            if (aeState == null || aeState !=
                                    CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                                mState = STATE_PICTURE_TAKEN;
                                captureStillPicture();
                            }
                            break;
                        }
                    }
                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request,
                                                @NonNull CaptureResult partialResult) {
                    process(partialResult);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    process(result);
                }
            };

    // Show right orientation
    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    // Sets up member variables related to camera.
    private void setUpCameraOutputs(int width, int height) {
        CameraCharacteristics characteristics = null;
        try {
            characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e1) {
            e1.printStackTrace();
        }
        assert characteristics != null;
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics
                .SCALER_STREAM_CONFIGURATION_MAP);
        // For still image captures, we use the largest available size.
        assert map != null;
        Size largest = Collections.max(Arrays.asList(
                map.getOutputSizes(ImageFormat.JPEG)),
                new CompareSizesByArea());
        mImageReader = ImageReader.newInstance(largest.getWidth(),
                largest.getHeight(),
                ImageFormat.JPEG, /*maxImages*/2);
        mImageReader.setOnImageAvailableListener(
                mOnImageAvailableListener, mHandler);
        // Find out if we need to swap dimension to get
        // the preview size relative to sensor
        // coordinate.
        //noinspection ConstantConditions
        mSensorOrientation = characteristics.get(
                CameraCharacteristics.SENSOR_ORIENTATION);
        boolean swappedDimensions = false;
        switch (mRotation) {
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
                Log.e(TAG, "Display rotation is invalid: " + mRotation);
        }

        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = mDisplaySize.x;
        int maxPreviewHeight = mDisplaySize.y;

        if (swappedDimensions) {
            rotatedPreviewWidth = height;
            rotatedPreviewHeight = width;
            maxPreviewWidth = mDisplaySize.y;
            maxPreviewHeight = mDisplaySize.x;
        }
        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
            maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }
        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
            maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }
        // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
        // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
        // garbage ct_capture data.
        mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                rotatedPreviewWidth, rotatedPreviewHeight,
                maxPreviewWidth, maxPreviewHeight, largest);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTextureView.setAspectRatio(mPreviewSize.getWidth(),
                    mPreviewSize.getHeight());
        } else {
            mTextureView.setAspectRatio(mPreviewSize.getHeight(),
                    mPreviewSize.getWidth());
        }
        // Check if the flash is supported.
        Boolean available = characteristics.get(
                CameraCharacteristics.FLASH_INFO_AVAILABLE);
        mFlashSupported = available == null ? false : available;
    }

    @Override
    void openCamera() {
        if (mHandlerThread == null) {
            startThread();
        }
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            setUpCameraOutputs(mWidth, mHeight);
            configureTransform(mWidth, mHeight);
            mCameraManager.openCamera(mCameraId, mStateCallback, mHandler);
            if (!mFlashSupported && mActivity.getLocalClassName()
                    .equals(LOW_POWER_ACTIVITY_NAME)) {
                Toast.makeText(mActivity.getApplication(),
                        mActivity.getString(R.string.ct_flashlight_not_support),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void openCamera(final String cameraId) {
        mCameraId = cameraId;
        checkTexture();
    }

    @Override
    void checkTexture() {
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }
            if (null != mCameraCaptureSession) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (null != mCamera) {
                mCamera.close();
                mCamera = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
            stopThread();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    // Creates a new {@link CameraCaptureSession} for camera preview.
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            // We configure the size of default buffer
            // to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(),
                    mPreviewSize.getHeight());
            // This is the output Surface we need to start preview.
            if (mSurface == null) {
                mSurface = new Surface(texture);
            }
            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCamera.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mSurface);
            // Here, we create a CameraCaptureSession for camera preview.
            mCamera.createCaptureSession(Arrays.asList(mSurface,
                    mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCamera) return;
                            // When the session is ready, we start displaying the preview.
                            mCameraCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                if (null == mCameraCaptureSession) return;
                                mCameraCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mHandler);
                                //camera opened
                                Intent intent = new Intent(ConstVar.CAMERA_OPENED);
                                mActivity.sendBroadcast(intent);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast(mActivity.getApplication(), "Failed");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    void takePicture() {
        mIsFlashOn = false;
        lockFocus();
    }

    @Override
    void takePictureWithFlash() {
        Log.e(TAG, "take picture with flash");
        mIsFlashOn = true;
        lockFocus();
    }

    @Override
    void switchCamera() {
        if (isBackCamera()) {
            if (mFacingFrontCameraId.size() > 0) {
                closeCamera();
                mCameraId = mFacingFrontCameraId.get(0);
                openCamera(mCameraId);
            }
        } else {
            if (mFacingBackCameraId.size() > 0) {
                closeCamera();
                mCameraId = mFacingBackCameraId.get(0);
                openCamera(mCameraId);
            }
        }
    }

    @Override
    boolean isBackCamera() {
        return (mCameraId.equals(mFacingBackCameraId.get(0)));
    }

    //Lock the focus as the first step for a still image ct_capture.
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata
                    .CONTROL_AF_TRIGGER_START);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest
                    .CONTROL_AE_MODE_ON);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            if (null == mCameraCaptureSession) {
                return;
            }
            mCameraCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Unlock the focus. This method should be called
    // when still image ct_capture sequence is finished.
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata
                    .CONTROL_AF_TRIGGER_CANCEL);
            setFlash(mPreviewRequestBuilder);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            if (null == mCameraCaptureSession) {
                return;
            }
            mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //set flash
    private void setFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported && mIsFlashOn) {
            //Auto exposure, to turn on the flash, must be set to ON / OFF
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            //Set flash ON
            requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        } else if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
        }
    }

    /**
     * Run the prepare capture sequence for capturing a still image.
     * This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrepareCaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the prepare capture sequence to be set.
            mState = STATE_WAITING_PRE_CAPTURE;
            if (null == mCameraCaptureSession) return;
            mCameraCaptureSession.capture(mPreviewRequestBuilder.build(),
                    mCaptureCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        try {
            if (null == mCamera) return;
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = mCamera.createCaptureRequest
                    (CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest
                    .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    getOrientation(mRotation));
            setFlash(captureBuilder);
            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    //Shutter Sound
                    SoundPlayer.shoot();
                    unlockFocus();
                    //Send broadcast to tell activity ct_capture is completed
                    Intent intent = new Intent();
                    intent.putExtra(ConstVar.PICTURE_PATH, mPictureFile.getPath());
                    intent.setAction(ConstVar.CAPTURE_COMPLETED);
                    mActivity.sendBroadcast(intent);
                }
            };
            if (null == mCameraCaptureSession) return;
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(captureBuilder.build(),
                    CaptureCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
