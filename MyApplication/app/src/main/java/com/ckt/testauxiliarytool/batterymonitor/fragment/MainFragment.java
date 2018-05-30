package com.ckt.testauxiliarytool.batterymonitor.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.batterymonitor.bean.BatteryInfo;
import com.ckt.testauxiliarytool.batterymonitor.db.InfoDaoImpl;
import com.ckt.testauxiliarytool.batterymonitor.dialog.ClearDataDialog;
import com.ckt.testauxiliarytool.batterymonitor.dialog.NoDataShowDialog;
import com.ckt.testauxiliarytool.batterymonitor.dialog.NotDataShowDialog;
import com.ckt.testauxiliarytool.batterymonitor.service.BatteryMonitorService;
import com.ckt.testauxiliarytool.utils.DateTimeUtils;
import com.ckt.testauxiliarytool.utils.DialogUtils;
import com.ckt.testauxiliarytool.utils.ExcelUtils;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.PermissionUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 主页Fragment
 */
public class MainFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = MainFragment.class.getSimpleName();
    private Button btn_start_monitor, btn_stop_monitor, btn_look_data, btn_export_excel, btn_look_chart, btn_clear_data;
    private UIHandler mUIHandler; // 更新UI的Handler
    private final int MSG_PROGRESS = 0X123; // 让对话框消失的消息码
    private ProgressDialog mProgressDialog; // 进度对话框
    protected static long mLastShowTime;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.bm_fragment_main;
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void findViews(View rootView) {
        btn_start_monitor = (Button) rootView.findViewById(R.id.btn_start_monitor);
        btn_stop_monitor = (Button) rootView.findViewById(R.id.btn_stop_monitor);
        btn_look_data = (Button) rootView.findViewById(R.id.btn_look_data);
        btn_export_excel = (Button) rootView.findViewById(R.id.btn_export_excel);
        btn_look_chart = (Button) rootView.findViewById(R.id.btn_look_chart);
        btn_clear_data = (Button) rootView.findViewById(R.id.btn_clear_data);
    }

    @Override
    protected void setListeners() {
        btn_start_monitor.setOnClickListener(this);
        btn_stop_monitor.setOnClickListener(this);
        btn_look_data.setOnClickListener(this);
        btn_export_excel.setOnClickListener(this);
        btn_look_chart.setOnClickListener(this);
        btn_clear_data.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_monitor:
                BatteryMonitorService.start(getActivity()); // 开启监测服务，开始接收广播
                break;
            case R.id.btn_stop_monitor:
                BatteryMonitorService.stop(getActivity()); // 停止监测服务
                break;
            case R.id.btn_look_data: // 显示历史记录的Fragment
                showFragment(new BatteryInfoListFragment(), null);
                break;
            case R.id.btn_export_excel:
                exportExcel(); // 导出表格
                break;
            case R.id.btn_look_chart: // 显示曲线图的Fragment
                showFragment(new ChartFragment(), null);
                break;
            case R.id.btn_clear_data: // 清空数据
                clearAllData();
                break;
        }
    }

    /**
     * 清除数据
     */
    private void clearAllData() {
        if (BatteryMonitorService.getRunningStatus()) {
            Toast.makeText(getActivity(), "正在监控中，请停止后再试!", Toast.LENGTH_LONG).show();
        } else {   // 显示清空数据的对话框
            DialogUtils.showDialog(new ClearDataDialog(), getFragmentManager(), "ClearDataDialog", false);
        }
    }

    /**
     * 显示Fragment
     *
     * @param fragment 要显示的Fragment
     * @param tag      添加到回退栈的Tag标记
     */
    private void showFragment(BaseFragment fragment, String tag) {
        if (shouldContinue()) {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(tag)
                    .replace(R.id.fl_container_main, fragment);
            fragmentTransaction.commit();
        }
    }

    /**
     * 导出表格：如果已经获取到读写外部存储的权限且数据库中
     * 存在数据，则开启导出表格子线程
     */
    private void exportExcel() {
        mUIHandler = new UIHandler(this);
        PermissionUtils.requestPermissions(getActivity(), 0, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new PermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                if (shouldContinue()) {
                    // 显示进度对话框，开启导出表格子线程
                    showProgressDialog();
                    ExportExcelThread thread = new ExportExcelThread(getResources().getStringArray(R.array.headTitles),
                            MainFragment.this);
                    thread.start();
                }
            }

            @Override
            public void onPermissionDenied(String[] deniedPermissions) {
                Toast.makeText(getActivity(), "读写外部存储权限被拒绝！", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 是否应该继续操作，如果数据库为空，则显示对应的对话框，否则执行后续操作
     *
     * @return 数据库为空返回false，否则返回true
     */
    private boolean shouldContinue() {
        List<BatteryInfo> data = InfoDaoImpl.getInstance().queryAll();
        if (data == null || data.isEmpty()) {
            if (BatteryMonitorService.getRunningStatus()) {
                DialogUtils.showDialog(new NoDataShowDialog(), getFragmentManager(), "NoDataShowDialog", false);
            } else {
                DialogUtils.showDialog(new NotDataShowDialog(), getFragmentManager(), "NotDataShowDialog", true);
            }
            return false;
        }
        return true;
    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setMessage(getString(R.string.dialog_progress_message));
        mProgressDialog.show();
        mLastShowTime = System.currentTimeMillis();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.batterymonitor);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 用于更新前台UI显示的Handler
     */
    private static class UIHandler extends Handler {
        private WeakReference<MainFragment> mMainFragmentRef;

        UIHandler(MainFragment mainFragment) {
            mMainFragmentRef = new WeakReference<>(mainFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            // 接收到消息时，让进度条对话框消失
            MainFragment mainFragment;
            if ((mainFragment = mMainFragmentRef.get()) != null && msg.what == mainFragment.MSG_PROGRESS) {
                mainFragment.mProgressDialog.dismiss();
            }
        }
    }

    /**
     * 导出表格的子线程
     */
    private static class ExportExcelThread extends Thread {
        private String[] mTitles;
        private WeakReference<MainFragment> mMainFragmentRef;

        ExportExcelThread(String[] titles, MainFragment mainFragment) {
            this.mTitles = titles;
            mMainFragmentRef = new WeakReference<>(mainFragment);
        }

        @Override
        public void run() {
            List<BatteryInfo> data = InfoDaoImpl.getInstance().queryAll();
            try {
                String excelName = DateTimeUtils.millis2String(System.currentTimeMillis()).replaceAll(" ", "_");
                ExcelUtils.write(mTitles, excelName, data);
                dismissProgressBar("导出成功!");
            } catch (Exception e) {
                dismissProgressBar("导出失败!");
                e.printStackTrace();
                LogUtil.d(TAG, "导出表格时出现异常");
            }
        }

        private void dismissProgressBar(final String msg) {
            long dt = System.currentTimeMillis() - mLastShowTime;
            // 导出完成，让进度条对话框消失
            final MainFragment mainFragment = mMainFragmentRef.get();
            SystemClock.sleep(dt < 300 ? 300 : 0);
            if (mainFragment != null) {
                mainFragment.mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mainFragment.mProgressDialog.setMessage(msg);
                    }
                });
                mainFragment.mUIHandler.sendEmptyMessageDelayed(mainFragment.MSG_PROGRESS, dt > 500 ? 0 : 500 - dt);
            }
        }
    }
}