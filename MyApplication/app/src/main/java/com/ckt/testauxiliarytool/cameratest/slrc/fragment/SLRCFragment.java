package com.ckt.testauxiliarytool.cameratest.slrc.fragment;

import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.Size;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.slrc.model.FunctionView;
import com.ckt.testauxiliarytool.cameratest.slrc.CameraActivity;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;
import com.ckt.testauxiliarytool.cameratest.slrc.model.ManageCamera;
import com.ckt.testauxiliarytool.cameratest.slrc.model.SLRCCamera1Manager;
import com.ckt.testauxiliarytool.cameratest.slrc.model.SLRCCamera2Manager;
import com.ckt.testauxiliarytool.utils.LogUtil;

import java.lang.ref.WeakReference;

/**
 * Created by Cc on 2017/9/25.
 * Use this to show SLRC function.
 * SLRC: Switch Camera, Lock Screen, Switch Resolution, Capture HDR.
 */

public class SLRCFragment extends CameraFragment {

    private static final int WHAT_ENABLE_VIEW = 0;
    private static final int WHAT_UPDATE_VIEW_COLOR = 1;
    private static final int WHAT_UPDATE_TEXT_TIPS = 2;
    private static final int WHAT_UPDATE_TEXT_TIMES = 3;
    private static final int WHAT_UPDATE_TEXT_NONE = 5;
    private static final int WHAT_SWITCH_CAMERA = 6;
    private static final int WHAT_SWITCH_RESOLUTION = 7;
    private static final int WHAT_CAPTURE_HDR = 8;
    private static final int WHAT_SWITCH_TO_DEFAULT_CAMERA = 9;

    /**
     * A RadioGroup to show Where the page is.
     */
    private RadioGroup mRadioGroup;

    private TextView mDoNotPressScreen;
    private TextView mCurrentTimes;
    private TextView mCurrentJPGResolution;

    private static SLRCThread sSLRCThread;

    private Handler mUpdateUiHandler = new UpdateUiHandler(this);

    @Override
    int setLayoutResId() {
        return R.layout.ct_slrc_fragment;
    }

