package com.ckt.testauxiliarytool.cameratest.slrc;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;

/**
 * Created by Cc on 2017/9/22.
 */

public abstract class CameraBaseActivity extends AppCompatActivity {

    private static final String ACTION_WAKEUP = "com.ckt.testauxiliarytool.cameratest.Wakeup";

    /**
     * Sleep screen time.
     */
    private static final int SLEEP_SCREEN_TIME = 2500;

    private static final String[] REQUEST_PERMISSIONS = {Manifest.permission
            .WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    private static final String DIALOG_TAG = "dialog";

    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final int REQUEST_ADMIN_MANAGEMENT = 0;

    private ComponentName mAdminName;

    /**
     * A manager to change the screen be locked.
     */
    private DevicePolicyManager mDevicePolicyManager;

    /**
     * A manager to manage alarm, in here we use it to wakeup screen.
     */
    private AlarmManager mAlarmManager;

    /**
     * Use to wakeup screen.
     */
    private PowerManager.WakeLock mWakeLock;

    /**
     * A intent, we use it to get the broadcast.
     */
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //去除滑动解锁
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, WindowManager
                .LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(getLayoutResId());

        grantedPermissions();

        setLockAndWakeupScreen();

        initData();
        initView();
        setListener();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //If the device manager is not active, a Intent that activates the device manager is
        // started here.Specific performance is the first time you open the program, the phone
        // will pop up activated device manager prompts, activate it.
        if (mDevicePolicyManager != null && !mDevicePolicyManager.isAdminActive(mAdminName)) {
            showAdminManagement();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), getString(R.string
                            .ct_grant_permission), Toast.LENGTH_LONG).show();
                    finish();
                    break;
                }
            }

            doAfterGrantPermission();

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADMIN_MANAGEMENT && resultCode != Activity.RESULT_OK) {
            Toast.makeText(getApplicationContext(), getString(R.string.ct_grant_device_manager),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    abstract int getLayoutResId();

    abstract void initData();

    abstract void initView();

    abstract void setListener();

    abstract void doAfterGrantPermission();

    /**
     * Whether the activity can be closed. It was used when click back button.
     *
     * @return If can be closed return true.
     */
    public abstract boolean whetherCanBeClosed();

    private void grantedPermissions() {
        if (checkPermissions()) {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    boolean checkPermissions() {
        for (String permission : REQUEST_PERMISSIONS)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager
                    .PERMISSION_GRANTED) {
                return true;
            }
        return false;
    }

    private void setLockAndWakeupScreen() {
        //we use this way to lock screen.
        mAdminName = new ComponentName(this, AdminManageReceiver.class);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context
                .DEVICE_POLICY_SERVICE);

        //we use it to wakeup screen.
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, WakeupReceiver.class);
        intent.setAction(ACTION_WAKEUP);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent
                .FLAG_UPDATE_CURRENT);

        //we use it to wakeup screen in another way.
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        // Get PowerManager.WakeLock Object
        if (powerManager != null)
            mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_DIM_WAKE_LOCK, IConstValue.TAG);
    }

    /**
     * Activate Device Manager {@link #mDevicePolicyManager}
     */
    private void showAdminManagement() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string
                .ct_activity_device));
        startActivityForResult(intent, REQUEST_ADMIN_MANAGEMENT);
    }

    /**
     * Lock screen {@link #mDevicePolicyManager}
     */
    private void lockScreen() {
        if (mDevicePolicyManager.isAdminActive(mAdminName)) {
            mDevicePolicyManager.lockNow();
        } else {
            showAdminManagement();
        }
    }

    /**
     * Lock then wakeup screen in {@link #SLEEP_SCREEN_TIME}
     */
    public void lockAndWakeupScreen() {
        if (mDevicePolicyManager == null || mAlarmManager == null) return;

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
                SLEEP_SCREEN_TIME, mPendingIntent);

        lockScreen();
    }

    public void lockScreenByThread() {
        if (mDevicePolicyManager != null) lockScreen();
    }

    public void wakeupScreenByThread() {
        if (mWakeLock != null) {
            //wakeup screen
            mWakeLock.acquire(1000);

            //release the lock
            mWakeLock.release();
        }
    }

}
