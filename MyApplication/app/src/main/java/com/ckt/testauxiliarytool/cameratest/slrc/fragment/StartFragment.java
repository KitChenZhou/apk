package com.ckt.testauxiliarytool.cameratest.slrc.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.capturetask.CaptureTaskFragment;
import com.ckt.testauxiliarytool.cameratest.capturetask.Params;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.cameratest.fb.LowPowerTakePhoto;
import com.ckt.testauxiliarytool.cameratest.fb.StartCameraActivity;
import com.ckt.testauxiliarytool.cameratest.fb.Tool;
import com.ckt.testauxiliarytool.cameratest.fb.UnusuallyExitCamera;
import com.ckt.testauxiliarytool.cameratest.slrc.CameraActivity;
import com.ckt.testauxiliarytool.cameratest.slrc.model.FunctionView;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;


/**
 * Created by Cc on 2017/8/17.
 */

public class StartFragment extends BaseFragment implements View.OnClickListener {

    /**
     * To input repeat times
     */
    private EditText mRepeatTimes;

    private TextView mEditTextEmpty;

    private FunctionView mFbActivity1, mFbActivity2, mFbActivity3, mCaptureTask;

    public static StartFragment newInstance() {
        return new StartFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        //mMyImageViewSet.updateFBImageView(mFbActivity1, mFbActivity2, mFbActivity3);
        checkCaptureTaskState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        FunctionView.saveAllViewStates(getContext());
        FunctionView.cleanAllImageView();
    }

    @Override
    int setLayoutResId() {
        return R.layout.ct_start_fragment;
    }

    @Override
    void initView(View view) {
        mRepeatTimes = (EditText) view.findViewById(R.id.repeat_times);
        mEditTextEmpty = (TextView) view.findViewById(R.id.tv_edit_empty);
        view.findViewById(R.id.reset).setOnClickListener(this);
        view.findViewById(R.id.switch_camera).setOnClickListener(this);
        view.findViewById(R.id.lock_screen).setOnClickListener(this);
        view.findViewById(R.id.switch_resolution).setOnClickListener(this);
        view.findViewById(R.id.capture_hdr).setOnClickListener(this);

        mCaptureTask = (FunctionView) view.findViewById(R.id.ct_catpure_task_fragment);
        mFbActivity1 = (FunctionView) view.findViewById(R.id.fb_activity_1);
        mFbActivity2 = (FunctionView) view.findViewById(R.id.fb_activity_2);
        mFbActivity3 = (FunctionView) view.findViewById(R.id.fb_activity_3);
    }

