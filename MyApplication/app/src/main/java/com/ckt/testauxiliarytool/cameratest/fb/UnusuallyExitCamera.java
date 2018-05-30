package com.ckt.testauxiliarytool.cameratest.fb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.cameratest.slrc.CameraActivity;
import com.ckt.testauxiliarytool.cameratest.slrc.model.FunctionView;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.BackConfirmDialog;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.FIRST_TAKE_PHOTO_MESSAGE;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.SECOND_TAKE_PHOTO_MESSAGE;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.SWITCH_CAMERA_MESSAGE;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.TAKE_PHOTO_DONE;
import static com.ckt.testauxiliarytool.cameratest.fb.Tool.showToast;

/**
 * Activity that achieve function of abnormal exit camera
 */
public class UnusuallyExitCamera extends BaseActivity {

    private AutoFitTextureView mTextureView;
    private Handler mChildHandler;
    private BroadcastReceiver mBroadcastReceiver;
    //Whether to start the boot flag
    private String mStartLog;
    private CameraManager mCameraManager;
    private int mTakePhotoCount = 0;
    //Reboot count
    private int mCount = 0;
    //if task is running
    private boolean mIsTaskRunning = false;
    private TextView mShowInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ct_activity_start_camera);

        //Receive boot start broadcast broadcast Intent data
        // to determine whether to perform camera operation!
        Intent intent = getIntent();
        mStartLog = intent.getStringExtra(ConstVar.UNUSUALLY);
        mCount = intent.getIntExtra(ConstVar.COUNT, 0);
        initView();

        registerReceiver();
    }

    @Override
    protected void initView() {
        mTextureView = (AutoFitTextureView) findViewById(R.id.texture_view);
        mShowInfoTextView = (TextView) findViewById(R.id.show_info_text_view);

        FunctionView.setImageViewState(this, R.id.fb_activity_2, FunctionView.STATE_FAIL);

        if (mStartLog != null) {
            mIsTaskRunning = true;
            initHandler();
            mCameraManager = new CameraManager(this, mTextureView);
            showToast(this,
                    getStringRes(R.string.ct_abnormal_exit_camera_start));
            updateTextView(mShowInfoTextView,
                    getStringRes(R.string.ct_cycle_count_down) + mCount);
        } else {
            super.startThread();
            mChildHandler = new Handler(mHandlerThread.getLooper());
            mCameraManager = new CameraManager(this, mTextureView);
            showToast(this, getStringRes(
                    R.string.ct_abnormal_exit_camera_start_pull_the_battery));
        }
    }

    @Override
    protected void initHandler() {
        super.startThread();
        mChildHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case FIRST_TAKE_PHOTO_MESSAGE:
                        mCameraManager.takePicture();
                        break;

                    case SWITCH_CAMERA_MESSAGE:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShowInfoTextView.setText("");
                                mCameraManager.switchCamera();
                            }
                        });
                        break;

                    case SECOND_TAKE_PHOTO_MESSAGE:
                        mCameraManager.takePicture();
                        break;

                    case TAKE_PHOTO_DONE:
                        if (mCount > 1) {
                            mIsTaskRunning = false;
                            updateTextView(mShowInfoTextView, "");
                            showToast(UnusuallyExitCamera.this,
                                    getStringRes(R.string.
                                            ct_abnormal_exit_camera_one_test_done));
                        } else if (mCount == 1) {
                            jumpToMainActivity();
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        mTextureView.setVisibility(View.VISIBLE);
        if (mStartLog == null) {
            startThread();
        } else initHandler();
        mCameraManager.checkTexture();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureView.setVisibility(View.GONE);
        try {
            Thread.sleep(500);
            stopThread();
            mChildHandler = null;
            mCameraManager.closeCamera();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //make back and home key disable when task is running
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK ||
                keyCode == KeyEvent.KEYCODE_HOME)
                && mIsTaskRunning) {
            showToast(this, getStringRes(R.string.ct_test_is_running));
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new BackConfirmDialog(this).alertConfirmDialog();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        mIsTaskRunning = false;
        initHandler();

        if (mStartLog != null) {
            if (mTakePhotoCount == 2) {
                if (mCount == 1) {
                    jumpToMainActivity();
                } else {
                    showToast(this, getStringRes(
                            R.string.ct_abnormal_exit_camera_stopping_taking_photo));
                }
                super.updateTextView(mShowInfoTextView, "");
            } else {
                showToast(this, getStringRes(
                        R.string.ct_continue_taking_photo));
            }
        } else {
            showToast(this, getStringRes(
                    R.string.ct_abnormal_exit_camera_stopping_taking_photo));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void registerReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case ConstVar.CAPTURE_COMPLETED:
                        String path = intent.getStringExtra(ConstVar.PICTURE_PATH);
                        if (path != null) {
                            UnusuallyExitCamera.super.updateTextView(
                                    mShowInfoTextView, getStringRes(
                                            R.string.ct_picture_path) + path);
                            mTakePhotoCount += 1;
                            if (mChildHandler != null) {
                                switch (mTakePhotoCount) {
                                    case 1:
                                        mChildHandler.sendEmptyMessageDelayed(
                                                SWITCH_CAMERA_MESSAGE, 1500);
                                        break;
                                    case 2:
                                        mChildHandler.sendEmptyMessageDelayed(
                                                TAKE_PHOTO_DONE, 1500);
                                        break;
                                }
                            }
                        }
                        break;
                    case ConstVar.CAMERA_OPENED:
                        if (mChildHandler != null) {
                            switch (mTakePhotoCount) {
                                case 0:
                                    mChildHandler.sendEmptyMessageDelayed(
                                            FIRST_TAKE_PHOTO_MESSAGE, 1500);
                                    break;
                                case 1:
                                    if (!mCameraManager.isBackCamera()) {
                                        mChildHandler.sendEmptyMessageDelayed(
                                                SECOND_TAKE_PHOTO_MESSAGE, 1500);
                                    } else {
                                        mChildHandler.sendEmptyMessageDelayed(
                                                SWITCH_CAMERA_MESSAGE, 1500);
                                    }
                                    break;
                            }
                        }
                        break;
                }
            }
        };
        mCameraManager.registerReceiver(mBroadcastReceiver, this);
    }

    @Override
    protected void jumpToMainActivity() {
        mCameraManager.closeCamera();
        SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, this).putInt(String.valueOf(R.id
                .fb_activity_2), FunctionView.STATE_SUCCESS);
        //start CameraActivity
        Intent intent = new Intent(UnusuallyExitCamera.this,
                CameraActivity.class);
        intent.putExtra(ConstVar.TEST_DONE, ConstVar.UNUSUALLY_EXIT_CAMERA);
        startActivity(intent);
        UnusuallyExitCamera.this.finish();
    }
}
