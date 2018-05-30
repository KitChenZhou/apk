package com.ckt.testauxiliarytool.tp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.model.ScreenInfo;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;
import com.ckt.testauxiliarytool.utils.UiUtil;

import static com.ckt.testauxiliarytool.utils.SizeUtil.dp2px;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/17.
 * TODO:自定义View，用于测量两指间的距离
 */

public class DistanceTouchView extends View {
    /* 绘制参考线的起始点 */
    private PointF mStartPoint;
    /* 上次触摸点 */
    private PointF mLastPoint;
    /* 第一指 */
    private PointF mFirst;
    /* 第二指 */
    private PointF mSecond;

    private int mPointCount = 0;

    /* 两指距离，px */
    double distance_px;
    /* 两指距离，mm */
    private double distance_mm;

    /* 文本*/
    private Paint mTextPaint;
    /* 矩形 */
    private Paint mRectPaint;
    /* 参考线*/
    private Paint mRefLinePaint;
    /* 轨迹 */
    private Paint mTracePaint;
    private Path mPath;

    // mm
    private float mWidth = 0;
    private float mHeight = 0;

    // px
    private float mWidthPixels;
    private float mHeightPixels;

    // scale : 换算比例
    private float xScale = 0;
    private float yScale = 0;

    private int mLineHeight;
    float mDashWidth;  // 虚线间隙的长度
    float mLineWidth;  // 虚线每小段的长度

    private float mStrokeWidth; // 画笔线宽
    /**
     * 是否在某个时刻绘制参考线
     */
    private boolean isDrawRefLine = false;
    private long mLastTime = 0;


    /**
     * 不再绘制参考线
     */
    private boolean isNeverDrawRefLine = false;

    /* 是否可以触发触摸事件*/
    private boolean touchable = true;

    public DistanceTouchView(Context context) {
        this(context, null);
    }

    public DistanceTouchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DistanceTouchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLineHeight = dp2px(context, 10);
        mDashWidth = dp2px(context, 5);  // 虚线间隙的长度
        mLineWidth = dp2px(context, 10);  // 虚线每小段的长度
        mStrokeWidth = dp2px(context, 3); // 画笔线宽

        mStartPoint = new PointF(0, 0);
        mFirst = new PointF();
        mSecond = new PointF();
        mLastPoint = new PointF();

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(dp2px(context, 18));

        mRectPaint = new Paint();
        mRectPaint.setColor(UiUtil.getColor(context, R.color.color_light_blue));
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(dp2px(context, 2));
        mRectPaint.setAntiAlias(true);

        mRefLinePaint = new Paint();
        mRefLinePaint.setColor(UiUtil.getColor(context, R.color.color_light_blue));
        mRefLinePaint.setAntiAlias(true);

        mTracePaint = new Paint();
        mTracePaint.setColor(Color.RED);
        mTracePaint.setStyle(Paint.Style.STROKE);
        mTracePaint.setStrokeWidth(dp2px(context, 1));
        mTracePaint.setAntiAlias(true);

        mPath = new Path();

        parseScreenSize();
        setEnabled(true);
        setClickable(true);

        TypedArray ta = context.getResources().obtainAttributes(attrs, R.styleable.DistanceTouchView);
        touchable = ta.getBoolean(R.styleable.DistanceTouchView_touch_enable, true);

