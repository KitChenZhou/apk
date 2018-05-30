package com.ckt.testauxiliarytool.cameratest.capturetask;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.capturetask.api.CameraApiImpl2;
import com.ckt.testauxiliarytool.cameratest.capturetask.api.ICameraApi;
import com.ckt.testauxiliarytool.cameratest.capturetask.task.CaptureFrontBackCam;
import com.ckt.testauxiliarytool.cameratest.capturetask.task.CaptureInDarkBright;
import com.ckt.testauxiliarytool.cameratest.capturetask.task.CaptureRecordSwitcher;
import com.ckt.testauxiliarytool.cameratest.capturetask.task.CaptureTask;
import com.ckt.testauxiliarytool.cameratest.capturetask.task.CaptureTaskHandler;
import com.ckt.testauxiliarytool.cameratest.capturetask.task.CaptureTaskUIModifier;
import com.ckt.testauxiliarytool.cameratest.slrc.CameraActivity;
import com.ckt.testauxiliarytool.cameratest.slrc.model.FunctionView;
import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.cameratest.common.SoundPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by asahi on 2017/11/23.
 */

public class CaptureTaskFragment extends Fragment implements View.OnClickListener, CaptureTaskUIModifier {

    public static final String TAG = "CaptureTaskFragment";

    /**
     * A RadioGroup to show Where the page is.
     */
    private RadioGroup mRadioGroup;

    private Button mCFBCBtn, mCIDBBtn, mCRSBtn;
    private List<Button> mButtons;

    private TextView mCaptureTaskTip;
    private String mCaptureTaskTipInfo;

    private AutoFitTextureView mTextureView;

    private CaptureTask mCaptureTask;
    private CaptureTaskHandler mTaskHandler;

    private Resources.Theme theme;

    private int taskOnColor, taskCompleteColor;
    private Drawable taskNotStartedColor;

    private CameraActivity mCameraActivity;

