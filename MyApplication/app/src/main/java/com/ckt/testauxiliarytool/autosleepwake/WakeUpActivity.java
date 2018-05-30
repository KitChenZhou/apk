package com.ckt.testauxiliarytool.autosleepwake;

import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * 该Activity仅用于唤醒屏幕，在Android 6.0下不需要PowerManager.WakeLock唤醒锁
 * 也可唤醒屏幕，但是Android 7.0加强了电量优化，必须配合PowerManager.WakeLock
 * 唤醒锁才能唤醒屏幕，因此还不如直接利用PowerManager来唤醒屏幕，故将该类废弃
 */
@Deprecated
public class WakeUpActivity extends AppCompatActivity {
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "CKT");
        mWakeLock.acquire();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWakeLock.release();
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
