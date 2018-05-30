package com.ckt.testauxiliarytool.autosleepwake.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ckt.testauxiliarytool.autosleepwake.interfaces.Constants;
import com.ckt.testauxiliarytool.autosleepwake.service.RestartIntentService;
import com.ckt.testauxiliarytool.utils.CacheUtil;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //如果开机完成且当前的测试任务尚未完成，启动重启测试服务
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
                && CacheUtil.getInt(context, Constants.TEST_COUNT, 0) != 0) {
            RestartIntentService.newIntent(context);
        }
    }
}
