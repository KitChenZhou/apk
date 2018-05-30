package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ckt.testauxiliarytool.MyApplication;

import java.util.HashMap;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/24
 * TODO: SharedPreferences工具类
 */

public class SharedPrefsUtil {
    private static SharedPreferences sSp;
    private static SharedPrefsUtil sSpUtil;
    private static HashMap<String, SharedPreferences> sSpMap;
    private static String sCurName;

    private SharedPrefsUtil() {

    }

    /**
     * 设置要操作的对象
     *
     * @param name sp操作对象，xml名
     * @return <br/> this method must be called after {@link MyApplication#onCreate()}
     */
    public static SharedPrefsUtil name(String name) {
        if (name == null) {
            throw new RuntimeException("name should not be null");
        }
        sCurName = name;
        if (sSpUtil == null) {
            sSpUtil = new SharedPrefsUtil();
        }
        if (sSpMap == null) {
            sSpMap = new HashMap<>();
        }
        // 不包含
        if (!sSpMap.containsKey(name) || sSp == null) {
            sSp = MyApplication.getContext().getSharedPreferences(name, Context.MODE_APPEND);
            sSpMap.put(name, sSp);  // 存储起来
        } else {
            sSp = sSpMap.get(name);
        }
        return sSpUtil;
    }

    /**
     * 参阅{@link #name(String)}
     */
    public static SharedPrefsUtil name(String name, Context context) {
        if (name == null) {
            throw new RuntimeException("name should not be null");
        }
        if (sSpMap == null) {
            sSpMap = new HashMap<>();
        }
        if (sSpUtil == null) {
            sSpUtil = new SharedPrefsUtil();
        }

        if (!sSpMap.containsKey(name) || sSp == null) {
            sSp = context.getSharedPreferences(name, Context.MODE_APPEND);
            sSpMap.put(name, sSp);  // 存储起来
        } else {
            sSp = sSpMap.get(name);
        }
        return sSpUtil;
    }


    /**
     * 存字符串
     *
     * @param key
     * @param value
     * @return SharedPrefsUtil
     * <br/>this method call {@link SharedPreferences.Editor#putString} to store value
     */
    public SharedPrefsUtil putString(String key, String value) {
        safetyCheck(key);
        sSp.edit().putString(key, value).apply();
        return sSpUtil;
    }

    /**
     * 取字符串
     *
     * @param key
     * @param defaultValue
     * @return <br/>this method call {@link SharedPreferences.Editor#getString(String, String)} to store value
     */
    public String getString(String key, String defaultValue) {
        safetyCheck(key);
        return sSp.getString(key, defaultValue);
    }

    /**
     * 存整型数据
     *
     * @param key
     * @param value
     * @return SharedPrefsUtil
     * <br/>this method call {@link SharedPreferences.Editor#putInt(String, int)} to store value
     */
    public SharedPrefsUtil putInt(String key, int value) {
        safetyCheck(key);
        sSp.edit().putInt(key, value).apply();
        return sSpUtil;
    }

    /**
     * 取整型数据
     *
     * @param key
     * @param defaultValue
     * @return <br/>this method call {@link SharedPreferences.Editor#getInt(String, int)}  to store value
     */
    public int getInt(String key, int defaultValue) {
        safetyCheck(key);
        return sSp.getInt(key, defaultValue);
    }

    /**
     * 存布尔数据
     *
     * @param key
     * @param value
     * @return SharedPrefsUtil
     * <br/>this method call {@link SharedPreferences.Editor#putInt(String, int)} to store value
     */
    public SharedPrefsUtil putBoolean(String key, boolean value) {
        safetyCheck(key);
        sSp.edit().putBoolean(key, value).apply();
        return sSpUtil;
    }

    /**
     * 取布尔数据
     *
     * @param key
     * @param defaultValue
     * @return <br/>this method call {@link SharedPreferences.Editor#getInt(String, int)}  to store value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        safetyCheck(key);
        return sSp.getBoolean(key, defaultValue);
    }
	
	/**
     * 存整型数组
     *
     * @param keys
     * @param values
     * @return SharedPrefsUtil
     * <br/>this method call {@link SharedPreferences.Editor#putInt(String, int)} to store value
     */
    public SharedPrefsUtil putIntArray(int[] keys, int[] values) {
        if (keys.length != values.length) return null;
        for (int i = 0; i < keys.length; i++) {
            putInt(String.valueOf(keys[i]), values[i]);
        }
        return sSpUtil;
    }

    /**
     * 取整型数组
     *
     * @param keys
     * @param defaultValue
     * @return <br/>this method call {@link SharedPreferences.Editor#getInt(String, int)}  to
     * store value
     */
    public int[] getIntArray(int[] keys, int defaultValue) {
        if (keys == null || keys.length == 0) return null;
        int[] valueArray = new int[keys.length];
        for (int i = 0; i < valueArray.length; i++) {
            valueArray[i] = getInt(String.valueOf(keys[i]),defaultValue);
        }
        return valueArray;
    }

    /**
     * this method used to recycle specified sSp
     */
    public static void recycle(String name) {
        if (sSpMap != null)
            sSpMap.remove(name);
    }

    /**
     * this method used to recycle cur sSp
     */
    public void recycle() {
        if (sSpMap != null)
            if (sCurName != null) {
                sSpMap.remove(sCurName);
                sCurName = null;
            }
    }

    /**
     * 移除指定key的存储
     *
     * @param key
     * @return <br/>this method call {@link SharedPreferences.Editor#remove(String)} to store value
     */
    public SharedPrefsUtil remove(String key) {
        safetyCheck(key);
        sSp.edit().remove(key).apply();
        return sSpUtil;
    }

    /**
     * 安全检查
     *
     * @param key
     */
    private static void safetyCheck(String key) {
        if (sSp == null) {
            throw new NullPointerException("SharedPrefsUtil:sSp should not be null");
        }
        if (key == null) {
            throw new NullPointerException("SharedPrefsUtil:key should not be null");
        }
    }

    /*
    * 释放所有资源，只有当不再使用工具类时调用
    * */
    public static void release() {
        sSp = null;
        sSpUtil = null;
        sSpMap = null;
        sCurName = null;
    }
}
