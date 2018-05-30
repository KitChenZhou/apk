package com.ckt.testauxiliarytool.sensortest.activities;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.bean.HSensor;
import com.ckt.testauxiliarytool.sensortest.db.SensorLab;
import com.ckt.testauxiliarytool.sensortest.fragment.HallFragment;
import com.ckt.testauxiliarytool.sensortest.widget.EditDialogFragment;
import com.ckt.testauxiliarytool.sensortest.widget.PromptDialogFragment;
import com.ckt.testauxiliarytool.utils.ExcelUtils;
import com.ckt.testauxiliarytool.utils.PermissionUtils;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

import static com.ckt.testauxiliarytool.sensortest.SensorType.TYPE_HSENSOR;
import static com.ckt.testauxiliarytool.sensortest.SensorType.TYPE_HSENSOR_CALL;

public class HallTestActivity extends BaseActivity implements PromptDialogFragment.mCallBack,
        EditDialogFragment.mCallBack, View.OnClickListener {

    private static String TAG = "HallTestActivity";
    private static String ACTION_CLEAR_COVER_STATE_CHANGE = "android.intent.action.HALL_STATE";


    private static String TAG_MAX_INTERVAL_FOR_OPEN = "open";
    private static String TAG_MAX_INTERVAL_FOR_CLOSE = "close";
    public static int MaxIntervalForClose = 0;
    public static int MaxIntervalForOpen = 0;

    private int[] HALL_MODE = new int[]{TYPE_HSENSOR, TYPE_HSENSOR_CALL};
    private int mCurState = -1;
    private long mStartTime;
    private int FLAG_SAVE = 1;
    private int FLAG_DELETE = 2;
    private boolean mIsHallTest = false; //判断是否在进行Hall测试

    private Button mButtonOpen, mButtonClose;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private BroadcastReceiver mBroadcastReceiver;

    private SensorLab mSensorLab;
    private String[] titles = {"正常模式", "拨打电话模式"};
    private List<HallFragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.st_activity_hsensor);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mButtonClose = (Button) findViewById(R.id.btn_interval_close);
        mButtonOpen = (Button) findViewById(R.id.btn_interval_open);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        fragments = new ArrayList<>();
        fragments.add(HallFragment.newInstance(TYPE_HSENSOR));
        fragments.add(HallFragment.newInstance(TYPE_HSENSOR_CALL));
        mViewPager.setAdapter(new TabFragmentPagerAdapter(getSupportFragmentManager(), titles, fragments));
        mTabLayout.setupWithViewPager(mViewPager);

    }

    private void initData() {
        //获取实例化
        mSensorLab = SensorLab.get(this);
        //获取最大响应时间
        MaxIntervalForClose = SharedPrefsUtil.name("max_interval").getInt(TAG_MAX_INTERVAL_FOR_CLOSE, 0);
        MaxIntervalForOpen = SharedPrefsUtil.name("max_interval").getInt(TAG_MAX_INTERVAL_FOR_OPEN, 0);
        mButtonClose.setText(String.valueOf(MaxIntervalForClose));
        mButtonOpen.setText(String.valueOf(MaxIntervalForOpen));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CLEAR_COVER_STATE_CHANGE);

        mBroadcastReceiver = new HallSensorReceiver();
        registerReceiver(mBroadcastReceiver, intentFilter);

        if (MaxIntervalForClose == 0 || MaxIntervalForOpen == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("请点击上方按钮设置最大响应时间！")
                    .setPositiveButton("确定", null)
                    .create()
                    .show();
        }
    }

    private void initListener() {
        mButtonOpen.setOnClickListener(this);
        mButtonClose.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sensor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart====" + mCurState + "---" + "亮屏");
        if (mIsHallTest && mCurState == 1 && mStartTime != 0) {
            long time = System.currentTimeMillis() - mStartTime;
            Log.d(TAG, "亮屏.time" + time);
            showRecord(time, "开盖->亮屏");
        } else {
            mCurState = -1;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop====" + mCurState + "---" + "灭屏");
        if (mCurState == 0 && mStartTime != 0) {
            long time = System.currentTimeMillis() - mStartTime;
            Log.d(TAG, "灭屏.time" + time);
            showRecord(time, "合盖->皮套应用出现");
            mIsHallTest = true;
        } else {
            mIsHallTest = false;
            mCurState = -1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorLab.close();
        unregisterReceiver(mBroadcastReceiver);
        SharedPrefsUtil.recycle("max_interval");
    }

    @Override
    public void onClick(View view) {
        FragmentManager manager = getSupportFragmentManager();
        if (view.getId() == R.id.btn_interval_open) {
            EditDialogFragment dialog = EditDialogFragment.newInstance(1);
            dialog.show(manager, "dialogFragment");
        } else if (view.getId() == R.id.btn_interval_close) {
            EditDialogFragment dialog = EditDialogFragment.newInstance(0);
            dialog.show(manager, "dialogFragment");
        }
    }

    private void updateFragment() {
        fragments.get(0).updateUI();
        fragments.get(1).updateUI();
    }

    /**
     * 自定义最大 合盖到灭屏响应时间
     *
     * @param time
     */
    @Override
    public void setMaxTime(int flag, int time) {

        if (flag == 0) {
            MaxIntervalForClose = time;
            mButtonClose.setText(String.valueOf(time));
            SharedPrefsUtil.name("max_interval").putInt(TAG_MAX_INTERVAL_FOR_CLOSE, time);
        } else {
            MaxIntervalForOpen = time;
            mButtonOpen.setText(String.valueOf(time));
            SharedPrefsUtil.name("max_interval").putInt(TAG_MAX_INTERVAL_FOR_OPEN, time);
        }
        updateFragment();
    }

    /**
     * Hall广播
     */
    class HallSensorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CLEAR_COVER_STATE_CHANGE)) {
                mStartTime = System.currentTimeMillis();
                mCurState = intent.getIntExtra("state", 0);
                Log.d(TAG, "COVER_STATE_CHANGE：" + intent.getIntExtra("state", 0));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        FragmentManager manager = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_delete:
                PromptDialogFragment prompt = PromptDialogFragment.newInstance("数据即将被删除！不能恢复！", FLAG_DELETE);
                prompt.show(manager, "dialogFragment");
                break;
            case R.id.action_to_excel:
                PromptDialogFragment prompt1 = PromptDialogFragment.
                        newInstance("正在进行导出Excel表格操作，是否继续？", FLAG_SAVE);
                prompt1.show(manager, "dialogFragment");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 提示对话框，回调接口
     *
     * @param type
     */
    @Override
    public void confirm(int type) {
        if (type == FLAG_SAVE) {
            PermissionUtils.requestPermissions(this, 0, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, new PermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
                    exportExcel();
                }

                @Override
                public void onPermissionDenied(String[] deniedPermissions) {
                    Toast.makeText(HallTestActivity.this, "读写外部存储权限被拒绝！", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (type == FLAG_DELETE) {
            clearData();
        }
    }

    /**
     * 导出文件
     */
    private void exportExcel() {
        try {
            int id = mTabLayout.getSelectedTabPosition();
            List<HSensor> results = SensorLab.get(this).getHRecords(HALL_MODE[id]);
            String exportInfo = "";
            if (id == 0) {
                exportInfo = ExcelUtils.createExcelForHSensor("hsensor", results, MaxIntervalForClose, MaxIntervalForOpen);
            } else if (id == 1) {
                exportInfo = ExcelUtils.createExcelForHSensor("hsensorForCall", results, MaxIntervalForClose, MaxIntervalForOpen);
            }
            Snackbar.make(mViewPager, exportInfo, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(mViewPager, "导出失败", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    /**
     * 数据清空
     */
    private void clearData() {
        mSensorLab.delete(HALL_MODE[mTabLayout.getSelectedTabPosition()]);
        updateFragment();
    }


    /**
     * @param time
     * @param status
     */
    private void showRecord(long time, String status) {
        HSensor hSensor = new HSensor(TYPE_HSENSOR, status, time);
        mSensorLab.addHRecord(hSensor);
        fragments.get(0).addRecord(hSensor);
        mCurState = -1;
    }

    /**
     * 界面适配器
     */
    public class TabFragmentPagerAdapter extends FragmentPagerAdapter {
        private String[] mTitles;
        private List<HallFragment> mFragments;

        public TabFragmentPagerAdapter(FragmentManager fm, String[] titles, List<HallFragment> fragments) {
            super(fm);
            mTitles = titles;
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }


}
