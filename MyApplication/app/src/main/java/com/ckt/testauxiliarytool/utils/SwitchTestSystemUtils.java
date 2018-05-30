package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by wgp on 2017/8/25.
 * 系统相关设置的工具类
 */

public class SwitchTestSystemUtils {
    /**
     * 用于关闭软键盘
     *
     * @param context
     */
    public static void hideSoftKeyBorad(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
