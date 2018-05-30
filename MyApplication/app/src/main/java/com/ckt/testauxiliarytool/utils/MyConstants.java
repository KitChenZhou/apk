package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.os.Environment;

/**
 * Created by ckt on 17-12-8.
 */

public final class MyConstants {
    private static final String TAG = "MyConstants";

    /**
     * @Description 私有化构造方法
     */
    private MyConstants() {
    }

    public static final String ROOT_DIR = "testauxiliarytool";
    public static final String SENSOR_DIR = "sensor";
    public static final String WEBTEST_DIR = "webtest";
    public static final String BATTERY_DIR = "battery";
    public static final String TP_DIR = "tp";
    public static final String AUTO_SLEEP_WEAK_DIR = "autosleepwake";
    public static final String AUTOPHONE_DIR = "autophone";
	public static final String CAMERA_DIR = "camera";
    public static final String LOG_DIR = "logs";

    /**
     * @param mContext
     * @return
     */
    public static String getStorageRootDir(Context mContext) {
        String dir = null;
        //先查找是否有外置SD卡，如果有就优先选择外置SD卡
        dir = GetRamRomSdUtil.getStoragePath(mContext, true);

        //没有位置SD卡则选择内置SD卡
        if (dir == null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()
        )) {
            dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        //如果内置SD卡也没有，则选择data分区根目录
        if (dir == null) {
            dir = Environment.getDataDirectory().toString();
        }
        LogUtil.d(TAG, "file directory is : " + dir == null ? "error: no fs!" : dir);
        return dir;
    }
}
