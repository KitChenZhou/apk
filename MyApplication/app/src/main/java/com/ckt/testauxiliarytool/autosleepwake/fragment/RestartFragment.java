package com.ckt.testauxiliarytool.autosleepwake.fragment;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.autosleepwake.dialog.ErrorSdkVersionDialogFragment;
import com.ckt.testauxiliarytool.autosleepwake.dialog.InputErrorDialogFragment;
import com.ckt.testauxiliarytool.autosleepwake.dialog.RestartIsNotUseDialog;
import com.ckt.testauxiliarytool.autosleepwake.interfaces.Constants;
import com.ckt.testauxiliarytool.autosleepwake.receiver.AdminReceiver;
import com.ckt.testauxiliarytool.autosleepwake.service.RestartIntentService;
import com.ckt.testauxiliarytool.utils.CacheUtil;
import com.ckt.testauxiliarytool.utils.TestUtil;

public class RestartFragment extends BaseFragment {
    public static final String TAG = RestartFragment.class.getSimpleName();

    private EditText mCountEdt, mTimeEdt;
    private Button mStartBtn;
    private Button mStopBtn;

    @Override
    protected void initData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    protected void initListener() {
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager packageManager = getActivity().getPackageManager();
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), PackageManager.GET_ACTIVITIES);
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)  {
                        //Toast.makeText(getActivity(), "系统应用", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "系统应用");
                        goToTest();
                    } else {
                       // Toast.makeText(getActivity(), "普通应用", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "三方应用");
                        TestUtil.showDialog(new RestartIsNotUseDialog(), getFragmentManager(), "RestartIsNotUseDialog");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartBtn.setEnabled(true);
                RestartIntentService.stopService(getActivity());
                mStopBtn.setEnabled(false);
            }
        });
    }

    private void prepareRestart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (TestUtil.isAdminActive(getActivity())) {
                goToTest();
            } else {
                goToActive();
            }
        } else {
            TestUtil.showDialog(new ErrorSdkVersionDialogFragment(),
                    getFragmentManager(), "ErrorSdkVersionDialogFragment");
        }
    }

    /**
     * 跳转至设备管理器激活页面
     */
    private void goToActive() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(getActivity(), AdminReceiver.class));
        startActivity(intent);
    }

    private void goToTest() {
        int testTime = getTestTime();
        int testCount = getTestCount();
        // 测试时间应该继续进行判断：不能太短
        if (testTime != 0 && testCount != 0) {
            //将测试间隔和测试次数写入SharedPreferences
            CacheUtil.putInt(getActivity(), Constants.TEST_TIME, testTime);
            CacheUtil.putInt(getActivity(), Constants.TEST_COUNT, testCount);
            //开启后台服务，进行测试任务
            RestartIntentService.newIntent(getActivity());
            //让按钮不可用
            mStartBtn.setEnabled(false);
            mStopBtn.setEnabled(true);
        } else {
            TestUtil.showDialog(new InputErrorDialogFragment(), getFragmentManager(), "InputErrorDialogFragment");
        }
    }

    private int getTestCount() {
        String count = mCountEdt.getText().toString().trim();
        if (TextUtils.isEmpty(count)) {
            return 0;
        }
        return Integer.parseInt(count);
    }

    private int getTestTime() {
        String time = mTimeEdt.getText().toString().trim();
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        return Integer.parseInt(time);
    }


    @Override
    protected void initViews(View rootView) {
        mCountEdt = (EditText) rootView.findViewById(R.id.edt_count);
        mTimeEdt = (EditText) rootView.findViewById(R.id.edt_time);
        mStartBtn = (Button) rootView.findViewById(R.id.btn_start);
        mStopBtn = (Button) rootView.findViewById(R.id.btn_stop);
        mStopBtn.setEnabled(false);


        //初始化界面时，如果当前的测试任务没有完成，则让按钮不可用
        if (CacheUtil.getInt(getActivity(), Constants.TEST_COUNT, 0) != 0) {
            mStartBtn.setEnabled(false);
            mStopBtn.setEnabled(true);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.asw_fragment_restart;
    }
}
