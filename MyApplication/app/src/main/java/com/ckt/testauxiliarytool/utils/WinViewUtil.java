package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.ckt.testauxiliarytool.tp.model.ScreenSize;

/**
 * Window浮窗显示
 *
 * @author pecuyu
 */
public class WinViewUtil {
    private WinViewListener mListener;
    private WindowManager mWm;
    private WindowManager.LayoutParams mParams;
    private View mRootView;
    private int mStartX = 0, mStartY = 0;
    // 屏幕宽高
    private int screenWidth = 0, screenHeight = 0;
    private TextView mTvTime;

    /**
     * 是否滑动来改变当前win view 的位置
     */
    private boolean isMoveToUpdateLayout = true;

    private boolean isShowing = false;

    private WinViewUtil(Context context, WindowManager.LayoutParams params, View customView, boolean isMoveToUpdateLayout) {
        super();
        this.mParams = params;
        mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        ScreenSize outSize = new ScreenSize();
        SizeUtil.getScreenSize(outSize);
        screenHeight = outSize.screenWidth;
        screenWidth = outSize.screenHeight;
        mRootView = customView;
        this.isMoveToUpdateLayout = isMoveToUpdateLayout;

    }

    private void setParams(WindowManager.LayoutParams params) {
        this.mParams = params;
    }

    public void show() {
        if (isShowing) {
            return;
        }

        if (mParams.x == 0)
            mParams.x = SharedPrefsUtil.name("config").getInt("mStartX", 5);      // 设置显示位置
        if (mParams.y == 0) mParams.y = SharedPrefsUtil.name("config").getInt("mStartY", 0);
        // 将View添加到窗体管理器
        mWm.addView(mRootView, mParams);
        isShowing = true;
        initEvents();
    }

    private void initEvents() {
        mRootView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mListener != null) {     // 当启用回调时
                    mListener.toastCallBack(mWm, mParams, mRootView, event);
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MotionEvent.PointerCoords out = new MotionEvent.PointerCoords();
                        event.getPointerCoords(0, out);
                        LogUtil.e("TAG", "out.x=" + out.x + ",out.y=" + out.y);
                        v.performClick();
                        mStartX = (int) event.getRawX();
                        mStartY = (int) event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int endX = (int) event.getRawX();
                        int endY = (int) event.getRawY();
                        int dX = endX - mStartX;
                        int dY = endY - mStartY;
                        mParams.x += dX;
                        mParams.y += dY;

                        // 防止越界
                        if (mParams.x < 0) {
                            mParams.x = 0;
                        }
                        if (mParams.y < 0) {
                            mParams.y = 0;
                        }
                        if (mParams.x > screenWidth - mRootView.getWidth()) {
                            mParams.x = screenWidth - mRootView.getWidth();
                        }
                        if (mParams.y > screenHeight - mRootView.getHeight()) {
                            mParams.y = screenHeight - mRootView.getHeight();
                        }
                        // 更新View
                        if (isMoveToUpdateLayout) {
                            mWm.updateViewLayout(mRootView, mParams);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        // 记录下次显示的初始位置
                        SharedPrefsUtil.name("config").putInt("mStartX", mParams.x);
                        SharedPrefsUtil.name("config").putInt("mStartY", mParams.y);

                        break;
                }
                mStartX = (int) event.getRawX();
                mStartY = (int) event.getRawY();
                return false;
            }
        });

        /**
         *
         * 双击使view居中
         */
        final long[] source = new long[2];
        mRootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(source, 1, source, 0, source.length - 1);
                source[source.length - 1] = System.currentTimeMillis();
                if (source[source.length - 1] - source[0] < 500) {
                    mParams.x = screenWidth / 2 - mRootView.getWidth() / 2;
                    mParams.y = screenHeight / 2 - mRootView.getHeight() / 2;
                    mWm.updateViewLayout(mRootView, mParams);
                }
            }
        });
    }

    /**
     * 使浮窗消失
     */
    public void dismiss() {
        if (mWm != null && mRootView != null && isShowing) {
            mWm.removeView(mRootView);
            mTvTime = null;
            isShowing = false;

            SharedPrefsUtil.recycle("config");

            mRootView.setOnClickListener(null);
            mRootView.setOnTouchListener(null);
        }

    }

    /**
     * 设置文本内容
     *
     * @param view
     * @param id
     * @param text
     */
    public void setTextViewText(View view, int id, String text) {
        if (view != mRootView || text == null || !isShowing) {
            return;
        }

        if (mTvTime == null) {
            mTvTime = (TextView) mRootView.findViewById(id);
        }
        mTvTime.setText(text);
        mWm.updateViewLayout(mRootView, mParams);
    }

    /**
     * 浮窗监听
     *
     * @author pecuyu
     */
    public interface WinViewListener {

        /**
         * 设置回调事件
         *
         * @param wm
         * @param params
         * @param view
         * @param event
         */
        public void toastCallBack(WindowManager wm, WindowManager.LayoutParams params, View view, MotionEvent event);
    }

    /**
     * 设置自定义窗体的点击事件
     *
     * @param listener
     */
    public void setOnTouchListener(WinViewListener listener) {
        this.mListener = listener;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public View getRootView() {
        return mRootView;
    }

    public WindowManager.LayoutParams getParams() {
        return mParams;
    }

    public static class Builder {
        private WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        private View customView;
        private boolean isMoveToUpdateLayout = true;
        private int x = 0, y = 0;   //  浮窗坐标

        private Context context;

        {
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.type = WindowManager.LayoutParams.TYPE_PHONE;// 电话窗口。它用于电话交互（特别是呼入）。它置于所有应用程序之上，状态栏之下。
            params.gravity = Gravity.TOP + Gravity.LEFT; // 将重心设置为左上方
            params.format = PixelFormat.TRANSLUCENT;    // 半透明
        }

        public Builder setX(int x) {
            this.x = x;
            params.x = x;
            return this;
        }

        public Builder setY(int y) {
            this.y = y;
            params.y = y;
            return this;
        }

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMoveToUpdateLayout(boolean moveToUpdateLayout) {
            isMoveToUpdateLayout = moveToUpdateLayout;
            return this;
        }

        public boolean isMoveToUpdateLayout() {
            return isMoveToUpdateLayout;
        }

        public Builder setCustomView(View customView) {
            this.customView = customView;
            return this;
        }

        public View getCustomView() {
            return customView;
        }

        public void setParams(WindowManager.LayoutParams params) {
            this.params = params;
        }

        public WindowManager.LayoutParams getParams() {
            return params;
        }

        public WinViewUtil create() {
            if (customView == null) {
                throw new RuntimeException("customView should not be null");
            }
            return new WinViewUtil(context, params, customView, isMoveToUpdateLayout);
        }

        public WinViewUtil show() {
            WinViewUtil winViewUtil = create();
            winViewUtil.show();
            return winViewUtil;
        }
    }

    public void clear() {
        if (isShowing()) {
            dismiss();
        }
        mRootView.setOnClickListener(null);
        mRootView.setOnTouchListener(null);
    }

}
