package com.ckt.testauxiliarytool.utils;


import android.content.Context;
import android.content.SharedPreferences;

public final class CacheUtil {
    //SharedPreferences文件的名字
    private static final String SHARED_PREFERENCES_NAME = "ckt_auto_test";

    private static SharedPreferences sSharedPreferences;

    private CacheUtil() {
        throw new UnsupportedOperationException("you can't instantiate CacheUtil");
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if (sSharedPreferences == null) {
            sSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        return sSharedPreferences;
    }

    /**
     * 向SharedPreferences文件写入整型值
     *
     * @param context 上下文
     * @param key     对应的键
     * @param value   对应的值
     */
    public static void putInt(Context context, String key, int value) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putInt(key, value).apply();
    }

    /**
     * 从SharedPreferences文件值获取整型值
     *
     * @param context  上下文
     * @param key      对应的键
     * @param defValue 默认值
     * @return 当从SharedPreferences文件值成功取得值，则返回取出的值，否则返回默认值
     */
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getInt(key, defValue);
    }
}