    @Override
    void setListener() {
        mCaptureTask.setOnClickListener(this);
        mFbActivity1.setOnClickListener(this);
        mFbActivity2.setOnClickListener(this);
        mFbActivity3.setOnClickListener(this);
        mRepeatTimes.setOnClickListener(this);
        if (mRepeatTimes.isCursorVisible()) mRepeatTimes.setCursorVisible(false);

        //监听编辑框里面文本的变化
        mRepeatTimes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.v("EditTextChanged", s.toString());
                int times = 0;
                try {
                    if (s.toString().equals("")) {
                        times = ConstVar.CAPTURE_TASK_TOTCNT_DEFAULT;

                        mRepeatTimes.setBackgroundResource(R.drawable.ct_edittext_wrong);
                        mEditTextEmpty.setText(R.string.ct_text_cannot_null);
                    } else {
                        times = Integer.parseInt(s.toString());

                        if (times == 0) {
                            mRepeatTimes.setBackgroundResource(R.drawable.ct_edittext_wrong);
                            mEditTextEmpty.setText(R.string.ct_text_cannot_null);
                        } else {
                            mRepeatTimes.setBackgroundResource(R.drawable.ct_edittext_normal);
                            mEditTextEmpty.setText("");
                        }
                    }

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    times = ConstVar.CAPTURE_TASK_TOTCNT_DEFAULT;
                } finally {
                    Params.set(ConstVar.CAPTURE_TASK_TOTCNT, times);
                    SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, getContext()).putInt
                            (IConstValue.KEY_REPEAT_TIMES, times);
                }
            }
        });
    }

    @Override
    void initData() {

        mRepeatTimes.setText(String.valueOf(SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE,
                getContext()).getInt(IConstValue.KEY_REPEAT_TIMES, IConstValue
                .VALUE_REPEAT_TIMES_DEFAULT)));

        FunctionView.getAllViewStates(getContext());
        FunctionView.enabledView(true);
        FunctionView.updateViewColor();
    }

    @Override
    void recycleObject() {

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.repeat_times) {
            if (!mRepeatTimes.isCursorVisible()) mRepeatTimes.setCursorVisible(true);
            return;
        }

        if (mRepeatTimes.isCursorVisible()) mRepeatTimes.setCursorVisible(false);

        if (v.getId() == R.id.reset) {

            SLRCFragment.stopSLRCThread();

            FunctionView.refreshView();

            Tool.clearFBRepeatTimes(getContext());

            resetCaptureTask();
            return;
        }

        if (mRepeatTimes.getText().toString().equals("")) return;
        int repeatTimes = Integer.parseInt(mRepeatTimes.getText().toString());
        if (repeatTimes == 0) return;

        closeKeyboard();

        FunctionView.enabledView(false);

        CameraActivity activity = (CameraActivity) getActivity();

        switch (v.getId()) {
            case R.id.switch_camera:
                activity.startSLRCThread(R.id.switch_camera, repeatTimes);
                break;
            case R.id.lock_screen:
                activity.startSLRCThread(R.id.lock_screen, repeatTimes);
                break;
            case R.id.switch_resolution:
                activity.startSLRCThread(R.id.switch_resolution, repeatTimes);
                break;
            case R.id.capture_hdr:
                activity.startSLRCThread(R.id.capture_hdr, repeatTimes);
                break;
            case R.id.fb_activity_1:
                //启动相机
                Intent mStartCameraIntent = new Intent(getActivity(), StartCameraActivity.class);
                startActivity(mStartCameraIntent);
                setRebootTimes(repeatTimes, -1);
                getActivity().finish();
                break;
            case R.id.fb_activity_2:
                //非常规操作退出相机，正常进入相机
                Intent mUnusuallyExitCameraIntent = new Intent(getActivity(), UnusuallyExitCamera
                        .class);
                startActivity(mUnusuallyExitCameraIntent);
                setRebootTimes(-1, repeatTimes);
                getActivity().finish();
                break;
            case R.id.fb_activity_3:
                //低电，开启闪光灯拍照
                Intent mLowPowerIntent = new Intent(getActivity(), LowPowerTakePhoto.class);
                startActivityForResult(mLowPowerIntent, 0x13);
                break;
            case R.id.ct_catpure_task_fragment:
                int total = Integer.parseInt(mRepeatTimes.getText().toString());
                Params.set(ConstVar.CAPTURE_TASK_TOTCNT, total);
                ((CameraActivity) getActivity()).replaceFragment(CaptureTaskFragment.newInstance());
                break;
            default:
                break;
        }
    }

    private void closeKeyboard() {
        View view = getActivity().getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && inputMethodManager.isActive())
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * yy's reset
     */
    private static final String sCaptureTaskTag = CaptureTaskFragment.TAG;

    private CaptureTaskFragment mCaptureTaskFragment;

    /**
     * Reset ct_capture task's state by calling CaptureTaskFragment.reset().
     */
    private void resetCaptureTask() {
        mCaptureTaskFragment = (CaptureTaskFragment)
                getActivity().getSupportFragmentManager().findFragmentByTag(sCaptureTaskTag);

        if (mCaptureTaskFragment != null) {
            mCaptureTaskFragment.reset();
            mCaptureTaskFragment = null;
        } else {
            Log.v(sCaptureTaskTag, "CaptureTaskFragment is null");
            CaptureTaskFragment.resetParameters();
        }
    }

    /**
     * check yy's task
     */
    private boolean isCaptureFragmentPresenting() {
        return getFragmentManager().findFragmentByTag(sCaptureTaskTag) != null;
    }

    /**
     * set background color of yy's image button to red when task not finished.
     */
    private void checkCaptureTaskState() {
        if (!isCaptureFragmentPresenting()) {//说明此时yy的Fragment没有显示
            if (Params.get(ConstVar.CAPTURE_TASK_STATUS, ConstVar.CAPTURE_TASK_NONE) != ConstVar
                    .CAPTURE_TASK_NONE) {//并且还有任务在执行。
                mCaptureTask.setBackgroundColor(Color.RED);
            }
        }
    }

    /**
     * set reboot times
     *
     * @param startCount
     * @param startNum
     */
    public void setRebootTimes(int startCount, int startNum) {
        SharedPreferences startBootPreferences = getActivity().getApplicationContext()
                .getSharedPreferences(ConstVar.BOOT_START, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = startBootPreferences.edit();
        editor.putInt(ConstVar.START_COUNT, startCount);
        editor.putInt(ConstVar.START_NUM, startNum);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x13) {
            FunctionView.getAllViewStates(getContext());
            FunctionView.enabledView(true);
            FunctionView.updateViewColor();
        }
    }
}
