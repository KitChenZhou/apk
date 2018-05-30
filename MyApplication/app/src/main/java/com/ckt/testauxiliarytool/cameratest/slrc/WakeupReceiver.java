package com.ckt.testauxiliarytool.cameratest.slrc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;

/**
 * Created by Cc on 2017/8/22.
 */

public class WakeupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        wakeUpAndUnlock(context);
    }

    private void wakeUpAndUnlock(Context context) {
        /*//屏锁管理器
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();*/

        //获取电源管理器对象
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager
                .ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, IConstValue.TAG);

        //点亮屏幕
        wakeLock.acquire();

        //释放
        wakeLock.release();
    }


}
