package com.ckt.testauxiliarytool.sensortest.activities;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.bean.CommendResult;
import com.ckt.testauxiliarytool.utils.ExcelUtils;
import com.ckt.testauxiliarytool.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class GyroscopeTestActivity extends BaseActivity {
    private static String TAG = "GyroscopeTestActivity";
    private Button mBtnResult;
    private Button mBtnSave;
    private Button mBtnClear;
    private Button mBtnStart;
    private TextView mTvResponse;
    private TextView mTvTips;
    private RadioGroup mRgGyroscope;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private float timestamp;
    private List<CommendResult> responseTimeList = new ArrayList<CommendResult>();
    // 将纳秒转化为秒
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float angle[] = new float[3];
    private int fixedAngles[] = {45, 90, 135, 180, 225, 270, 315, 360};

    private List<Long> responseTime = new ArrayList<>();
    private String[] mAxis = {"X轴", "Y轴", "Z轴"};
    private int currentAxis = 0;

    //是否点击了Save
    private boolean checkSave = false;
    private boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.st_activity_gyroscope);
        initView();
        initListener();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        isSupportGyroscope();
        initData();
    }

    private void initView() {
        mTvResponse = (TextView) findViewById(R.id.tv_response);
        mTvTips = (TextView) findViewById(R.id.tv_tips);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnResult = (Button) findViewById(R.id.btn_result);
        mBtnClear = (Button) findViewById(R.id.btn_clear);
        mRgGyroscope = (RadioGroup) findViewById(R.id.rg_gyroscope);
    }

    private void initData() {
        //初始化 当前时间
        responseTime.clear();
        //初始化responseTime 集合
        for (int i = 0; i < 9; i++) {
            responseTime.add(0L);
        }
        //初始化角度
        angle[0] = 0;
        angle[1] = 0;
        angle[2] = 0;
        //更新按钮状态
        isStart = false;
        checkSave = false;
        mBtnStart.setEnabled(true);
        mBtnSave.setEnabled(false);
        mBtnClear.setEnabled(false);
        mBtnResult.setEnabled(false);
        updateRadioGroup(true);
        mTvResponse.setText("");
        mTvTips.setText(getResources().getString(R.string.gyroscope_angle, 0f));
    }

    /**
     * 是否支持陀螺仪
     */
    private void isSupportGyroscope() {
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscopeSensor == null) {
            new AlertDialog.Builder(this)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .setTitle("缺少陀螺仪传感器，请退出当前测试！")
                    .create()
                    .show();
        }
        sensorManager.registerListener(mSensorEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * 更新RadioButtong状态
     *
     * @param isClickable
     */
    private void updateRadioGroup(boolean isClickable) {
        for (int i = 0; i < mRgGyroscope.getChildCount(); i++) {
            mRgGyroscope.getChildAt(i).setEnabled(isClickable);
        }
    }

    private void initListener() {
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStart) {
                    isStart = true;
                    mBtnStart.setEnabled(false);
                    mBtnStart.setText("请向 " + mAxis[currentAxis] + " 方向旋转手机一周");
                    updateRadioGroup(false);
                }
            }
        });
        mRgGyroscope.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.xAxis) currentAxis = 0;
                if (checkedId == R.id.yAxis) currentAxis = 1;
                if (checkedId == R.id.zAxis) currentAxis = 2;
                mRgGyroscope.check(checkedId);
                Log.e(TAG, "cur = " + currentAxis + "__" + mAxis[currentAxis]);
            }
        });
        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSave) {
                    Toast.makeText(GyroscopeTestActivity.this, "已保存！", Toast.LENGTH_SHORT).show();
                } else {
                    PermissionUtils.requestPermissions(GyroscopeTestActivity.this, 0, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, new PermissionUtils.OnPermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            try {
                                int angle = 45;
                                responseTimeList.clear();
                                for (Long r : responseTime) {
                                    CommendResult angleTime = new CommendResult();
                                    angleTime.setTimes(angle);
                                    angleTime.setTime(r.intValue());
                                    angle += 45;
                                    responseTimeList.add(angleTime);
                                }
                                String Catelog = ExcelUtils.createExcelForGyroscope("Gyroscope_test_" + mAxis[currentAxis], responseTimeList);
                                Toast.makeText(GyroscopeTestActivity.this, Catelog, Toast.LENGTH_LONG).show();
                                checkSave = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(GyroscopeTestActivity.this, "导出失败！", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionDenied(String[] deniedPermissions) {
                            Toast.makeText(GyroscopeTestActivity.this, "读写外部存储权限被拒绝！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        mBtnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int k = 0; k < 9; k++) {
                    if (responseTime.get(k) == 0) {
                        mTvResponse.setText("手速太快，没有采集到全部测试数据！\n请清空记录，重新来过！");
                        return;
                    }
                }
                mTvResponse.setText("");
                for (int i = 0; i < 8; i++) {
                    Log.e(TAG, responseTime.get(i + 1) + "-" + responseTime.get(i) + "=");
                    mTvResponse.append("\n" + " 角度：" + fixedAngles[i] + " 响应时间：" + (responseTime.get(i + 1) - responseTime.get(i)) + "ms");
                }
                mBtnSave.setEnabled(true);
                mBtnClear.setEnabled(true);
                mBtnResult.setEnabled(false);
            }
        });

    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (isStart) {
                //从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值；否则，为负值
                if (timestamp != 0) {
                    // 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒
                    float dT = (event.timestamp - timestamp) * NS2S;
                    // 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度
                    angle[0] += event.values[0] * dT;
                    angle[1] += event.values[1] * dT;
                    angle[2] += event.values[2] * dT;
                    // 将弧度转化为角度
                    Log.e(TAG, "dT = " + dT);
                    Log.e(TAG, "angle[currentAxis] = " + angle[currentAxis]);
                    float currentAngle = -(float) Math.toDegrees(angle[currentAxis]);

                    Log.e(TAG, "currentAngle" + currentAngle);

                    if (currentAngle < 10.99 && currentAngle > 0.00) {
                        Log.d(TAG, "开始时间：" + System.currentTimeMillis());
                        responseTime.set(0, System.currentTimeMillis());
                    }
                    if (55.99 > currentAngle && currentAngle > 35.00) {
                        Log.d(TAG, "45度的时间：" + System.currentTimeMillis());
                        responseTime.set(1, System.currentTimeMillis());
                    }
                    if (99.99 > currentAngle && currentAngle > 80.00) {
                        Log.d(TAG, "90度的时间：" + System.currentTimeMillis());
                        responseTime.set(2, System.currentTimeMillis());
                    }
                    if (145.99 > currentAngle && currentAngle > 125.00) {
                        Log.d(TAG, "135度的时间：" + System.currentTimeMillis());
                        responseTime.set(3, System.currentTimeMillis());
                    }
                    if (190.99 > currentAngle && currentAngle > 170.00) {
                        Log.d(TAG, "180度的时间：" + System.currentTimeMillis());
                        responseTime.set(4, System.currentTimeMillis());
                    }
                    if (235.99 > currentAngle && currentAngle > 215.00) {
                        Log.d(TAG, "225度的时间：" + System.currentTimeMillis());
                        responseTime.set(5, System.currentTimeMillis());
                    }
                    if (280.99 > currentAngle && currentAngle > 260.00) {
                        Log.d(TAG, "270度的时间：" + System.currentTimeMillis());
                        responseTime.set(6, System.currentTimeMillis());
                    }
                    if (325.99 > currentAngle && currentAngle > 305.00) {
                        Log.d(TAG, "315度的时间：" + System.currentTimeMillis());
                        responseTime.set(7, System.currentTimeMillis());
                    }
                    if (370.99 > currentAngle && currentAngle > 350.00) {
                        Log.d(TAG, "360度的时间：" + System.currentTimeMillis());
                        responseTime.set(8, System.currentTimeMillis());
                    }
                    System.out.println("currentAngle------------>" + currentAngle);
                    System.out.println("gyroscopeSensor.getMinDelay()----------->" +
                            gyroscopeSensor.getMinDelay());
                    mTvTips.setText(getResources().getString(R.string.gyroscope_angle, currentAngle));
                    if (currentAngle > 371) {
                        //测试完成
                        isStart = false;
                        mBtnResult.setEnabled(true);
                        mTvResponse.setText("数据采集完成，请点击按钮获取结果");
                    }
                }
            }
            //将当前时间赋值给timestamp
            timestamp = event.timestamp;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        sensorManager.unregisterListener(mSensorEventListener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