    public static CaptureTaskFragment newInstance() {
        return new CaptureTaskFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ct_capture_task_fragment,
                container, false);
        return view;
    }

    // override this method to avoid null pointer exception
    public void onSwitchRadioButtonChecked(String checked) {
        if ("start".equals(checked)) {
            mRadioGroup.check(R.id.ct_capture_task_rb_start);
        } else {
            mRadioGroup.check(R.id.ct_capture_task_rb_main);
        }
    }

    /**
     * 初始化控件，并为Button设置点击监听事件。
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mRadioGroup = (RadioGroup) view.findViewById(R.id.ct_capture_task_radio_group);

        mCFBCBtn = (Button) view.findViewById(R.id.ct_start_task_cfbc);
        mCFBCBtn.setOnClickListener(this);

        mCIDBBtn = (Button) view.findViewById(R.id.ct_start_task_cidb);
        mCIDBBtn.setOnClickListener(this);

        mCRSBtn = (Button) view.findViewById(R.id.ct_start_task_crs);
        mCRSBtn.setOnClickListener(this);

        mCaptureTaskTip = (TextView) view.findViewById(R.id.ct_capture_fragment_tip);
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.ct_capture_fragment_texture_view);


        theme = getActivity().getTheme();

        mCameraActivity = (CameraActivity) getActivity();

        taskOnColor = getResources().getColor(R.color.ct_task_on);
        taskNotStartedColor = getResources().getDrawable(R.drawable.ct_button_style, theme);
        taskCompleteColor = getResources().getColor(R.color.ct_color_red);

        mButtons = new ArrayList<>(Arrays.asList(null, mCFBCBtn, mCIDBBtn, mCRSBtn));
    }


    /**
     * reset all conditions
     */
    public void reset() {

        if (mCaptureTask != null && mCaptureTask.isAlive()) {
            mCaptureTask.reset();
            mCaptureTask = null;
        }

        //重置参数
        //resetParameters();

        if (mCamApi != null) {
            mCamApi.closeCamera();
            mCamApi = null;
        }

        //Log.e(TAG, "reset executed!"
                //+ Params.get(ConstVar.CAPTURE_TASK_STATUS, ConstVar.CAPTURE_TASK_NONE));
        restartActivity();
        resetParameters();
    }

    /**
     * reset parameters
     */
    public static void resetParameters() {

        Params.set(ConstVar.CAPTURE_TASK_CURCNT_CFBS, ConstVar.CAPTURE_TASK_CURCNT_DEFAULT);
        Params.set(ConstVar.CAPTURE_TASK_CURCNT_CIDB, ConstVar.CAPTURE_TASK_CURCNT_DEFAULT);
        Params.set(ConstVar.CAPTURE_TASK_CURCNT_CRS, ConstVar.CAPTURE_TASK_CURCNT_DEFAULT);

        Params.set(ConstVar.CAPTURE_TASK_STATUS, ConstVar.CAPTURE_TASK_NONE);
    }

    private ICameraApi mCamApi;

    /**
     * 在onResume中开启摄像头预览;
     * 如果有开启的项目，则继续执行该项目。
     */
    @Override
    public void onResume() {
        super.onResume();
        setButtonsClickable(false);
        mTaskHandler = new CaptureTaskHandler(this);
        try {
            // 初始化mCamApi这个摄像头工具类
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCamApi = new CameraApiImpl2(getActivity(), mTextureView);
                mCamApi.startBackgroundThread();
                mCamApi.initTextureView();
                mCamApi.setHandler(mTaskHandler);
            } else {
                // mCamApi = new CameraApiImpl(); // not being initialized.
                Toast.makeText(mCameraActivity, "暂时只支持Android 5.0及以上系统", Toast.LENGTH_SHORT).show();
                return;
            }
            /**
             * 由于存在重启Fragment的行为或者是意外退出、此时操作不能停，
             * 所以需要在这个方法中重新开启相应的线程。
             */

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setButtonsClickable(true);
                }
            }, 3000);

            int index = Params.get(ConstVar.CAPTURE_TASK_STATUS,
                    ConstVar.CAPTURE_TASK_NONE);
            if (index != ConstVar.CAPTURE_TASK_NONE) {
                runTransaction(index);
            }
        } catch (Exception e) {
        }
    }

    private void setButtonsClickable(boolean isClickable){
        for (Button btn : mButtons){
            if (btn != null)
                btn.setClickable(isClickable);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mCaptureTask != null)
            mCaptureTask.onPause();
        if (mCamApi != null) {
            mCamApi.closeCamera();
            mCamApi.stopBackgroundThread();
            mCamApi = null;
        }

        SoundPlayer.release();
    }


    /**
     * 对于重启相机、重新启动父Activity存在一些问题，并且与重启Fragment的效果相似
     * 因此在这里选择重启本Fragment。
     */
    public void restartActivity() {
        if (mCamApi != null) {
            mCamApi.closeCamera();
            mCamApi.stopBackgroundThread();
        }
        mCameraActivity.replaceFragment(CaptureTaskFragment.newInstance());
    }

    public void runTransaction(int index) {
        String taskName;
        switch (index) {
            case ConstVar.CAPTURE_TASK_CFBC: {
                //mCurBtn = mCFBCBtn;
                taskName = getResources().getString(R.string.ct_capture_task_cfbc_name);
                //mTaskHandler.sendMessage(MessageCreator.create(ConstVar.MSG_REQ_01_TASK_BEGIN));
                mCaptureTask = new CaptureFrontBackCam(index, taskName, mCamApi, mTaskHandler);
                mCaptureTask.start();
                break;
            }
            case ConstVar.CAPTURE_TASK_CIDB: {
                //mCurBtn = mCIDBBtn;
                taskName = getResources().getString(R.string.ct_capture_task_cidb_name);
                //mTaskHandler.sendMessage(MessageCreator.create(ConstVar.MSG_REQ_02_TASK_BEGIN));
                mCaptureTask = new CaptureInDarkBright(index, taskName, mCamApi, mTaskHandler);
                mCaptureTask.start();
                break;
            }
            case ConstVar.CAPTURE_TASK_CRS: {
                //mCurBtn = mCRSBtn;
                taskName = getResources().getString(R.string.ct_capture_task_crs_name);
                //mTaskHandler.sendMessage(MessageCreator.create(ConstVar.MSG_REQ_03_TASK_BEGIN));
                mCaptureTask = new CaptureRecordSwitcher(index, taskName, mCamApi, mTaskHandler);
                mCaptureTask.start();
                break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        /**
         * 三个Button，在一个被点击之后，其他的button在该任务执行完毕之前将不能被点击。
         */
        if (isTaskRunning()) {
            return;
        }
        // initialize taskId
        int taskId = ConstVar.CAPTURE_TASK_NONE;

        switch (view.getId()) {
            case R.id.ct_start_task_cfbc: {
                taskId = ConstVar.CAPTURE_TASK_CFBC;
                break;
            }
            case R.id.ct_start_task_cidb: {
                taskId = ConstVar.CAPTURE_TASK_CIDB;
                break;
            }
            case R.id.ct_start_task_crs: {
                taskId = ConstVar.CAPTURE_TASK_CRS;
                break;
            }
        }
        runTransaction(taskId);
        Params.set(ConstVar.CAPTURE_TASK_STATUS, taskId);
    }

    private boolean isTaskRunning() {
        return Params.get(ConstVar.CAPTURE_TASK_STATUS, ConstVar.CAPTURE_TASK_NONE)
                != ConstVar.CAPTURE_TASK_NONE;
    }

    @Override
    public void switchCamera() {
        if (mCamApi != null) {
            mCamApi.switchCamera();
            changeTip("");//清除tip信息。
        }
    }

    @Override
    public void restartFragment() {
        restartActivity();
    }

    @Override
    public void markTaskBtnStart(int index) {

        Button btn = mButtons.get(index);
        if (btn != null) {
            btn.setText(R.string.ct_capture_task_task_on);
            btn.setClickable(false);
            btn.setBackgroundColor(taskOnColor);
        } else {
            //Log.e(TAG, "index = " + index);
        }
        FunctionView.enabledView(false);
    }

    @Override
    public void markTaskComplete(int index) {
        Button btn = mButtons.get(index);
        if (btn != null) {
            btn.setText(R.string.ct_capture_task_task_complete);
            btn.setBackgroundColor(taskCompleteColor);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void markTaskNotStart(int index) {
        Button btn = mButtons.get(index);
        if (btn != null) {
            btn.setText(R.string.ct_capture_task_cfbc_name);
            btn.setBackground(taskNotStartedColor);
            btn.setClickable(true);
        }
        handleColor();
    }

    /**
     * handle the left-side icon color.
     */
    private void handleColor() {
        FunctionView.enabledView(true);
        //FunctionView.refreshView();
        FunctionView.setImageViewState(R.id.ct_catpure_task_fragment, FunctionView.STATE_SUCCESS);
        //FunctionView.getAllViewStates(getContext().getApplicationContext());
        FunctionView.updateViewColor();
    }

    @Override
    public void initCRSCamera() {
        if (mCamApi != null)
            mCamApi.initTextureView();
    }

    /**
     * 修改上方的提示栏
     *
     * @param text
     */
    public void changeTip(String text) {
        mCaptureTaskTipInfo = text;
        if (mCaptureTaskTip != null) {
            mCaptureTaskTip.setText(mCaptureTaskTipInfo);
        }
    }
}
