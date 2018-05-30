package com.ckt.testauxiliarytool.utils;

import android.os.Build;

/**
 * Created by Cc on 2017/12/1.
 */

public class BuildUtil {

    public static boolean isLollipopMR1OrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean isUseCamera2() {
        return isLollipopMR1OrLater();
    }

}
