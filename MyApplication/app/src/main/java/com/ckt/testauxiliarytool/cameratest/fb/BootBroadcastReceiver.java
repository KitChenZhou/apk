package com.ckt.testauxiliarytool.cameratest.fb;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.ckt.testauxiliarytool.cameratest.common.ConstVar;

import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.BOOT_BROADCAST;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.COUNT;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.START_COUNT;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.START_NUM;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.START_TIME;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.UNUSUALLY;
import static com.ckt.testauxiliarytool.cameratest.common.ConstVar.UNUSUALLY_START;

/**
 * Created by D22431 on 2017/8/21.
 * <p>
 * Boot start broadcast
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";
    public static final String BOOT_START = "com.ckt.camera.boot_start";
    public static final String BOOT_START_VALUE = "com.ckt.camera.boot_start_value";

    public void onReceive(Context context, Intent intent) {
        //store time between boot and  ACTION_MEDIA_MOUNTED/ACTION_BOOT_COMPLETED
        SharedPreferences bootSharedPreference =
                context.getSharedPreferences(BOOT_BROADCAST, Context.MODE_PRIVATE);
        SharedPreferences.Editor bootEditor = bootSharedPreference.edit();
        long start_time = bootSharedPreference.getLong(START_TIME, -2);

        // 得到键盘锁管理器对象
        KeyguardManager keyguardManager = (KeyguardManager)
                context.getSystemService(Context.KEYGUARD_SERVICE);

        switch (intent.getAction()) {
            case Intent.ACTION_MEDIA_MOUNTED:
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_USER_PRESENT:
                Log.i(TAG, "action: " + intent.getAction());
                if (!keyguardManager.isKeyguardLocked()
                        && start_time == -1) {
                    Log.i(TAG, "start test by: " +
                            intent.getAction());
                    doWhat(context);
                    bootEditor.putLong(START_TIME, SystemClock.uptimeMillis());
                    bootEditor.apply();
                }
                break;

            case Intent.ACTION_SHUTDOWN:
            case Intent.ACTION_REBOOT:
                Log.i(TAG, "shutdown or reboot");
                if (start_time != -1) {
                    bootEditor.putLong(START_TIME, -1);
                    bootEditor.apply();
                }
                break;
        }
    }

    /**
     * do something when broadcast is received
     */
    private void doWhat(Context context) {
        //Determine which Activity to start with the SharedPreferences value
        //StartCameraActivity
        SharedPreferences bootStartPreferences = context.getApplicationContext()
                .getSharedPreferences(ConstVar.BOOT_START, Context.MODE_PRIVATE);
        int startCount = bootStartPreferences.getInt(START_COUNT, -1);
        //UnusuallyExitCamera
        int startNum = bootStartPreferences.getInt(START_NUM, -1);

        if (startCount > 0) {
            //Update SharedPreferences
            SharedPreferences.Editor editor = bootStartPreferences.edit();
            editor.putInt(START_COUNT, startCount - 1);
            editor.apply();

            Intent startIntent = new Intent(context.getApplicationContext(),
                    StartCameraActivity.class);
            startIntent.putExtra(BOOT_START, BOOT_START_VALUE);
            startIntent.setAction(Intent.ACTION_MAIN);
            startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.putExtra(COUNT, startCount);
            context.startActivity(startIntent);
        } else if (startNum > 0) {
            SharedPreferences.Editor editor = bootStartPreferences.edit();
            editor.putInt(START_NUM, startNum - 1);
            editor.apply();

            Intent startIntent1 = new Intent(context.getApplicationContext(),
                    UnusuallyExitCamera.class);
            startIntent1.setAction(Intent.ACTION_MAIN);
            startIntent1.putExtra(BOOT_START, BOOT_START_VALUE);
            startIntent1.addCategory(Intent.CATEGORY_LAUNCHER);
            startIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent1.putExtra(UNUSUALLY, UNUSUALLY_START);
            startIntent1.putExtra(COUNT, startNum);
            context.startActivity(startIntent1);
        }
    }
}
