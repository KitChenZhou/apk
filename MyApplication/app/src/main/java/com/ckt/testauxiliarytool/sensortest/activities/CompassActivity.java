package com.ckt.testauxiliarytool.sensortest.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.SensorType;
import com.ckt.testauxiliarytool.sensortest.adapter.MAdapter;
import com.ckt.testauxiliarytool.sensortest.bean.MSensor;
import com.ckt.testauxiliarytool.sensortest.db.SensorLab;
import com.ckt.testauxiliarytool.sensortest.widget.CompassView;
import com.ckt.testauxiliarytool.sensortest.widget.PromptDialogFragment;
import com.ckt.testauxiliarytool.utils.ExcelUtils;
import com.ckt.testauxiliarytool.utils.PermissionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.ckt.testauxiliarytool.sensortest.SensorType.TYPE_MSENSOR;

/**
 * 使用方向传感器获取方向
 * 方向值和mmi_test中的指南针一样
 */
public class CompassActivity extends BaseActivity implements PromptDialogFragment.mCallBack {

    private static String TAG = "CompassActivity";
    /**
     * 控件
     */
    private TextView mTvReference;
    private TextView mTvAccuracy;
    private LinearLayout mLLCalibration;
    private FloatingActionButton mBtnRecord;
    private RecyclerView mRecyclerView;
    private CompassView mCompassView;
    private ImageView mIvCalibration;
    private AnimationDrawable animationDrawable;
    /**
     * 传感器
     */
    private Sensor oSensor;
    private Sensor mSensor;
    private SensorManager sm;
    /**
     * 数据
     */
    private List<MSensor> results = new ArrayList<>();
    private MAdapter myAdapter;
    private int[] angles = new int[]{0, 45, 90, 135, 180, 225, 270, 315};
    private SensorLab mSensorLab;
    //参考值
    private int angle = 0;
    //对话框标志值
    private static int FLAG_SAVE = 1;
    private static int FLAG_DELETE = 2;
    private Vibrator mVibrator;
    //精确值次数
    private static final int MAX_ACCURATE_COUNT = 20;
    private static final int MAX_INACCURATE_COUNT = 20;
    private volatile int mAccurateCount;
    private volatile int mInaccurateCount;
    private volatile boolean mCalibration;
    private float mTargetDirection;
    private AccelerateInterpolator mInterpolator;
    private int mMagneticFieldAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE;
    private final float EARTH_MAGNETIC_MIN = SensorManager.MAGNETIC_FIELD_EARTH_MIN - 5;
    private final float EARTH_MAGNETIC_MAX = SensorManager.MAGNETIC_FIELD_EARTH_MAX + 5;
    private float[] magneticFieldValues = new float[3];


    private void resetAccurateCount() {
        mAccurateCount = 0;
    }

    private void increaseAccurateCount() {
        mAccurateCount++;
    }

    private void resetInaccurateCount() {
        mInaccurateCount = 0;
    }

    private void increaseInaccurateCount() {
        mInaccurateCount++;
    }

