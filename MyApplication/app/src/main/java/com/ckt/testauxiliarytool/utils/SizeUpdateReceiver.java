package com.ckt.testauxiliarytool.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ckt.testauxiliarytool.MyApplication;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/10/27
 * <br/>TODO: 更新size广播
 */

public abstract class SizeUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (getAction().equals(intent.getAction())) {
            doUpdate();
        }
    }

    public abstract String getAction();

    public abstract void doUpdate();

    public static void register(SizeUpdateReceiver receiver, String action) {
        if (MyApplication.getContext() != null && receiver != null && action != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(action);
            MyApplication.getContext().registerReceiver(receiver, filter);
        }
    }

    public static void unregister(SizeUpdateReceiver receiver) {
        if (MyApplication.getContext() != null && receiver != null) {
            MyApplication.getContext().unregisterReceiver(receiver);
        }
    }
}
