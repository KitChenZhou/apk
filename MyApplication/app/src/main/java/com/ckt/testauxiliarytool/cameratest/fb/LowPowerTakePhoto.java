package com.ckt.testauxiliarytool.cameratest.fb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.BackConfirmDialog;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;
import com.ckt.testauxiliarytool.utils.OnMultiClickListener;
import com.ckt.testauxiliarytool.cameratest.slrc.model.FunctionView;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;


import static android.view.View.VISIBLE;
import static com.ckt.testauxiliarytool.cameratest.fb.Tool.showToast;

public class LowPowerTakePhoto extends BaseActivity {
    private static final int MSG_TAKE_PHOTO = 0x99;
    private static final int MSG_SWITCH_CAMERA = 0x98;

    private ImageButton mSwitchCameraBtn;
    private ImageButton mStartPauseBtn;
    private AutoFitTextureView mTextureView;
    private Handler mChildHandler;
    private CameraManager mCameraManager;
    private BroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver mBatteryReceiver;
    //if mTakePhotoBtn clicked
    private boolean mIsTaskRunning = false;
    //if activity run onStop method
    private boolean mActivityStopped = false;
    //count TakePhoto
    private int mTakePhotoCount = 0;
    private TextView mShowInfoTextView;
    private TextView mBatteryWarning;
    private boolean mIsPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ct_activity_low_power_take_photo);

        initView();
        buttonClick();
        registerReceiver();
    }

    @Override
    protected void initView() {
        mTextureView = (AutoFitTextureView) findViewById(R.id.texture_view);
        mStartPauseBtn = (ImageButton) findViewById(R.id.start_pause_btn);
        mSwitchCameraBtn = (ImageButton) findViewById(R.id.switch_camera_btn);
        mShowInfoTextView = (TextView) findViewById(R.id.show_info_text_view);
        mBatteryWarning = (TextView) findViewById(R.id.show_battery_warning);

        initHandler();
        mCameraManager = new CameraManager(this, mTextureView);
        showToast(this, getStringRes(R.string.ct_start_low_power_test));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHandlerThread == null) initHandler();
        mTextureView.setVisibility(VISIBLE);
        mCameraManager.checkTexture();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureView.setVisibility(View.GONE);
        try {
            mChildHandler.removeMessages(MSG_TAKE_PHOTO);
            Thread.sleep(500);
            mCameraManager.closeCamera();
            stopThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


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
        mActivityStopped = false;
        initHandler();
        updateTextView(mShowInfoTextView, "");
        if (mIsTaskRunning) {
            mChildHandler.sendEmptyMessageDelayed(MSG_TAKE_PHOTO, 1500);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mActivityStopped = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mBatteryReceiver);
    }

    /**
     * Button clicked
     */
    private void buttonClick() {
        //switch camera
        mSwitchCameraBtn.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                mChildHandler.sendEmptyMessage(MSG_SWITCH_CAMERA);
            }
        });

        //take photo
        mStartPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FunctionView.setImageViewState(LowPowerTakePhoto.this, R.id.fb_activity_3, FunctionView.STATE_FAIL);
                if (!mIsTaskRunning) {
                    mIsPaused = false;
                    mStartPauseBtn.setImageResource(R.drawable.ct_pause);
                    mChildHandler.sendEmptyMessage(MSG_TAKE_PHOTO);
                    mIsTaskRunning = true;
                    mSwitchCameraBtn.setClickable(false);
                    mSwitchCameraBtn.setImageResource(R.drawable.ct_switch_camera_grey);
                } else {
                    mIsPaused = true;
                    mStartPauseBtn.setImageResource(R.drawable.ct_start);
                    mChildHandler.removeMessages(MSG_TAKE_PHOTO);
                    mSwitchCameraBtn.setClickable(true);
                    mSwitchCameraBtn.setImageResource(R.drawable.ct_switch_camera);
                    mIsTaskRunning = false;
                }
            }
        });
    }

    @Override
    protected void initHandler() {
        super.startThread();
        mChildHandler = new Handler(super.mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SWITCH_CAMERA:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCameraManager.switchCamera();
                            }
                        });
                        break;
                    case MSG_TAKE_PHOTO:
                        if (mActivityStopped) return;
                        mTakePhotoCount++;
                        if (mTakePhotoCount > 100) {
                            if (!mActivityStopped) {
                                jumpToMainActivity();
                            }
                        } else {
                            updateTextView(mShowInfoTextView,
                                    getStringRes(R.string.ct_take_photo_count) + mTakePhotoCount);
                            mCameraManager.takePictureWithFlash();
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void registerReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String path = intent.getStringExtra(ConstVar.PICTURE_PATH);
                if (path != null) {
                    updateTextView(mShowInfoTextView,
                            getStringRes(R.string.ct_picture_path) + path);
                    if (mChildHandler != null && !mIsPaused) {
                        mChildHandler.sendEmptyMessageDelayed(MSG_TAKE_PHOTO,
                                1500);
                    }
                }
            }
        };
        mCameraManager.registerReceiver(mBroadcastReceiver, this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(
                        BatteryManager.EXTRA_LEVEL, -1);
                if (level > 15) {
                    mBatteryWarning.setVisibility(VISIBLE);
                } else if (0 <= level && level <= 15) {
                    mBatteryWarning.setVisibility(View.INVISIBLE);
                }
                Log.i("Battery", "battery power level:  " + level + "%");
            }
        };
        registerReceiver(mBatteryReceiver, filter);
    }

    @Override
    protected void jumpToMainActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(LowPowerTakePhoto.this, getStringRes(R.string.ct_take_photo_done));
                SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, LowPowerTakePhoto.this).putInt(String.valueOf(R.id
                        .fb_activity_3), FunctionView.STATE_SUCCESS);
            }
        });
        mCameraManager.closeCamera();
        LowPowerTakePhoto.this.finish();
    }
}