    /**
     * 校准选择
     *
     * @param isCalibration
     */
    private void switchMode(boolean isCalibration) {
        mCalibration = isCalibration;
        if (mCalibration) {
            mLLCalibration.setVisibility(View.VISIBLE);
            animationDrawable.start();
            mBtnRecord.setEnabled(false);
            resetAccurateCount();
        } else {
            mLLCalibration.setVisibility(View.GONE);
            animationDrawable.stop();
            mBtnRecord.setEnabled(true);
            Toast.makeText(this, R.string.calibrate_success, Toast.LENGTH_SHORT).show();
            mVibrator.vibrate(200);
            resetInaccurateCount();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.st_activity_msensor);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mIvCalibration = (ImageView) findViewById(R.id.iv_calibrate);
        mTvAccuracy = (TextView) findViewById(R.id.tv_accuracy);
        animationDrawable = (AnimationDrawable) mIvCalibration.getDrawable();
        mLLCalibration = (LinearLayout) findViewById(R.id.ll_calibration);
        mTvReference = (TextView) findViewById(R.id.tv_refer);
        mBtnRecord = (FloatingActionButton) findViewById(R.id.btn_record);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_record);
        mCompassView = (CompassView) findViewById(R.id.cv_compass);
    }

    private void initData() {
        mSensorLab = SensorLab.get(this);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mInterpolator = new AccelerateInterpolator();
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        oSensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (mSensor == null || oSensor == null) {
            new AlertDialog.Builder(this)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .setTitle("因缺少相关传感器，所以不支持M-Sensor测试！")
                    .create()
                    .show();
        }
        results = mSensorLab.getMRecords(TYPE_MSENSOR);
        myAdapter = new MAdapter(this, results);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(myAdapter);
    }


    private void initListener() {
        mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //计算偏差角度:0 - 359
                float z = Math.abs(mTargetDirection > 337.5 ?
                        360 - mTargetDirection : mTargetDirection - angle);

                //保留三位小数
                BigDecimal bg = new BigDecimal(z);
                z = bg.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();

                //保存数据
                MSensor result = new MSensor(TYPE_MSENSOR, angle, z);
                results.add(0, result);
                mSensorLab.addMRecord(result);
                //更新视图
                myAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(0);
            }
        });

        //滑动监听
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean isSignificantDelta = Math.abs(dy) > 0;
                if (isSignificantDelta) {
                    if (dy > 0) {
                        //向下
                        mBtnRecord.hide();
                    } else {
                        //向上
                        mBtnRecord.show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sensor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单按键监听
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        FragmentManager manager = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_delete:
                PromptDialogFragment prompt = PromptDialogFragment.
                        newInstance("数据即将被删除！不能恢复！", FLAG_DELETE);
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

    /***
     * 在onStop中注销传感器的监听事件
     */
    @Override
    protected void onStop() {
        super.onStop();
        sm.unregisterListener(listener);
    }

    /***
     * 在onStart中注册传感器的监听事件
     */
    @Override
    protected void onStart() {
        super.onStart();
        sm.registerListener(listener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(listener, oSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorLab.close();
    }

    /**
     * 传感器的监听对象
     */
    SensorEventListener listener = new SensorEventListener() {
        //传感器改变时,一般是通过这个方法里面的参数确定传感器状态的改变
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                mTargetDirection = event.values[0];
                mCompassView.setRotate(mTargetDirection);
                Log.d(TAG, "" + mTargetDirection);
                showDirectionToView();
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mTvAccuracy.setText(getResources().getString(R.string.accuracy_compass, event.accuracy));
                magneticFieldValues = event.values;
                mMagneticFieldAccuracy = event.accuracy;
                //指南针校准
                calibrateCompass();
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mTvAccuracy.setText(getResources().getString(R.string.accuracy_compass, accuracy));
            }
            //当精准度改变时
        }
    };

    /**
     * 显示方向，更新UI
     */
    private void showDirectionToView() {
        //转换角度
        for (int angle1 : angles) {
            if (Math.abs(angle1 - mTargetDirection) <= 22.5) {
                angle = angle1;
                break;
            }
        }
        mTvReference.setText(String.valueOf(angle));
    }

    /**
     * 指南针校准
     */
    private void calibrateCompass() {
        //计算磁场
        double magneticField = Math.sqrt(magneticFieldValues[0] * magneticFieldValues[0]
                + magneticFieldValues[1] * magneticFieldValues[1]
                + magneticFieldValues[2] * magneticFieldValues[2]);
        Log.d(TAG, "磁场强度magneticField = " + magneticField);
        //取20组数据判断指南针是否校准成功
        if (mCalibration) {
            if (mMagneticFieldAccuracy != SensorManager.SENSOR_STATUS_UNRELIABLE
                    && magneticField >= EARTH_MAGNETIC_MIN
                    && magneticField <= EARTH_MAGNETIC_MAX) {
                increaseAccurateCount();
            } else {
                resetAccurateCount();
            }
            Log.d(TAG, "accurate count = " + mAccurateCount);
            if (mAccurateCount >= MAX_ACCURATE_COUNT) {
                switchMode(false);
            }
        } else {
            if (mMagneticFieldAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
                    || magneticField < EARTH_MAGNETIC_MIN
                    || magneticField > EARTH_MAGNETIC_MAX) {
                increaseInaccurateCount();
            } else {
                resetInaccurateCount();
            }
            Log.d(TAG, "inaccurate count = " + mInaccurateCount);
            if (mInaccurateCount >= MAX_INACCURATE_COUNT) {
                switchMode(true);
            }
        }
    }


    /**
     * 对话框确认按钮回调
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
                    Toast.makeText(CompassActivity.this, "读写外部存储权限被拒绝！", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (type == FLAG_DELETE) {
            results.clear();
            mSensorLab.delete(TYPE_MSENSOR);
            myAdapter.notifyDataSetChanged();
            mBtnRecord.show();
        }
    }

    /**
     * 导出Excel表格
     */
    private void exportExcel() {
        try {
            List<MSensor> results = SensorLab.get(this).getMRecords(SensorType.TYPE_MSENSOR);
            String exportInfo = ExcelUtils.createExcelForMSensor("msensor", results);
            Snackbar.make(mBtnRecord, exportInfo, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(mBtnRecord, "导出失败", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
