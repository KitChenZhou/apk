package com.ckt.testauxiliarytool.utils;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

/**
 * 对话框工具
 *
 */
public final class DialogUtils {

    private DialogUtils() {
        throw new UnsupportedOperationException("you can't instantiate DialogUtils");
    }

    /**
     * 显示对话框
     *
     * @param dialog 对话框
     * @param fragmentManager Fragment管理者
     * @param tag 标签
     */
    public static void showDialog(DialogFragment dialog, FragmentManager fragmentManager, String tag, boolean isCancelable) {
        dialog.setCancelable(isCancelable);
        dialog.show(fragmentManager, tag);
    }
}
