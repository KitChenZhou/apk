package com.ckt.testauxiliarytool.tp.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.adapters.OnGestureListenerAdapter;
import com.ckt.testauxiliarytool.utils.SizeUtil;
import com.ckt.testauxiliarytool.utils.UiUtil;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/19.
 * TODO:自定义Button，实现点击状态转换，动态变化动画
 */

public class RecordButton extends View {
    private Paint mPaint;
    private GestureDetector mGestureDetector;
    /* 状态信息*/
    public static final int TYPE_START = 0;
    public static final int TYPE_STOP = 1;
    private int mCurType = TYPE_STOP;
    /* 时间流逝比*/
    private float mFraction = 0.0f;
    /*  半径 */
    private float mRadius;
    /* 文字 */
    private String mMsgText = "";
    private String mBtnText = "";
    private int mTextMsgSize;
    private int mTextBtnSize;

    private boolean enabled = true;

    // 开启和结束时的颜色动画
    private ValueAnimator mStartColorAnimator;
    private ValueAnimator mStopColorAnimator;
    // 呼吸效果颜色动画
    private ValueAnimator mCycleAnimator;

    // 动画更新监听
    private ValueAnimator.AnimatorUpdateListener mStartUpdateListener;
    private ValueAnimator.AnimatorUpdateListener mStopUpdateListener;
    private ValueAnimator.AnimatorUpdateListener mCycleUpdateListener;

    // 动画监听
    private AnimatorListenerAdapter mStartListener;
    private AnimatorListenerAdapter mStopListener;
    /* start - end */
    private int color_start = R.color.green_dark;
    private int color_end = R.color.red;
    /* fraction color*/
    private int color = getResources().getColor(R.color.green_dark);

    /* 是否取消呼吸效果*/
    private boolean isCancelAnimatedCycle = false;

    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextMsgSize = SizeUtil.dp2px(20);
        mTextBtnSize = SizeUtil.dp2px(35);
        mBtnText = UiUtil.getString(R.string.screen_record);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        CustomButtonViewGestureListener gestureListener = new CustomButtonViewGestureListener();
        mGestureDetector = new GestureDetector(context, gestureListener);

