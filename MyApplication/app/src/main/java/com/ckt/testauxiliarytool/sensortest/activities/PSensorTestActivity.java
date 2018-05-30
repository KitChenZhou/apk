package com.ckt.testauxiliarytool.sensortest.activities;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.DividerItemDecoration;
import com.ckt.testauxiliarytool.sensortest.adapter.PRecordsListAdapter;
import com.ckt.testauxiliarytool.sensortest.bean.PSensorTestRecord;
import com.ckt.testauxiliarytool.sensortest.service.PSensorTestService;
import com.ckt.testauxiliarytool.utils.ExcelUtils;
import com.ckt.testauxiliarytool.utils.PermissionUtils;

import java.util.List;

public class PSensorTestActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "PST.";

    //表示服务是否异常终止的布尔值
    private boolean mIsServiceCrashed = false;
    //
    private boolean mIsSaveExcel = true;

    private PRecordsListAdapter mPRecordsListAdapter;

    Toolbar mToolBar;
    TextView mAverageTextView;
    Button mStartButton;
    Button mStopButton;
    Button mGetRecordsButton;
    Button mClearButton;
    RecyclerView mRecyclerView;


    private PSensorTestService.PSTestBinder mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (PSensorTestService.PSTestBinder) service;
            mPRecordsListAdapter = new PRecordsListAdapter(PSensorTestActivity.this, mService.getRecords());
            mRecyclerView.setAdapter(mPRecordsListAdapter);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(PSensorTestActivity.this,
                    DividerItemDecoration.VERTICAL_LIST));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceCrashed = true;
            Toast.makeText(PSensorTestActivity.this, "测试服务异常关闭，测试已停止", Toast.LENGTH_SHORT).show();
        }
    };
    private List<PSensorTestRecord> mRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.st_activity_psensor_test);
        initView();
        initListener();
        initPermission();
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                applyWriteSettingsPermission();
            } else {
                //设置系统亮度模式为手动调节模式
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        }
    }


    private void initView() {
        mToolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(mToolBar);
        mAverageTextView = (TextView) findViewById(R.id.tv_average);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mStopButton = (Button) findViewById(R.id.btn_stop);
        mGetRecordsButton = (Button) findViewById(R.id.btn_get_records);
        mClearButton = (Button) findViewById(R.id.btn_clear);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_records);
        mToolBar.setTitle("P-Sensor测试");
        mStopButton.setEnabled(false);
        mClearButton.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void initListener() {
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mGetRecordsButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsServiceCrashed) {
            //如果服务已崩溃，禁用“停止测试”按钮
            mStopButton.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_psensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.btn_save_excel) {
            if (!mIsSaveExcel && mRecords != null && mRecords.size() > 0) {
                mIsSaveExcel = true;
                Toast.makeText(this, ExcelUtils.createExcelForPSensor("PSensorTest", mRecords), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "已导出EXCEL或无记录可导出", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                if (!mIsSaveExcel && mRecords != null && mRecords.size() > 0) {
                    new AlertDialog.Builder(PSensorTestActivity.this)
                            .setTitle("注意")
                            .setMessage("记录未导出，开始新测试将清除旧记录，确定要这样做吗？")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mClearButton.setEnabled(false);
                                    clearRecords();
                                    startTest();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create().show();
                } else {
                    if (mRecords != null && mRecords.size() > 0) {
                        mClearButton.setEnabled(false);
                        clearRecords();
                    }
                    startTest();
                }
                break;
            case R.id.btn_stop:
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
                unbindService(mConnection);
                break;
            case R.id.btn_get_records:
                if (!mIsServiceCrashed && mService != null && mService.getIsRecordsUpdate()) {
                    mPRecordsListAdapter.notifyDataSetChanged();
                    mRecyclerView.smoothScrollToPosition(mService.getRecords().size());
                    mAverageTextView.setText(getString(R.string.p_average_time,
                            mService.getAverageOffTime(), mService.getAverageOnTime()));
                    mClearButton.setEnabled(true);//有记录显示后才能点击“清除记录”按钮
                    mIsSaveExcel = false;//目前处于未导出EXCEL状态
                    mService.setIsRecordsUpdate(false);//更新记录完毕
                    mRecords = mService.getRecords();
                } else {
                    Toast.makeText(this, "记录未更新或无记录", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_clear:
                clearRecords();
                mClearButton.setEnabled(false);
                break;
        }
    }


    private void clearRecords() {
        mService.clearRecords();
        mPRecordsListAdapter.notifyDataSetChanged();
        mAverageTextView.setText("");
    }

    private void startTest() {
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);
        Intent service = new Intent(this, PSensorTestService.class);
        bindService(service, mConnection, BIND_AUTO_CREATE);
        //开始测试后自动跳转到拨号
        Intent toDial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
        startActivity(toDial);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private final int REQUEST_CODE_ASK_WRITE_SETTINGS = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void applyWriteSettingsPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_ASK_WRITE_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ASK_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Toast.makeText(this, "该测试必须WRITE_SETTINGS权限！！", Toast.LENGTH_SHORT).show();
                    applyWriteSettingsPermission();
                }
            }
        }
    }
}