        ta.recycle();
    }

    /**
     * 设置是否绘制参考线
     *
     * @param isDrawRefLine    是否绘制?
     * @param neverDrawRefLine 禁止绘制?
     */
    public void setWhetherDrawRefLine(boolean isDrawRefLine, boolean neverDrawRefLine) {
        this.isDrawRefLine = isDrawRefLine;
        this.isNeverDrawRefLine = neverDrawRefLine;
        postInvalidate();
    }

    /**
     * 解析屏幕尺寸信息
     */
    private void parseScreenSize() {
        /* 获取物理尺寸，mm */
        String screen_width = SharedPrefsUtil.name("screen_info").getString("screen_width", null);
        String screen_height = SharedPrefsUtil.name("screen_info").getString("screen_height", null);
        if (screen_width != null) {
            mWidth = Float.parseFloat(screen_width);
        }

        if (screen_height != null) {
            mHeight = Float.parseFloat(screen_height);
        }
         /* 获取物理尺寸，px */
        String sScreenInfo;
        if ((sScreenInfo = SharedPrefsUtil.name("screen_info").getString("screenInfo", null)) != null) {
            String[] args = sScreenInfo.split(",");
            ScreenInfo screenInfo = new ScreenInfo(args);
            mWidthPixels = screenInfo.widthPixels;
            mHeightPixels = screenInfo.heightPixels;

            if (mWidth <= 0 || mHeight <= 0) {  // 获取物理尺寸失败
                mWidth = screenInfo.realWidth; // 使用自动获取的宽高
                mHeight = screenInfo.realHeight;
            }

            /* 计算比率*/
            xScale = mWidth / mWidthPixels;
            yScale = mHeight / mHeightPixels;
        }

        SharedPrefsUtil.recycle("screen_info");
    }


    public float getXScale() {
        return xScale;
    }

    /**
     * 更新尺寸
     */
    public void updateSize() {

        if (mWidthPixels == 0 || mHeightPixels == 0 ) {
            parseScreenSize();
        }
        // 获取屏幕实际宽高
        String screen_width = SharedPrefsUtil.name("screen_info").getString("screen_width", null);
        String screen_height = SharedPrefsUtil.name("screen_info").getString("screen_height", null);
        SharedPrefsUtil.recycle("screen_info");
        if (screen_width != null && screen_height != null) {
            mWidth = Float.parseFloat(screen_width);
            mHeight = Float.parseFloat(screen_height);

            xScale = mWidth / mWidthPixels;
            yScale = mHeight / mHeightPixels;

            invalidate();
        }
    }


    /**
     * 更新距离信息
     *
     * @param first        {@link PointF}
     * @param second       {@link PointF}
     * @param pointerCount int
     */
    public void updateDistanceInfo(PointF first, PointF second, int pointerCount) {
        mPointCount = pointerCount;
        invalidate();
        if (first == null || second == null) {
            return;
        }
        doCalcDistance(first, second);

        postInvalidate();
    }

    // 重写onTouchEvent，完成滑动事件的处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!touchable) return false;
        mPointCount = event.getPointerCount();
        calcDistance(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtil.i("TAG", "x=" + event.getX() + ",y=" + event.getY());
                getParent().requestDisallowInterceptTouchEvent(true);
                isDrawRefLine = true;
                mStartPoint.x = event.getX();
                mStartPoint.y = event.getY();
                mPath.reset();
                mPath.moveTo(mStartPoint.x, mStartPoint.y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDrawRefLine)
                    mPath.quadTo(mLastPoint.x, mLastPoint.y, event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                if (event.getPointerCount() == 1) mPointCount = 0;// 只有一根手指时，并抬起
                if (event.getPointerCount() == 2) mPointCount = 1;
                if (listener != null) {
                    listener.onTouchUp();
                }

                break;
        }
        mLastPoint.x = event.getX();
        mLastPoint.y = event.getY();

        invalidate();  // 刷新UI
        return true;
    }

    /**
     * 计算距离并刷新
     *
     * @param event {@link MotionEvent}
     */
    private void calcDistance(MotionEvent event) {
        if (event.getPointerCount() >= 2) {  // 手指数大于等于2时才进行计算
            isDrawRefLine = false;
            if (listener != null) listener.onTouchDown();
            mFirst.set(event.getX(0), event.getY(0));
            mSecond.set(event.getX(1), event.getY(1));
            doCalcDistance(mFirst, mSecond);
        }
    }

    /**
     * 计算两指距离
     *
     * @param first  手指1
     * @param second 手指2
     */
    private void doCalcDistance(PointF first, PointF second) {
        float dx = Math.abs(first.x - second.x); // 两指水平距离
        float dy = Math.abs(first.y - second.y); // 两指竖直距离
        distance_px = splitAndRound(Math.sqrt(dx * dx + dy * dy), 2);
        float scaleX = dx * xScale;
        float scaleY = dy * yScale;
        distance_mm = splitAndRound(Math.sqrt(scaleX * scaleX + scaleY * scaleY), 2);
        LogUtil.i(DistanceTouchView.class.getName(), "distance_px=" + distance_px + ", distance_mm=" + distance_mm);
    }


    /**
     * 取几位小数
     *
     * @param a 操作对象
     * @param n 保留位数
     * @return double
     */
    public double splitAndRound(double a, int n) {
        a = a * Math.pow(10, n);
        return (Math.round(a)) / (Math.pow(10, n));
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return super.onGenericMotionEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制测距显示的窗格
        drawDistancePane(canvas);

        // 绘制参考线
        drawReferenceLine(canvas);

        // 绘制手指轨迹
        drawPath(canvas);
    }

    /**
     * 绘制参考线
     *
     * @param canvas Canvas
     */
    private void drawPath(Canvas canvas) {
        if (isDrawRefLine)
            canvas.drawPath(mPath, mTracePaint);
    }

    /**
     * 绘制测距显示的窗格
     *
     * @param canvas {@link Canvas}
     */
    private void drawDistancePane(Canvas canvas) {
        if (System.currentTimeMillis() - mLastTime > 1) { // 每隔1ms绘制一次
            mLastTime = System.currentTimeMillis();
            drawResultPane(canvas);
        }
    }

    /**
     * 绘制测距结果栏
     *
     * @param canvas Canvas
     */
    private void drawResultPane(Canvas canvas) {
        float base = -mTextPaint.ascent() + 2;
        if (xScale == 0) {  // mm / px
            mTextPaint.setColor(Color.RED);
            canvas.drawText("未识别屏幕宽高", 5, base, mTextPaint);
            return;
        }
        mTextPaint.setColor(Color.BLACK);

        float itemW = getWidth() / 7;
        float top = 2;
        float bottom = mTextPaint.descent() - mTextPaint.ascent() + 6;
        int textOffset = 15;


        // 绘制触摸点数矩形与文字
        canvas.drawRect(1, top, itemW, bottom, mRectPaint); // 0~1 itemW
        canvas.drawText("P:" + mPointCount, textOffset, base, mTextPaint);
        LogUtil.i(DistanceTouchView.class.getName(), "y=" + -mTextPaint.ascent() + 1);

        // 绘制两点距离(mm)矩形与文字
        canvas.drawRect(itemW, top, itemW * 4, bottom, mRectPaint); // 1~4 itemW
        canvas.drawText("d_mm:" + distance_mm + "mm", itemW + textOffset, base, mTextPaint);

        // 绘制两点距离(px)矩形与文字
        canvas.drawRect(4 * itemW, top, itemW * 7, bottom, mRectPaint);  // 4~7 itemW
        canvas.drawText("d_px:" + distance_px + "px", itemW * 4 + textOffset, base, mTextPaint);
    }

    /**
     * 绘制参考线
     */
    private void drawReferenceLine(Canvas canvas) {
        if (isDrawRefLine && !isNeverDrawRefLine) {
            // 绘制参考线交汇处的圆
            canvas.drawCircle(mStartPoint.x, mStartPoint.y, dp2px(5), mRefLinePaint);
            drawHorizontalLine(canvas);
            drawVerticalLine(canvas);
            drawDiagonalLine(canvas);
        }
    }

    /**
     * 绘制对角线
     *
     * @param canvas
     */
    private void drawDiagonalLine(Canvas canvas) {
        float[] pts = {mStartPoint.x, mStartPoint.y, mStartPoint.x + mLineWidth, mStartPoint.y};
        for (int i = 0; i < 4; i++) {
            float totalWidth = 0;
            canvas.save();
            canvas.rotate(45 * (2 * i + 1), mStartPoint.x, mStartPoint.y);
            mRefLinePaint.setStrokeWidth(mStrokeWidth);
            while (totalWidth <= getWidth() + getHeight()) { // 从起始点开始画水平线
                canvas.drawLines(pts, mRefLinePaint);  // 绘制一段
                canvas.translate(mLineWidth + mDashWidth, 0);  // 平移, 线宽+间距
                totalWidth += mLineWidth + mDashWidth; // 统计已经绘制的
            }
            canvas.restore();
        }

    }


    /**
     * 画水平方向虚线
     *
     * @param canvas {@link Canvas}
     */
    public void drawHorizontalLine(Canvas canvas) {
        float totalWidth = 0;
        canvas.save();

        float[] pts = {0, mStartPoint.y, mLineWidth, mStartPoint.y};
        //因为画线段的起点位置在线段左下角
        //在画线之前需要先把画布向下平移办个线段高度的位置，目的就是为了防止线段只画出一半的高度
        //canvas.translate(0, mLineHeight / 2);
        mRefLinePaint.setStrokeWidth(mStrokeWidth);
        while (totalWidth <= getWidth()) { // 从起始点开始画水平线
            canvas.drawLines(pts, mRefLinePaint);  // 绘制一段
            canvas.translate(mLineWidth + mDashWidth, 0);  // 平移, 线宽+间距
            totalWidth += mLineWidth + mDashWidth; // 统计已经绘制的
        }

        canvas.restore();
    }

    /**
     * 画竖直方向虚线
     *
     * @param canvas {@link Canvas}
     */
    public void drawVerticalLine(Canvas canvas) {
        float totalHeight = 0;
        canvas.save();
        float[] pts = {mStartPoint.x, 0, mStartPoint.x, mLineWidth};
        //因为画线段的起点位置在线段左下角
        //在画线之前需要先把画布向右平移半个线段高度的位置，目的就是为了防止线段只画出一半的高度
        //canvas.translate(mLineHeight / 2, 0);
        mRefLinePaint.setStrokeWidth(mStrokeWidth);
        while (totalHeight <= getHeight()) {  // 从起始点开始画竖直线
            canvas.drawLines(pts, mRefLinePaint);
            canvas.translate(0, mLineWidth + mDashWidth);
            totalHeight += mLineWidth + mDashWidth;
        }
        canvas.restore();
    }

    /**
     * 滑动状态改变监听
     */
    public interface OnTouchStateChangeListener {
        /**
         * 双指按下
         */
        void onTouchDown();

        /**
         *
         */
        void onTouchUp();
    }

    OnTouchStateChangeListener listener;

    public void setOnTouchStateChangeListener(OnTouchStateChangeListener listener) {
        this.listener = listener;
    }
}
