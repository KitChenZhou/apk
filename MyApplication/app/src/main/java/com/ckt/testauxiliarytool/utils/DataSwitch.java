package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wgp on 2017/8/23.
 * 控制数据开关的工具类
 * 这里主要用到是反射机制来控制和关闭数据开关
 */

public class DataSwitch {
    private static Class[] getArgArray = null;//获取类信息
    private static Class[] setArgArray = new Class[]{boolean.class};//设置类信息
    private static Object[] getArgInvoke = null;//获得执行的参数信息
    private Context mContext;//上下文信息
    private TelephonyManager mTelephonyManager;//电话管理类
    private Method mGetMethod, mSetMethod;

    /**
     * 参数初始化
     *
     * @param mContext 上下文
     */
    public DataSwitch(Context mContext) {
        this.mContext = mContext;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            mGetMethod = mTelephonyManager.getClass().getMethod("getDataEnabled", getArgArray);
            mSetMethod = mTelephonyManager.getClass().getMethod("setDataEnabled", setArgArray);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断数据开关是否开启
     *
     * @return true为开启
     */
    public boolean isDataOpen() {
        boolean isOpen = false;
        try {
            isOpen = (Boolean) mGetMethod.invoke(mTelephonyManager, getArgInvoke);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return isOpen;
    }

    /**
     * 打开或关闭数据开关
     *
     * @param open true为开启，false为关闭
     */
    public void dataSwitcher(boolean open) {
        try {
            mSetMethod.invoke(mTelephonyManager, open);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
