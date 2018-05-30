package com.ckt.testauxiliarytool.autosleepwake.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.autosleepwake.ASWMainActivity;
import com.ckt.testauxiliarytool.autosleepwake.ResultActivity;
import com.ckt.testauxiliarytool.autosleepwake.dialog.DeleteLogDialogFragment;
import com.ckt.testauxiliarytool.autosleepwake.dialog.InputErrorDialogFragment;
import com.ckt.testauxiliarytool.autosleepwake.dialog.NotLogShowDialogFragment;
import com.ckt.testauxiliarytool.autosleepwake.dialog.ScreenLockMethodDialogFragment;
import com.ckt.testauxiliarytool.autosleepwake.dialog.SleepSettingDialogFragment;
import com.ckt.testauxiliarytool.autosleepwake.interfaces.Constants;
import com.ckt.testauxiliarytool.autosleepwake.receiver.AdminReceiver;
import com.ckt.testauxiliarytool.autosleepwake.service.SleepWakeService;
import com.ckt.testauxiliarytool.utils.FileUtils;
import com.ckt.testauxiliarytool.utils.PermissionUtils;
import com.ckt.testauxiliarytool.utils.TestUtil;

import java.io.File;
import java.util.List;

public class SleepWakeFragment extends BaseFragment implements View.OnClickListener, SleepWakeService.OnTestTaskListener {
    private EditText mCountEdt, mTimeEdt;
    private Button mStartBtn, mStopBtn, mShowResultBtn, mClearLogBtn;
    // 读写外部存储的权限请求码
    private final int RW_PERMISSION_REQUEST_CODE = 0;

    @Override
    protected void initData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    protected void initListener() {
        mStartBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
        mShowResultBtn.setOnClickListener(this);
        mClearLogBtn.setOnClickListener(this);
    }

    /**
     * 弹出设置忽略电池优化的白名单，Doze模式
     */
    private void setIgnoreBatteryOptimization() {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        startActivity(intent);
    }

    // 跳转至设备管理器激活界面
    private void gotToActive() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(getActivity(), AdminReceiver.class));
        startActivity(intent);
    }

    // 执行测试
    private void goToTest() {
        int testCount = getTestCount();
        int testTime = getTestTime();
        if (testCount == 0 || testTime == 0) {
            TestUtil.showDialog(new InputErrorDialogFragment(), getFragmentManager(), "InputErrorDialogFragment");
        } else {
            if (checkTime(testTime)) {
                // 开启后台测试任务
                SleepWakeService.start(getActivity(), testTime, testCount);
                SleepWakeService.setOnTaskTestListener(this); // 设置回调
            }
        }
    }

    /**
     * 校验手机的屏幕超时时间和用户输入的时间，如果屏幕超时时间太短，可能导致锁屏Bug
     *
     * @param testTime 用户输入的测试时间间隔
     * @return false表示当前手机的屏幕超时时间小于等于用户输入的测试时间，true则大于
     */
    private boolean checkTime(int testTime) {
        int phoneSleepTime = 0;
        try {
            phoneSleepTime = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (phoneSleepTime <= testTime * 1000) { // 给用户输入的是秒
            TestUtil.showDialog(new SleepSettingDialogFragment(), getFragmentManager(), "SleepSettingDialogFragment");
            return false;
        }
        return true;
    }

    /**
     * 从编辑框获取测试的时间间隔
     *
     * @return 如果用户输入为空，则返回0；否则返回解析后的时间间隔
     */
    private int getTestTime() {
        String time = mTimeEdt.getText().toString().trim();
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        return Integer.parseInt(time);
    }

    /**
     * 从编辑框获取测试次数
     *
     * @return 如果用户输入为空，则返回0；否则返回解析后的测试次数
     */
    private int getTestCount() {
        String count = mCountEdt.getText().toString().trim();
        if (TextUtils.isEmpty(count)) {
            return 0;
        }
        return Integer.parseInt(count);
    }

    @Override
    protected void initViews(View rootView) {
        mCountEdt = (EditText) rootView.findViewById(R.id.edt_count);
        mTimeEdt = (EditText) rootView.findViewById(R.id.edt_time);
        mStartBtn = (Button) rootView.findViewById(R.id.btn_start);
        mStopBtn = (Button) rootView.findViewById(R.id.btn_stop);
        mShowResultBtn = (Button) rootView.findViewById(R.id.btn_show_result);
        mClearLogBtn = (Button) rootView.findViewById(R.id.btn_clear_log);
        TestUtil.showDialog(new ScreenLockMethodDialogFragment(), getFragmentManager(), "ScreenLockMethodDialogFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SleepWakeService.getRunningStatus()) {
            mStartBtn.setEnabled(false);
            mStopBtn.setEnabled(true);
            getActivity().setTitle("休眠唤醒测试   " + SleepWakeService.sFinishCount);
        } else {
            mStartBtn.setEnabled(true);
            mStopBtn.setEnabled(false);
            getActivity().setTitle("休眠唤醒测试");
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.asw_fragment_sleep_wake;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                start();
                break;
            case R.id.btn_stop:
                SleepWakeService.stop(getActivity());
                break;
            case R.id.btn_show_result:
                showResultActivity();
                break;
            case R.id.btn_clear_log:
                clearAllLog();
                break;
        }
    }

    private void clearAllLog() {
        // 创建对话框，让让用户确认再删除文件
        TestUtil.showDialog(new DeleteLogDialogFragment(), getFragmentManager(), "DeleteLogDialogFragment");
    }

    private void showResultActivity() {
        // 弹对话框，如果当前目录下没有日志，则告诉用户跑测试
        List<File> files = FileUtils.listFilesInDirWithFilter(Constants.LOG_DIR, ".log");
        if (files == null || files.isEmpty()) {
            TestUtil.showDialog(new NotLogShowDialogFragment(), getFragmentManager(), "NotLogShowDialogFragment");
        } else {
            Intent intent = new Intent(getActivity(), ResultActivity.class);
            startActivity(intent);
        }
    }

    private void start() {
        PermissionUtils.requestPermissions(getActivity(), RW_PERMISSION_REQUEST_CODE,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionUtils.OnPermissionListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onPermissionGranted() {
                        PowerManager powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
                        // 判断是否在电池优化白名单
                        if (powerManager.isIgnoringBatteryOptimizations(getActivity().getPackageName())) {
                            // 判断是否激活设备管理器，激活了则去测试，否则去激活
                            if (TestUtil.isAdminActive(getActivity())) {
                                goToTest();
                            } else {
                                gotToActive();
                            }
                        } else {
                            setIgnoreBatteryOptimization();
                        }
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                        Toast.makeText(getActivity(), "读写文件权限被拒绝", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onTaskRunning() {
        mStartBtn.setEnabled(false);
        mStopBtn.setEnabled(true);
    }

    @Override
    public void onTaskStop() {
        mStartBtn.setEnabled(true);
        mStopBtn.setEnabled(false);
        ASWMainActivity mainActivity = (ASWMainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setTitle("休眠唤醒测试");
        }
    }
}
