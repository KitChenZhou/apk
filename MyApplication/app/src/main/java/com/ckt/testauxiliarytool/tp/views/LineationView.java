package com.ckt.testauxiliarytool.tp.views;

import android.content.Context;
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
import com.ckt.testauxiliarytool.tp.model.ScreenSize;
import com.ckt.testauxiliarytool.utils.UiUtil;

import static com.ckt.testauxiliarytool.utils.SizeUtil.dp2px;
import static com.ckt.testauxiliarytool.utils.SizeUtil.getRealScreenSize;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/10/19
 * <br/>TODO: 划线View
 */

public class LineationView extends View {
    /* 绘制参考线的起始点 */
    private PointF mStartPoint;
    /* 上次触摸点 */
    private PointF mLastPoint;
    /* 参考线*/
    private Paint mRefLinePaint;
    /* 轨迹 */
    private Paint mTracePaint;
    private Path mPath;

    float mDashWidth;  // 虚线间隙的长度
    float mLineWidth;  // 虚线每小段的长度

    private float mStrokeWidth; // 画笔线宽

    private boolean isDrawRefLine = false;

    ScreenSize screenSize;

    public LineationView(Context context) {
        this(context, null);
    }

    public LineationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mDashWidth = dp2px(context, 5);  // 虚线间隙的长度
        mLineWidth = dp2px(context, 10);  // 虚线每小段的长度
        mStrokeWidth = dp2px(context, 3); // 画笔线宽

        mLastPoint = new PointF();
        mStartPoint = new PointF(0, 0);
        mRefLinePaint = new Paint();
        mRefLinePaint.setColor(UiUtil.getColor(context, R.color.color_light_blue));
        mRefLinePaint.setAntiAlias(true);

        mTracePaint = new Paint();
        mTracePaint.setColor(Color.RED);
        mTracePaint.setStyle(Paint.Style.STROKE);
        mTracePaint.setStrokeWidth(dp2px(context, 1));
        mTracePaint.setAntiAlias(true);

        mPath = new Path();


        screenSize = new ScreenSize();
        getRealScreenSize(screenSize);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制参考线
        drawReferenceLine(canvas);

        // 绘制屏幕对角线
        drawScreenDiagonalLine(canvas);

        // 绘制手指轨迹
        drawPath(canvas);

    }


    /**
     * 绘制屏幕对角线
     *
     * @param canvas
     */
    private void drawScreenDiagonalLine(Canvas canvas) {
        if (!isDrawRefLine) {
            canvas.drawLine(0, 0, screenSize.screenWidth, screenSize.screenHeight, mRefLinePaint);
            canvas.drawLine(screenSize.screenWidth, 0, 0, screenSize.screenHeight, mRefLinePaint);
        }

    }


    /**
     * 绘制参考线
     */
    private void drawReferenceLine(Canvas canvas) {
        if (isDrawRefLine) {
            // 绘制参考线交汇处的圆
            canvas.drawCircle(mStartPoint.x, mStartPoint.y, dp2px(5), mRefLinePaint);
            drawHorizontalLine(canvas);
            drawVerticalLine(canvas);
            drawDiagonalLine(canvas);
        }
    }

    /**
     * 绘制参考线
     *
     * @param canvas Canvas
     */
    private void drawPath(Canvas canvas) {
        canvas.drawPath(mPath, mTracePaint);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartPoint.x = event.getX();
                mStartPoint.y = event.getY();
                isDrawRefLine = whetherDrawRefLine(event);
                mPath.reset();
                mPath.moveTo(mStartPoint.x, mStartPoint.y);
                if (touchListener!=null) touchListener.onTouchDown();
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.quadTo(mLastPoint.x, mLastPoint.y, event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                if (touchListener!=null) touchListener.onTouchUp();
                break;
        }
        mLastPoint.x = event.getX();
        mLastPoint.y = event.getY();
        invalidate();
        return true;
    }

    private boolean whetherDrawRefLine(MotionEvent event) {
        int r = 15;
        int x = (int) event.getX();
        int y = (int) event.getY();
        // 对屏幕4个角做特殊处理
        if (x < r && y < r) return false;
        if (x < r && y > screenSize.screenHeight - r) return false;
        if (y < r && x > screenSize.screenWidth - r) return false;
        if (x > screenSize.screenWidth - r && y > screenSize.screenWidth - r) return false;
        return true;
    }

    OnLineationTouchListener touchListener;
    public interface OnLineationTouchListener{
        void onTouchDown();
        void onTouchUp();
    }

    public void setOnLineationTouchListener(OnLineationTouchListener touchListener) {
        this.touchListener = touchListener;
    }
}
