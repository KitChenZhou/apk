package com.ckt.testauxiliarytool.utils;

import android.view.View;

/**
 * Created by D22431 on 2017/9/25.
 * <p>
 * The click interval between the two click buttons can not be less than 1000 milliseconds
 */

public abstract class OnMultiClickListener implements View.OnClickListener {
    // The click interval between the two click buttons can not be less than 1000 milliseconds
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long sLastClickTime;

    public abstract void onMultiClick(View v);

    @Override
    public void onClick(View v) {
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - sLastClickTime) >= MIN_CLICK_DELAY_TIME) {
            // Than lastClickTime is reset to the current click time after the click interval
            sLastClickTime = curClickTime;
            onMultiClick(v);
        }
    }
}
