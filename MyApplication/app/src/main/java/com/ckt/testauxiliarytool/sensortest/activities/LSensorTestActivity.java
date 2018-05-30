package com.ckt.testauxiliarytool.sensortest.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.DividerItemDecoration;
import com.ckt.testauxiliarytool.sensortest.LcdBrightnessGetter;
import com.ckt.testauxiliarytool.sensortest.adapter.LRecordsListAdapter;
import com.ckt.testauxiliarytool.sensortest.bean.LSensorTestRecord;
import com.ckt.testauxiliarytool.utils.ExcelUtils;
import com.ckt.testauxiliarytool.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class LSensorTestActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "LST.";


    private boolean mIsListening = false;//是否监听屏幕亮度的标志
    private boolean mIsTestStart = false;//开始测试的标志
    private boolean mStarting = false;//是否测试中的标志
    private boolean mIsSaveExcel = true;//是否保存记录的标志
    private boolean mLightPrepare = false;

    private int mBrightness = -1;//当前屏幕亮度（LCD）
    private float mLux;
    private float mAverageTime = 0;

    private long mStartTime = -1;//一次记录的开始时间
    private long mEndTime = -1;//一次记录的结束时间
    private long mDuration = -1;//一次记录的总时间
    private long mSumTime = 0;//所有记录的总时间

    private String mTestName = "暗环境";//默认暗环境

    private SensorManager mSensorManager;
    private Sensor mLight;
    private LRecordsListAdapter mAdapter;
    private LSensorTestRecord mRecord;
    private List<LSensorTestRecord> mRecords;


    Toolbar mToolBar;
    TextView mLuxTextView;
    TextView mBrightnessTextView;
    RadioGroup mRadioGroup;
    Button mStartButton;
    Button mStopButton;
    Button mClearButton;
    RecyclerView mRecyclerView;
    TextView mAverageTextView;
    RadioButton mDarkRadioButton;
    RadioButton mLightRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.st_activity_lsensor_test);
        initView();
        initData();
        initListener();
        initPermission();
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                applyWriteSettingsPermission();
            } else {
                //设置系统亮度模式为自动调节模式
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            }
        }
    }


    private void initView() {
        mToolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(mToolBar);
        mLuxTextView = (TextView) findViewById(R.id.tv_lux);
        mBrightnessTextView = (TextView) findViewById(R.id.tv_brightness);
        mAverageTextView = (TextView) findViewById(R.id.tv_average);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mStopButton = (Button) findViewById(R.id.btn_stop);
        mClearButton = (Button) findViewById(R.id.btn_clear);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_records);
        mDarkRadioButton = (RadioButton) findViewById(R.id.rb_dark);
        mLightRadioButton = (RadioButton) findViewById(R.id.rb_light);
        mStopButton.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mToolBar.setTitle("L-Sensor测试");
    }

    private void initData() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        mRecords = new ArrayList<>();
        mAdapter = new LRecordsListAdapter(this, mRecords);
        mAdapter.setOnLongClickListener(new LRecordsListAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(int position) {
                mSumTime = mSumTime - mRecords.get(position).getTime();
                mRecords.remove(position);
                mAverageTime = ((mSumTime) / mRecords.size()) / 1000f;
                mAverageTextView.setText(getString(R.string.l_average_time, mAverageTime));
                mAdapter.notifyDataSetChanged();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        //
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.rb_dark) {
                    mTestName = "暗环境";
                } else {
                    mTestName = "亮环境";
                }
            }
        });
    }

    private void initListener() {
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsListening) {
            mIsListening = true;
            listenBrightness();
        }
        mSensorManager.registerListener(mSensorEventListener, mLight, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lsensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.btn_save_excel) {
            if (!mIsSaveExcel && mRecords.size() > 0) {
                mIsSaveExcel = true;
                Toast.makeText(this, ExcelUtils.createExcelForLSensor("LSensorTest", mRecords), Toast.LENGTH_SHORT).show();
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
                if (!mIsSaveExcel && mRecords.size() > 0) {
                    new AlertDialog.Builder(LSensorTestActivity.this)
                            .setTitle("注意")
                            .setMessage("记录未导出，开始新测试将清除旧记录，确定要这样做吗？")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    clearRecords();
                                    mIsTestStart = true;
                                    mIsSaveExcel = true;
                                    mStartButton.setEnabled(false);
                                    mStopButton.setEnabled(true);
                                    mDarkRadioButton.setEnabled(false);
                                    mLightRadioButton.setEnabled(false);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create().show();
                } else {
                    mIsTestStart = true;
                    mStartButton.setEnabled(false);
                    mStopButton.setEnabled(true);
                    mDarkRadioButton.setEnabled(false);
                    mLightRadioButton.setEnabled(false);
                }

                break;
            case R.id.btn_stop:
                mIsTestStart = false;
                mRadioGroup.setEnabled(true);
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
                mDarkRadioButton.setEnabled(true);
                mLightRadioButton.setEnabled(true);
                break;
            case R.id.btn_clear:
                if (mRecords.size() == 0) {
                    break;
                } else if (!mIsSaveExcel)
                    clearRecords();
                break;
        }
    }

    private void clearRecords() {
        mRecords.clear();
        mSumTime = 0;
        mAverageTextView.setText("");
        mAdapter.notifyDataSetChanged();
    }


    /**
     * L-Sensor事件监听器
     */
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mLux = event.values[0];
            mLuxTextView.setText(getString(R.string.show_lux, mLux));
            //如果已经点击开始测试，判断是否满足测试条件
            if (mIsTestStart) {
                if (TextUtils.equals(mTestName, "暗环境")) {
                    if (mLux <= 4.0f && !mStarting) {
                        //如果光照强度达到条件且不是测试中，则开启一次测试
                        mStarting = true;
                        //记录开始时间
                        mRecord = new LSensorTestRecord();
                        mRecord.setRange(mBrightness + "->");
                        mStartTime = System.currentTimeMillis();
                        Log.i(TAG, "开始时间: " + mStartTime);
                    }
                    if (mStarting && mLux > 4.0f) {
                        mStarting = false;
                    }
                } else {
                    //如果是亮环境
                    if (mLux <= 4.0f && !mStarting) {
                        mLightPrepare = true;
                    } else if (mLux <= 4.0f) {
                        //防止未正确进行准备操作
                        mLightPrepare = true;
                        mStarting = false;
                    }
                    if (mLightPrepare && mLux > 4.0f) {
                        mLightPrepare = false;
                        mStarting = true;
                        mRecord = new LSensorTestRecord();
                        mRecord.setRange(mBrightness + "->");
                        mStartTime = System.currentTimeMillis();
                    }
                }

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    /**
     * 监听LCD亮度值的方法
     * 需要系统权限
     * 内有线程
     */
    private void listenBrightness() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsListening) {
                    try {
                        int tempBrightness = LcdBrightnessGetter.getLcdBrightness();
                        if (mBrightness != tempBrightness) {
                            //当亮度值有变化时
                            mBrightness = tempBrightness;
                            //每次亮度变化都记录时间
                            if (mStarting) {
                                mEndTime = System.currentTimeMillis();
                                Log.i(TAG, "结束时间：" + mEndTime);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mBrightnessTextView.setText(getString(R.string.brightness, mBrightness));
                                }
                            });
                        } else {
                            //当亮度值没变化时
                            if (mStarting && mEndTime != -1 && mDuration == -1
                                    && System.currentTimeMillis() - mEndTime >= 3000) {
                                mDuration = mEndTime - mStartTime;
                                Log.i(TAG, "变化时间：" + mDuration);
                                //
                                mSumTime = mSumTime + mDuration;
                                mRecord.setTestName(mTestName);
                                mRecord.setLux(mLux);
                                mRecord.setRange(mRecord.getRange() + mBrightness);
                                mRecord.setTime(mDuration);
                                mRecords.add(mRecord);
                                mAverageTime = ((mSumTime) / mRecords.size()) / 1000f;
                                mDuration = -1;
                                mStartTime = -1;
                                mEndTime = -1;
                                mStarting = false;
                                mIsSaveExcel = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyItemChanged(mRecords.size() - 1);
                                        mRecyclerView.smoothScrollToPosition(mRecords.size() - 1);
                                        mAverageTextView.setText(getString(R.string.l_average_time, mAverageTime));
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsListening = false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void applyWriteSettingsPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_ASK_WRITE_SETTINGS);
    }

    private final int REQUEST_CODE_ASK_WRITE_SETTINGS = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ASK_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Toast.makeText(this, "该测试必须WRITE_SETTINGS权限！！", Toast.LENGTH_SHORT).show();
//                    applyWriteSettingsPermission();
                }
            }
        }
    }
}