        initAnimations();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);

        mTextBtnSize = Math.min(SizeUtil.dp2px(width / 10), mTextBtnSize);
        mTextMsgSize = Math.min(SizeUtil.dp2px(height / 20), mTextMsgSize);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRadius = getRadius();
    }

    private int measureWidth(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = 200;
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = 200;
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制圆形
        mPaint.setColor(color);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius, mPaint);

        // 绘制文本
        mPaint.setColor(Color.RED);
        drawCenterText(mMsgText, canvas, mPaint, mTextMsgSize, getHeight() / 12);
        mPaint.setColor(Color.WHITE);
        drawCenterText(mBtnText, canvas, mPaint, mTextBtnSize, 0);
    }

    /**
     * 在中间绘制文字
     *
     * @param msgText  文字
     * @param canvas   画布
     * @param paint    画笔
     * @param textSize 文字大小
     * @param offsetY  Y反向偏移
     */
    private void drawCenterText(String msgText, Canvas canvas, Paint paint, int textSize, int offsetY) {
        int color = paint.getColor();
        if (color == 0) {
            paint.setColor(Color.WHITE);
        }
        paint.setTextSize(textSize);
        //获取paint中的字体信息 ， setTextSize方法要在他前面
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        // 计算文字高度
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        // 计算文字高度baseline
        float textBaseY = getHeight() / 2 + (fontHeight / 2 - fontMetrics.bottom);
        //获取字体的长度
        float fontWidth = paint.measureText(msgText);
        //计算文字长度的baseline
        float textBaseX = (getWidth() - fontWidth) / 2;
        canvas.drawText(msgText, textBaseX, textBaseY + offsetY, paint);
    }


    /**
     * 获取圆得半径
     *
     * @return mRadius
     */
    private int getRadius() {
        return Math.min(getWidth() / 2, getHeight() / 2);
    }


    private void initAnimations() {
        initStartColorAnimation();
        initStopColorAnimation();
        initBreathCycleAnimation();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initStartColorAnimation() {
        mStartColorAnimator = ValueAnimator.ofArgb(getResources().getColor(color_start), getResources().getColor(color_end));
        mStartColorAnimator.setEvaluator(new ArgbEvaluator());
        mStartColorAnimator.setInterpolator(new LinearInterpolator());
        mStartColorAnimator.setDuration(600);
        mStartUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                color = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        };

        mStartListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
//                startButtonViewAnimatedBreathCycle();
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initStopColorAnimation() {
        mStopColorAnimator = ValueAnimator.ofArgb(getResources().getColor(color_end), getResources().getColor(color_start));
        mStopColorAnimator.setEvaluator(new ArgbEvaluator());
        mStopColorAnimator.setInterpolator(new LinearInterpolator());
        mStopColorAnimator.setDuration(600);
        mStopUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                color = (int) animation.getAnimatedValue();
                invalidate();
            }
        };

        mStopListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                cancelButtonViewBreathAnimatedCycle();
            }
        };

    }

    private void initBreathCycleAnimation() {
        mCycleAnimator = ValueAnimator.ofFloat(1, 0);
        mCycleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mCycleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mCycleAnimator.setDuration(1000);
        mCycleUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFraction = 1.0f - animation.getAnimatedFraction() * 0.2f;
                mRadius = getRadius() * mFraction;
                if (isCancelAnimatedCycle && mFraction >= 0.999999f) {
                    mCycleAnimator.cancel();
                    isCancelAnimatedCycle = false;
                }

                postInvalidate();
            }
        };

    }

    /**
     * 点击开始时的颜色变化动画效果
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startColorAnimation() {
        if (mStartColorAnimator != null && !mStartColorAnimator.isRunning())
            mStartColorAnimator.start();
    }


    /**
     * 点击结束时的颜色变化动画效果
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopColorAnimation() {
        if (mStopColorAnimator != null && !mStopColorAnimator.isRunning())
            mStopColorAnimator.start();
    }

    /**
     * 开始按钮呼吸动画效果
     */
    private void startButtonViewAnimatedBreathCycle() {
        if (mCycleAnimator != null && !mCycleAnimator.isRunning() && mCurType == TYPE_START)
            mCycleAnimator.start();
    }


    /**
     * 取消按钮呼吸效果
     */
    private void cancelButtonViewBreathAnimatedCycle() {
        if (mCycleAnimator != null && mCycleAnimator.isRunning()) {
            isCancelAnimatedCycle = true;
        }
    }

    /**
     * 添加监听
     */
    private void addListeners() {
        mStartColorAnimator.addUpdateListener(mStartUpdateListener);
        mStartColorAnimator.addListener(mStartListener);

        mStopColorAnimator.addUpdateListener(mStopUpdateListener);
        mStopColorAnimator.addListener(mStopListener);

        mCycleAnimator.addUpdateListener(mCycleUpdateListener);
    }

    /**
     * 清除监听
     */
    private void removeListeners() {
        removeUpdateListener(mStartColorAnimator, mStartUpdateListener);
        removeUpdateListener(mStopColorAnimator, mStopUpdateListener);
        removeUpdateListener(mCycleAnimator, mCycleUpdateListener);

        removeListener(mStartColorAnimator, mStartListener);
        removeListener(mStopColorAnimator, mStopListener);
    }

    private void removeListener(ValueAnimator animator, AnimatorListenerAdapter listener) {
        if (animator != null && listener != null) {
            animator.removeListener(listener);
        }
    }

    public void removeUpdateListener(ValueAnimator animator, ValueAnimator.AnimatorUpdateListener listener) {
        if (animator != null && listener != null && animator.isRunning()) {
            animator.removeUpdateListener(listener);
            animator.cancel();
        }
    }


    private class CustomButtonViewGestureListener extends OnGestureListenerAdapter {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;  // 一定要返回true才能响应事件
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (listener != null) listener.onEnabled(enabled);
            if (!enabled) {
                return false;
            }
            if (mCurType == TYPE_STOP) {
                mCurType = TYPE_START;
                startColorAnimation();
                if (listener != null) listener.onStart();
            } else if (mCurType == TYPE_START) {
                mCurType = TYPE_STOP;
                stopColorAnimation();
                if (listener != null) listener.onStop();
            }
            return true;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 设置指定状态
     *
     * @param condition true 设置为开始态，false 设置为结束态
     */
    public void setToState(boolean condition) {
        setToState(condition, null);
    }

    public void setToState(boolean condition, String btnText) {
        if (condition) {
            if (mCurType != TYPE_START) {  // 置为正在录屏态
                mCurType = TYPE_START;
                startColorAnimation();
            }
            setBtnText(btnText == null ? UiUtil.getString(R.string.stop_record_main) : btnText);
        } else {
            if (mCurType != TYPE_STOP) {    // 置为停止录屏态
                mCurType = TYPE_STOP;
                stopColorAnimation();
            }
            setBtnText(btnText == null ? UiUtil.getString(R.string.start_record_main) : btnText);
        }
    }


    /**
     * 重置状态
     */
    public void reset() {
        if (mCurType == TYPE_START) {
            mCurType = TYPE_STOP;
            stopColorAnimation();
        }
    }

    public void setMsgText(String msgText) {
        this.mMsgText = msgText;
        postInvalidate();
    }

    public void setMsgText(int msgText) {
        this.mMsgText = getResources().getString(msgText);
        postInvalidate();
    }

    public void setBtnText(String btnText) {
        this.mBtnText = btnText;
        postInvalidate();
    }

    public void setBtnText(int btnText) {
        this.mBtnText = getResources().getString(btnText);
        postInvalidate();
    }

    public int getCurType() {
        return mCurType;
    }

    /**
     * 状态改变监听
     */
    public interface OnStateChangeListener {
        /**
         * 开始时调用
         */
        void onStart();

        /**
         * 结束时调用
         */
        void onStop();

        /**
         * 当是否可用时调用
         */
        void onEnabled(boolean enabled);
    }

    private OnStateChangeListener listener;

    public void setOnClickStateChangeListener(OnStateChangeListener listener) {
        this.listener = listener;
    }


    /**
     * 立即取消按钮呼吸效果
     */
    private void cancelButtonViewBreathAnimatedCycleImmediately() {
        if (mCycleAnimator != null && mCycleAnimator.isRunning()) {
            mCycleAnimator.cancel();
        }
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {    // view可见时
            addListeners();
            if (mCurType == TYPE_START) {
//                startButtonViewAnimatedBreathCycle();
            }
        } else {
//            cancelButtonViewBreathAnimatedCycle();
//            cancelButtonViewBreathAnimatedCycleImmediately();
            removeListeners();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeListeners();
    }
}
