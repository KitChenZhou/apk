package com.ckt.testauxiliarytool.cameratest.fb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.BackConfirmDialog;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.BOOT_BROADCAST;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.FIRST_TAKE_PHOTO_MESSAGE;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.SECOND_TAKE_PHOTO_MESSAGE;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.START_TIME;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.SWITCH_CAMERA_MESSAGE;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.TAKE_PHOTO_DONE;
import static com.ckt.testauxiliarytool.utils.RebootTool.Root.isDeviceRooted;
import static com.ckt.testauxiliarytool.utils.RebootTool.isSystemApp;
import static com.ckt.testauxiliarytool.utils.RebootTool.rebootForSystemApp;
import static com.ckt.testauxiliarytool.utils.RebootTool.rebootForUserApp;
import static com.ckt.testauxiliarytool.cameratest.fb.Tool.showToast;

/**
 * Activity achieve function of starting camera
 */
public class StartCameraActivity extends BaseActivity {

    private AutoFitTextureView mTextureView;
    private CameraManager mCameraManager;
    //Count reboot
    private int mCount = 0;
    private int mTakePhotoCount = 0;
    private TextView mShowInfoTextView;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ct_activity_start_camera);

        Intent intent = getIntent();
        mCount = intent.getIntExtra(ConstVar.COUNT, -1);
        if (mCount == 1) {
            jumpToMainActivity();
        } else {
            initView();
            registerReceiver();
        }
    }

    @Override
    protected void initView() {
        mTextureView = (AutoFitTextureView) findViewById(R.id.texture_view);
        mShowInfoTextView = (TextView) findViewById(R.id.show_info_text_view);

        FunctionView.setImageViewState(this, R.id.fb_activity_1, FunctionView.STATE_FAIL);

        initHandler();

        if (mCount > 1) {
            showToast(this, getStringRes(R.string.ct_start_camera_test_start));
            super.updateTextView(mShowInfoTextView,
                    getStringRes(R.string.ct_cycle_count_down) + mCount);
        } else {
            showToast(this, getStringRes(R.string.ct_start_camera_test_start));
        }

        mCameraManager = new CameraManager(this, mTextureView);
    }

    @Override
    protected void initHandler() {
        super.startThread();
        mChildHandler = new Handler(super.mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case FIRST_TAKE_PHOTO_MESSAGE:
                        mCameraManager.takePicture();
                        break;

                    case SWITCH_CAMERA_MESSAGE:
                        updateTextView(mShowInfoTextView, "");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCameraManager.switchCamera();
                            }
                        });
                        break;

                    case SECOND_TAKE_PHOTO_MESSAGE:
                        mCameraManager.takePicture();
                        break;

                    case TAKE_PHOTO_DONE:
                        if (mCount > 1 || mCount == -1) {
                            reboot();
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        initHandler();
        mTextureView.setVisibility(View.VISIBLE);
        mCameraManager.checkTexture();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureView.setVisibility(View.GONE);
        try {
            Thread.sleep(500);
            mCameraManager.closeCamera();
            super.stopThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //make back and back key disable when task is running
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new BackConfirmDialog(this).alertConfirmDialog();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initHandler();

        if (mTakePhotoCount == 2) {
            if (mCount == 1) {
                jumpToMainActivity();
            } else {
                reboot();
            }

            super.updateTextView(mShowInfoTextView, "");
        } else {
            showToast(this, getStringRes(R.string.ct_continue_taking_photo));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCount != 1) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    //reboot
    private void reboot() {
        clearPreferenceBeforeReboot();
        Activity activity = StartCameraActivity.this.getParent();
        if (activity != null) activity.finish();

        if (isSystemApp(this)) {
            mCameraManager.closeCamera();
            rebootForSystemApp(this);
            this.finish();
        } else {
            if (isDeviceRooted()) {
                rebootForUserApp();
            } else {
                showToast(this, getString(R.string.ct_not_root_reboot_by_hand));
            }
        }
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
                            StartCameraActivity.super.updateTextView(
                                    mShowInfoTextView,
                                    getStringRes(R.string.ct_picture_path) + path);
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
        SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, this)
                .putInt(String.valueOf(R.id
                        .fb_activity_1), FunctionView.STATE_SUCCESS);
        //Start CameraActivity
        Intent intent = new Intent(StartCameraActivity.this,
                CameraActivity.class);
        intent.putExtra(ConstVar.TEST_DONE, ConstVar.START_CAMERA_ACTIVITY);
        startActivity(intent);
        StartCameraActivity.this.finish();
    }

    private void clearPreferenceBeforeReboot() {
        SharedPreferences bootSharedPreference =
                this.getSharedPreferences(BOOT_BROADCAST, Context.MODE_PRIVATE);
        SharedPreferences.Editor bootEditor = bootSharedPreference.edit();
        long start_time = bootSharedPreference.getLong(START_TIME, -2);
        if (start_time != -1) {
            bootEditor.putLong(START_TIME, -1);
            bootEditor.apply();
        }
    }
}
