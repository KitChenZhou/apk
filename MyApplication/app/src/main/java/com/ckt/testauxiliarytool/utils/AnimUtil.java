package com.ckt.testauxiliarytool.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/25
 * TODO:动画帮助类，控制view从下端飞出与飞入
 */

public class AnimUtil {
    private static AnimUtil sAnimUtil;
    /**
     * 是否正在进行飞出动画
     */
    private boolean isAnimatingOut = false;
    /**
     * 是否正在进行飞入动画
     */
    private boolean isAnimatingIn = false;

    public static AnimUtil getInstance() {
        if (sAnimUtil == null) {
            sAnimUtil = new AnimUtil();
        }
        return sAnimUtil;
    }

    //  飞出动画.
    public void animateOut(final View view) {
        if (isAnimatingOut) return;
        isAnimatingOut = true;
        int translationY = view.getHeight() + getMarginBottom(view);
        performAnimate(view, translationY, new OvershootInterpolator(), 500, false);
    }

    //    public void animateOut(RecordButtonView[] views) {
//        if (isAnimatingOut) return;
//        isAnimatingOut = true;
//        for (RecordButtonView view : views) {
//            int translationY = view.getHeight() + getMarginBottom(view);
//            performAnimate(view,translationY,new FastOutSlowInInterpolator(),500);
//        }
//    }

    public void animateOut(View... views) {
        if (isAnimatingOut) return;
        isAnimatingOut = true;
        for (View view : views) {
            int translationY = view.getHeight() + getMarginBottom(view);
            performAnimate(view, translationY, new FastOutSlowInInterpolator(), 500, false);
        }
    }


    //  进入动画.
    public void animateIn(View view) {
        if (isAnimatingIn) return;
        isAnimatingIn = true;
        performAnimate(view, 0, new FastOutSlowInInterpolator(), 500, true);
    }

    public void animateIn(View... views) {
        if (isAnimatingIn) return;
        isAnimatingIn = true;
        for (View view : views) {
            performAnimate(view, 0, new FastOutSlowInInterpolator(), 500, true);
        }
    }


    /**
     * 执行动画
     *
     * @param view         动画作用的对象
     * @param translationY Y方向偏移量
     * @param interpolator interpolator
     * @param duration     duration
     */
    private void performAnimate(View view, float translationY, TimeInterpolator interpolator, int duration, final boolean in) {
        view.animate()
                .translationY(translationY)
                .setInterpolator(interpolator)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (in) {
                            isAnimatingIn = false;
                        } else {
                            isAnimatingOut = false;
                        }
                    }
                })
                .start();
    }

    public static int getMarginBottom(View v) {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }
}
