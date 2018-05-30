package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

/**
 * Created by Cc on 2017/9/14.
 */

public class FunctionView extends android.support.v7.widget.AppCompatImageView {

    public static final int[] VIEW_FUNCTIONS = {R.id.switch_camera, R.id.lock_screen, R.id
            .switch_resolution, R.id.capture_hdr};

    public static final int STATE_FAIL = Color.RED;
    public static final int STATE_NORMAL = Color.TRANSPARENT;
    public static final int STATE_NORMAL_PRESSED = Color.GRAY;
    public static final int STATE_RUNNIG = Color.LTGRAY;
    public static final int STATE_SUCCESS = Color.GREEN;

    private static final SparseArray<FunctionView> ALL_IMAGE_VIEW = new SparseArray<>();

    /**
     * Current view state, value from {@link #STATE_FAIL}, {@link #STATE_NORMAL},
     * {@link #STATE_NORMAL_PRESSED}, {@link #STATE_RUNNIG}, {@link #STATE_SUCCESS}
     */
    private int mViewState;

    public FunctionView(Context context) {
        super(context);
        ALL_IMAGE_VIEW.put(getId(), this);
    }

    public FunctionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ALL_IMAGE_VIEW.put(getId(), this);
    }

    public FunctionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ALL_IMAGE_VIEW.put(getId(), this);
    }

    public static int getViewState(int resId) {
        return ALL_IMAGE_VIEW.get(resId).mViewState;
    }

    private static int[] getAllViewStates() {
        int[] viewStates = new int[ALL_IMAGE_VIEW.size()];
        for (int i = 0; i < ALL_IMAGE_VIEW.size(); i++) {
            viewStates[i] = ALL_IMAGE_VIEW.valueAt(i).mViewState;
        }
        return viewStates;
    }

    /**
     * It must call before Fragment's onDestroy(). Because the ALL_IMAGE_VIEW will be clean, in
     * Fragment's onDestroy().
     */
    public static void getAllViewStates(Context context) {
        int[] viewStates = SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, context)
                .getIntArray(getAllViewResIds(), 0);
        for (int i = 0; i < ALL_IMAGE_VIEW.size(); i++) {
            ALL_IMAGE_VIEW.valueAt(i).mViewState = viewStates[i];
        }
    }

    /**
     * It must call before Fragment's onDestroy(). Because the ALL_IMAGE_VIEW will be clean, in
     * Fragment's onDestroy().
     */
    public static void saveAllViewStates(Context context) {
        SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, context).putIntArray(getAllViewResIds
                (), getAllViewStates());
    }

    /**
     * It must call before Fragment's onDestroy(). Because the ALL_IMAGE_VIEW will be clean, in
     * Fragment's onDestroy().
     */
    private static int[] getAllViewResIds() {
        int[] viewResIds = new int[ALL_IMAGE_VIEW.size()];
        for (int i = 0; i < ALL_IMAGE_VIEW.size(); i++) {
            viewResIds[i] = ALL_IMAGE_VIEW.valueAt(i).getId();
        }
        return viewResIds;
    }

    /**
     * It must call before Fragment's onDestroy(). Because the ALL_IMAGE_VIEW will be clean, in
     * Fragment's onDestroy().
     */
    public static void setImageViewState(int resId, int viewState) {
        FunctionView functionView = ALL_IMAGE_VIEW.get(resId);
        if (functionView != null) functionView.mViewState = viewState;
    }

    public static void setImageViewState(Context context, int resId, int viewState) {
        setImageViewState(resId, viewState);
        SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, context).putInt(String.valueOf(resId)
                , viewState);
    }

    /**
     * Set view state and color, the color come from state.
     *
     * @param resId     view id.
     * @param viewState it is view state and color, for example {@link #STATE_FAIL}.
     */
    public static void updateViewColor(int resId, int viewState) {
        updateViewColor(resId, viewState, viewState);
    }

    public static void updateViewColor(int resId, int viewState, int color) {
        FunctionView functionView = ALL_IMAGE_VIEW.get(resId);
        if (functionView != null) {
            functionView.mViewState = viewState;
            functionView.setBackgroundColor(color);
        }
    }

    /**
     * According to the mViewState to set Background color.
     */
    public static void updateViewColor() {
        for (int i = 0; i < ALL_IMAGE_VIEW.size(); i++) {
            ALL_IMAGE_VIEW.valueAt(i).setBackgroundColor(ALL_IMAGE_VIEW.valueAt(i).mViewState);
        }
    }

    /**
     * Refresh view state and color.
     */
    public static void refreshView() {
        enabledView(true);

        for (int i = 0; i < ALL_IMAGE_VIEW.size(); i++) {
            ALL_IMAGE_VIEW.valueAt(i).mViewState = STATE_NORMAL;
        }
    }

    public static void enabledView(boolean enabled) {
        for (int i = 0; i < ALL_IMAGE_VIEW.size(); i++) {
            ALL_IMAGE_VIEW.valueAt(i).setEnabled(enabled);
        }
    }

    /**
     * It must be call in Fragment's onDestroy().
     */
    public static void cleanAllImageView() {
        ALL_IMAGE_VIEW.clear();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            this.setBackgroundColor(STATE_NORMAL_PRESSED);
        } else {
            this.setBackgroundColor(STATE_NORMAL);
        }
    }

}