    @Override
    void initView(View view) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
        mDoNotPressScreen = (TextView) view.findViewById(R.id.donot_press_screen);
        mCurrentTimes = (TextView) view.findViewById(R.id.current_times);
        mCurrentJPGResolution = (TextView) view.findViewById(R.id.current_jpg_resolution);
    }

    @Override
    void setListener() {
    }

    @Override
    void initData() {
        super.initData();
    }

    @Override
    void recycleObject() {
        super.recycleObject();
        if (!isSLRCThreadStop()) {
            FunctionView.setImageViewState(getContext(), sSLRCThread.getViewResId(), FunctionView
                    .STATE_FAIL);
            SLRCFragment.stopSLRCThread();
        }
    }

    @Override
    void initLOrLater() {
        mManageCamera = new SLRCCamera2Manager(this, mTextureView);
    }

    @Override
    void initBeforeL() {
        mManageCamera = new SLRCCamera1Manager(this, mTextureView);
    }

    public static SLRCFragment newInstance() {
        return new SLRCFragment();
    }

    public void setCurrentResolutionData(boolean show) {
        String jpgResolution;

        if (show) {
            Size imageSize = ((ManageCamera) mManageCamera).getImageSize();
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                jpgResolution = getString(R.string.ct_current_jpg_resolution) + "【" + imageSize
                        .getWidth() + "×" + imageSize.getHeight() + "（" + imageSize.getWidth() *
                        imageSize.getHeight() / 10000 + "万）】";
            } else {
                jpgResolution = getString(R.string.ct_current_jpg_resolution) + "【" + imageSize
                        .getHeight() + "×" + imageSize.getWidth() + "（" + imageSize.getWidth() *
                        imageSize.getHeight() / 10000 + "万）】";
            }
        } else jpgResolution = "";

        mCurrentJPGResolution.setText(jpgResolution);
    }

    /**
     * Called by {@link SLRCThread}
     *
     * @param currentTimes Be used for showing current repeat times in TextView.
     */
    private void setCurrentTimesData(int currentTimes) {
        String currentTimesStr;

        if (!isSLRCThreadStop() && currentTimes > 0)
            currentTimesStr = getString(R.string.ct_current_times) + "【" + currentTimes + "】";
        else currentTimesStr = "";

        mCurrentTimes.setText(currentTimesStr);
    }

    /**
     * Whether visible the TextView show don't press screen message.
     *
     * @param visible If true the view visible, false the view gone.
     */
    private void visibleDoNotPressScreen(boolean visible) {
        if (visible) {
            if (mDoNotPressScreen.getVisibility() != View.VISIBLE)
                mDoNotPressScreen.setVisibility(View.VISIBLE);
        } else {
            if (mDoNotPressScreen.getVisibility() != View.GONE)
                mDoNotPressScreen.setVisibility(View.GONE);
        }

    }

    /**
     * Change RadioGroup checked
     */
    public void onSwitchRadioButtonChecked(String checked) {
        if ("start".equals(checked)) {
            mRadioGroup.check(R.id.rb_start);
        } else {
            mRadioGroup.check(R.id.rb_main);
        }
    }

    private int getImageSizesLength() {
        return ((ManageCamera) mManageCamera).getImageSizesLength();
    }

    public void startSLRCThread(int viewResId, int repeatTimes) {
        new SLRCThread(viewResId, repeatTimes).start();
    }

    /**
     * Whether SLRCThread is stop.
     *
     * @return true means is stop.
     */
    public static boolean isSLRCThreadStop() {
        return sSLRCThread == null;
    }

    public static void stopSLRCThread() {
        if (sSLRCThread != null) sSLRCThread.interrupt();
        sSLRCThread = null;
    }

    private static class UpdateUiHandler extends Handler {

        private final WeakReference<SLRCFragment> mWeakSLRCFragment;

        UpdateUiHandler(SLRCFragment slrcFragment) {
            mWeakSLRCFragment = new WeakReference<>(slrcFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakSLRCFragment.get() != null) {
                switch (msg.what) {
                    case WHAT_ENABLE_VIEW:
                        FunctionView.enabledView(true);
                        FunctionView.updateViewColor();
                        break;
                    case WHAT_UPDATE_VIEW_COLOR:
                        FunctionView.updateViewColor(msg.arg1, msg.arg2);
                        break;
                    case WHAT_UPDATE_TEXT_TIPS:
                        mWeakSLRCFragment.get().visibleDoNotPressScreen((Boolean) msg.obj);
                        break;
                    case WHAT_UPDATE_TEXT_TIMES:
                        mWeakSLRCFragment.get().setCurrentTimesData(msg.arg1 - msg.arg2);
                        break;
                    case WHAT_UPDATE_TEXT_NONE:
                        mWeakSLRCFragment.get().visibleDoNotPressScreen(false);
                        mWeakSLRCFragment.get().setCurrentTimesData(0);
                        mWeakSLRCFragment.get().setCurrentResolutionData(false);
                        break;
                    case WHAT_SWITCH_CAMERA:
                        if (!isSLRCThreadStop() && mWeakSLRCFragment.get().isPreview())
                            mWeakSLRCFragment.get().getManageCamera().switchCamera();
                        break;
                    case WHAT_SWITCH_RESOLUTION:
                        if (!isSLRCThreadStop() && mWeakSLRCFragment.get().isPreview())
                            mWeakSLRCFragment.get().getManageCamera().switchResolution();
                        break;
                    case WHAT_CAPTURE_HDR:
                        if (!isSLRCThreadStop() && mWeakSLRCFragment.get().isPreview())
                            mWeakSLRCFragment.get().getManageCamera().captureInHDR();
                        break;
                    case WHAT_SWITCH_TO_DEFAULT_CAMERA:
                        if (!isSLRCThreadStop() && mWeakSLRCFragment.get().isPreview())
                            mWeakSLRCFragment.get().getManageCamera().switchToDefaultCamera();
                        break;
                }
            }
        }
    }

    private class SLRCThread extends Thread {

        /**
         * When operation the time interval of the preview.
         */
        private static final int OPERATION_PREVIEW_TIME_INTERVAL = 2500;

        /**
         * Sleep screen time.
         */
        private static final int SLEEP_SCREEN_TIME = 2500;

        /**
         * We need to wait camera operation. It was a inaccurate time
         */
        private static final int CAMERA_OPERATION_TIME = 1000;

        /**
         * When we need to pause, this is minimal inaccurate time
         */
        private static final int PAUSE_TIME_UNIT = 1000;

        private static final int PAUSE_TIMES = 60 * 5;

        private int mViewResId;
        private int mRepeatTimes = 20;

        private final int mTotalTimes;

        /**
         * Thread to do repetitive operation.
         *
         * @param viewResId   the operation you it need to repeat.
         * @param repeatTimes the times to repeat.
         */
        private SLRCThread(int viewResId, int repeatTimes) {
            mViewResId = viewResId;
            mTotalTimes = repeatTimes;
            mRepeatTimes = repeatTimes;

            mUpdateUiHandler.sendMessage(mUpdateUiHandler.obtainMessage(WHAT_UPDATE_VIEW_COLOR,
                    viewResId, FunctionView.STATE_RUNNIG));

            sSLRCThread = this;
        }

        @Override
        public void run() {

            try {
                waitForPreview();

                // Whatever the camera in what state, we change it to default camera when we
                // start new SLRCThread.
                uiOperate(WHAT_SWITCH_TO_DEFAULT_CAMERA);

                mUpdateUiHandler.sendMessage(mUpdateUiHandler.obtainMessage
                        (WHAT_UPDATE_TEXT_TIPS, true));

                while (mRepeatTimes > 0) {

                    waitForPreview();

                    mRepeatTimes--;

                    mUpdateUiHandler.sendMessage(mUpdateUiHandler.obtainMessage
                            (WHAT_UPDATE_TEXT_TIMES, mTotalTimes, mRepeatTimes));

                    switch (mViewResId) {

                        case R.id.lock_screen:

                            CameraActivity cameraActivity = (CameraActivity) getActivity();
                            if (cameraActivity == null) return;
                            
                            cameraActivity.lockScreenByThread();
                            Thread.sleep(SLEEP_SCREEN_TIME);
                            cameraActivity.wakeupScreenByThread();

                            /* This is old way to wake up screen.
                            cameraActivity.lockAndWakeupScreen();*/

                            // Wait for fragment to be onStop state, so isPreview() will be
                            // false, the thread will wait until the camera to bo preview
                            Thread.sleep(CAMERA_OPERATION_TIME);
                            waitForPreview();

                            Thread.sleep(OPERATION_PREVIEW_TIME_INTERVAL);

                            uiOperate(WHAT_SWITCH_CAMERA);

                            break;
                        case R.id.switch_camera:
                            uiOperate(WHAT_SWITCH_CAMERA);
                            uiOperate(WHAT_SWITCH_CAMERA);
                            break;
                        case R.id.switch_resolution:
                            final int imageSizesLength = getImageSizesLength();
                            for (int i = 0; i < imageSizesLength; i++) {
                                uiOperate(WHAT_SWITCH_RESOLUTION);
                            }
                            break;
                        case R.id.capture_hdr:
                            uiOperate(WHAT_CAPTURE_HDR);
                            uiOperate(WHAT_SWITCH_CAMERA);
                            uiOperate(WHAT_CAPTURE_HDR);
                            if (mRepeatTimes > 0) uiOperate(WHAT_SWITCH_CAMERA);
                            break;
                    }
                }

                startNextThread(mTotalTimes);

            } catch (InterruptedException e) {
                LogUtil.i(IConstValue.TAG, "interrupt the SLRCThread!");
            } finally {
                mUpdateUiHandler.sendMessage(mUpdateUiHandler.obtainMessage(WHAT_UPDATE_TEXT_NONE));
            }
        }

        int getViewResId() {
            return mViewResId;
        }

        private void waitForPreview() throws InterruptedException {
            for (int i = 0; i < PAUSE_TIMES; i++) {
                if (!isPreview()) Thread.sleep(PAUSE_TIME_UNIT);
                else break;
            }
            if (!isPreview() && getActivity() != null) getActivity().finish();
        }

        private void startNextThread(int defaultTimes) throws InterruptedException {
            waitForPreview();

            // If there has next operate, then begin it.
            if (mRepeatTimes == 0 && isPreview()) {
                final int viewResId = mViewResId;
                // Update current function button state and color.
                mUpdateUiHandler.sendMessage(mUpdateUiHandler.obtainMessage
                        (WHAT_UPDATE_VIEW_COLOR, viewResId, FunctionView.STATE_SUCCESS));

                int index = FunctionView.VIEW_FUNCTIONS.length;
                for (int i = 0; i < index; i++) {
                    if (mViewResId == FunctionView.VIEW_FUNCTIONS[i]) {
                        for (int j = i + 1; j < index; j++) {
                            if (FunctionView.getViewState(FunctionView.VIEW_FUNCTIONS[j]) !=
                                    FunctionView.STATE_SUCCESS) {
                                index = j;
                            }
                        }
                        break;
                    }
                }

                if (index < FunctionView.VIEW_FUNCTIONS.length) {
                    new SLRCThread(FunctionView.VIEW_FUNCTIONS[index], defaultTimes).start();
                } else {
                    mUpdateUiHandler.sendMessage(mUpdateUiHandler.obtainMessage(WHAT_ENABLE_VIEW));

                    sSLRCThread = null;
                }

            }
        }

        /**
         * Do Camera operate
         *
         * @param operateWhat {@link #WHAT_SWITCH_CAMERA},
         *                    {@link #WHAT_SWITCH_RESOLUTION},
         *                    {@link #WHAT_SWITCH_TO_DEFAULT_CAMERA},
         *                    {@link #WHAT_CAPTURE_HDR}
         */
        private void uiOperate(int operateWhat) throws InterruptedException {

            waitForPreview();

            if (getActivity() == null) {
                LogUtil.e(IConstValue.TAG, "SLRCFragment getActivity() is null!");
                return;
            }

            mUpdateUiHandler.sendMessage(mUpdateUiHandler.obtainMessage(operateWhat));

            Thread.sleep(CAMERA_OPERATION_TIME);

            if (operateWhat == WHAT_CAPTURE_HDR) waitForPreview();

            Thread.sleep(OPERATION_PREVIEW_TIME_INTERVAL);

        }
    }
}
