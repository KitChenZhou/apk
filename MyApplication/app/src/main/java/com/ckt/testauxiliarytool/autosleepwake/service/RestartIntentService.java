package com.ckt.testauxiliarytool.autosleepwake.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.ckt.testauxiliarytool.autosleepwake.interfaces.Constants;
import com.ckt.testauxiliarytool.utils.CacheUtil;
import com.ckt.testauxiliarytool.utils.TestUtil;

public class RestartIntentService extends IntentService {

    public RestartIntentService() {
        super("RestartIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //从SharedPreferences中取出测试次数
        int testCount = CacheUtil.getInt(getApplicationContext(), Constants.TEST_COUNT, 0);
        if (testCount != 0) {
            //取出测试时间间隔
            int testTime = CacheUtil.getInt(getApplicationContext(), Constants.TEST_TIME, 0);
            SystemClock.sleep(testTime * 1000);
            //测试次数减一
            CacheUtil.putInt(getApplicationContext(), Constants.TEST_COUNT, --testCount);
            Toast.makeText(this, "即将重启!", Toast.LENGTH_SHORT).show();
            // 此处应重启，
            TestUtil.rebootDeviceWithBroadcast(this);
        }
    }

    public static void newIntent(Context context) {
        Intent intent = new Intent(context, RestartIntentService.class);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        CacheUtil.putInt(context, Constants.TEST_COUNT, 0);
        Intent intent = new Intent(context, RestartIntentService.class);
        context.stopService(intent);
    }

}
