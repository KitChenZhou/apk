package com.ckt.testauxiliarytool.cameratest.capturetask;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by D22433 on 2017/8/31.
 */

public class Params {

    private static Context globalContext;

    /**
     * Initialize globalContext to ApplicationContext.
     * @param context ApplicationContext
     */
    public static void setGlobalContext(Context context){
        globalContext = context;
    }

    /**
     * New method to get value from shared preference.
     * 从SharedPreferences获取数据。
     * @param name 所取值的名字。
     * @param defaultNum 没有取到值的时候的默认值。
     * @return
     */
    public static int get(String name, int defaultNum){
        if (globalContext != null) {
            SharedPreferences sp = globalContext
                    .getSharedPreferences("parameters", globalContext.MODE_PRIVATE);
            int result = sp.getInt(name, defaultNum);
            return result;
        } else {
            return defaultNum;
        }
    }

    /**
     * New method to insert value to shared preference.
     * @param name 所要设置的值的名字
     * @param num 所要设置的值
     */
    public static boolean set(String name, int num){
        if (globalContext != null) {
            SharedPreferences sp = globalContext.getSharedPreferences("parameters", globalContext.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(name, num);
            return editor.commit();
        } else {
            return false;
        }
    }
}
