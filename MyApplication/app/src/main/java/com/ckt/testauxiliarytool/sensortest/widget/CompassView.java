package com.ckt.testauxiliarytool.sensortest.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ckt.testauxiliarytool.R;

/**
 * Created by D22434 on 2017/8/25.
 */

public class CompassView extends View {

    private Paint mLinePaint;
    private Paint mMainLinePaint;
    private Paint mPaint;

    private Paint mAnglePaint;
    private Paint mDegreePaint;
    private Path mPath;
    private Rect mRect;

    private float mAngleSize;
    private float mDegreeSize;
    private float mTextSize;
    private float mLineLength;
    private float mMainLineLength;
    private float mCircleRadius;

    private int rotate = 0;
    private float curDegree = 0;
    private float x, y, r;
    private float[] temp;
    String degree;
    String mDegreeText;

    private float[] points = new float[2];

    //外置接口
    public void setRotate(float rotate) {
        curDegree = rotate;
        this.rotate = Math.round(rotate);//采用round方式转换为整型
        invalidate();
    }

    public CompassView(Context context) {
        super(context, null);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.CompassView);
        int lineColor = t.getColor(R.styleable.CompassView_lineColor, Color.BLACK);
        int keyColor = t.getColor(R.styleable.CompassView_keyLineColor, Color.BLACK);
        int mainColor = t.getColor(R.styleable.CompassView_mainLineColor, Color.BLACK);
        mDegreeSize = t.getDimension(R.styleable.CompassView_mDegreeSize, 50f);
        mAngleSize = t.getDimension(R.styleable.CompassView_mAngleSize, 20f);
        mTextSize = t.getDimension(R.styleable.CompassView_mTextSize, 30f);

        mLineLength = t.getDimension(R.styleable.CompassView_mLineLength, 30f);
        mMainLineLength = t.getDimension(R.styleable.CompassView_mainLineLength, 30f);
        mCircleRadius = mLineLength;

        mLinePaint = new Paint();
        mLinePaint.setColor(lineColor);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);


        mMainLinePaint = new Paint();
        mMainLinePaint.setColor(mainColor);
        mMainLinePaint.setStrokeWidth(5);
        mMainLinePaint.setAntiAlias(true);
        mMainLinePaint.setStyle(Paint.Style.STROKE);


        mAnglePaint = new Paint();
        mAnglePaint.setAntiAlias(true);
        mAnglePaint.setColor(lineColor);
        mAnglePaint.setStyle(Paint.Style.STROKE);


        mDegreePaint = new Paint();
        mDegreePaint.setColor(keyColor);
        mDegreePaint.setTextSize(mDegreeSize);
        mDegreePaint.setAntiAlias(true);
        mDegreePaint.setStyle(Paint.Style.STROKE);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#F44336"));
        mPaint.setStyle(Paint.Style.FILL);

        mPath = new Path();
        mRect = new Rect();
        t.recycle();

    }

    /**
     * 测量宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        x = measure(widthMeasureSpec) / 2;
        y = measure(heightMeasureSpec) / 2;
        if (x > y) {
            r = (float) (y * 0.7);
        } else {
            r = (float) (x * 0.7);
        }
    }

    protected int measure(int measureSpec) {
        int size = 0;
        int measureMode = MeasureSpec.getMode(measureSpec);
        if (measureMode == MeasureSpec.UNSPECIFIED) {
            size = 250;
        } else {
            size = MeasureSpec.getSize(measureSpec);
        }
        return size;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制标尺
        canvas.drawLine(x, y - r, x, y - r - mMainLineLength, mMainLinePaint);

        //绘制当前度数
        mDegreeText = curDegree + "°";
        mDegreePaint.getTextBounds(mDegreeText, 0, mDegreeText.length(), mRect);
        canvas.drawText(mDegreeText, x - mDegreePaint.measureText(mDegreeText) / 2,
                y + mRect.height() / 2, mDegreePaint);

        //顺时针旋转
        canvas.rotate(-rotate, x, y);

        //绘制刻度
        for (int i = 0; i < 120; i++) {
            if (i == 0) {
                //绘制三角形
                mPath.moveTo(x - mLineLength / 2, y - r - mLineLength - 5);
                mPath.lineTo(x + mLineLength / 2, y - r - mLineLength - 5);
                mPath.lineTo(x, (float) (y - r - mLineLength - mLineLength / 2 * Math.sqrt(3)));
                canvas.drawPath(mPath, mPaint);
            }
            if (i % 10 == 0) {
                mLinePaint.setStrokeWidth(4);
                canvas.drawLine(x, y - r, x, y - r - mLineLength, mLinePaint);

            } else {
                mLinePaint.setStrokeWidth(2);
                canvas.drawLine(x, y - r, x, y - r - mLineLength, mLinePaint);
            }
            canvas.rotate(3, x, y);
        }

        //绘制文字.先旋转后还原，保持文字正常显示
        for (int i = 0; i < 12; i++) {
            degree = i * 30 + "°";
            temp = calculatePoint(30 * i, r - mCircleRadius);
            if (i == 0) {
                degree = "北";
                mAnglePaint.setTextSize(mTextSize);
            } else if (i == 3) {
                degree = "东";
                mAnglePaint.setTextSize(mTextSize);
            } else if (i == 6) {
                degree = "南";
                mAnglePaint.setTextSize(mTextSize);
            } else if (i == 9) {
                degree = "西";
                mAnglePaint.setTextSize(mTextSize);
            } else {
                mAnglePaint.setTextSize(mAngleSize);
            }

            //绘制旋转的度数
            if (rotate != 0) {
                canvas.rotate(rotate, x + temp[1], y - temp[0]);
            }
            canvas.drawText(degree, x + temp[1] - mAnglePaint.measureText(degree) / 2, y - temp[0] + mAnglePaint.getTextSize() / 2, mAnglePaint);
            if (rotate != 0) {
                canvas.rotate(-rotate, x + temp[1], y - temp[0]);
            }
        }

    }

    //计算角度数值的起始坐标
    private float[] calculatePoint(float angle, float r) {
        points[0] = (float) (r * Math.cos(Math.PI * angle / 180));
        points[1] = (float) (r * Math.sin(Math.PI * angle / 180));
        return points;
    }

}
