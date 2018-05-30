package com.ckt.testauxiliarytool.switchtest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.NumberProgressBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 重复开启关闭相机功能
 * Created by cl on 2017.12.14.
 * Camera Test
 */
public class CameraActivity extends Activity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_CODE_ASK_CAMERA = 2;
    private static final String DEFAULT_VALUE = "1000";//EditText初始的默认值

    private static Toast toast = null;
    //退出时的时间
    private long mExitTime;
    private Button mStartBtn;
    private Button mStopBtn;
    private boolean isOpen; //TRUE表示相机开启 反之关闭
    private Timer mTimer;
    private EditText mVancantTimeEdit;// Camera空闲时间编辑
    private EditText mTestTimesEdit; // Camera测试次数编辑
    private TextView mShowCount; // Camera已完成开关次数
    private NumberProgressBar mProgressBar;// Camera百分比进度条
    private TextView mReportMessage;
    private TextView mReportMessageTitle;

    private int mRunningCount; // 成功开关次数
    private int mVancantTime; // Camera空闲时间
    private int mTestTimes; // Camera测试次数
    private boolean isRunning; // 是否开始测试
    private int mStartCount; // 开启次数
    private boolean isSucceed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        initView();
        // 开始
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVancantTimeEdit.getText().toString().length() != 0
                        && mTestTimesEdit.getText().toString().length() != 0) {
                    if (!isRunning) {
                        if (ActivityCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CODE_ASK_CAMERA);
                        } else {
                            hideKeyboard();
                            initData();
                            startTimer();
                        }
                    } else {
                        showToast(getString(R.string.ct_stop_test_first));
                    }
                } else {
                    showToast(getString(R.string.ct_enter_correct_parameters));
                }
            }
        });

        // 停止
        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimer != null) {
                    showToast(getString(R.string.ct_stop_test));
                    mTimer.cancel();
                    isRunning = false;
                } else {
                    showToast(getString(R.string.ct_not_start_test));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ASK_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(getString(R.string.ct_get_camera_permission));
                    hideKeyboard();
                    initData();
                    startTimer();
                } else {
                    showToast(getString(R.string.ct_refuse_camera_permission));
                }
                break;
        }
    }

    /**
     * 开始循环打开关闭Camera
     */
    private void startTimer() {
//        Observable.intervalRange(0, 10, 5, 5, TimeUnit.SECONDS)
//                .doFinally(new Action() {
//                    @Override
//                    public void run() throws Exception {
//                        Log.i("Cherry", "finally");
//                    }
//                })
//                .subscribe(new Observer<Long>() {
//                    @Override
//                    public void onSubscribe(Disposable disposable) {
//                        Log.i("Cherry", "start");
//                    }
//
//                    @Override
//                    public void onNext(@io.reactivex.annotations.NonNull Long aLong) {
//                        Log.i("Cherry", "接受到了事件：" + aLong);
//                    }
//
//                    @Override
//                    public void onError(@io.reactivex.annotations.NonNull Throwable throwable) {
//                        Log.i("Cherry", "onError");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.i("Cherry", "over");
//                    }
//                });
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isOpen) {
                    openCamera();
                    mStartCount++;
                    isOpen = true;
                    isSucceed = true;
                } else {
                    finishActivity(REQUEST_CAMERA);
                    isOpen = false;
                    if (isSucceed) {
                        mRunningCount++;
                        isSucceed = false;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mShowCount.setText(getString(R.string.ct_completed) +mRunningCount + "/" + mTestTimes + " " + getString(R.string.ct_camera_test_times));
                            mProgressBar.setProgress(mRunningCount);
                            mReportMessage.setText(getString(R.string.ct_successes_times) + mRunningCount + "\n"
                                    + getString(R.string.ct_failures_times) + (mStartCount - mRunningCount) + "\n"
                                    + getString(R.string.ct_all_times) + mRunningCount + "\n"
                                    + getString(R.string.ct_successes_rate) + (mRunningCount * 1.0) / mStartCount * 100 + getString(R.string.ct_percentage));
                        }
                    });
                }
                if (mRunningCount >= mTestTimes) {
                    this.cancel();//取消时间任务
                    isRunning = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(CameraActivity.this);
                            dialog.setIcon(android.R.drawable.ic_dialog_info);
                            dialog.setTitle(R.string.ct_test_completed);
//                            dialog.setMessage("测试已完成！");
                            dialog.setCancelable(true);
                            dialog.setPositiveButton(R.string.ct_confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            dialog.show();
                        }
                    });
                }
            }
        }, 0, mVancantTime * 1000 + 1000);//转换成秒
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mStartBtn = (Button) findViewById(R.id.start_button);
        mStopBtn = (Button) findViewById(R.id.stop_button);
        mVancantTimeEdit = (EditText) findViewById(R.id.vancant_time_edit);
        mTestTimesEdit = (EditText) findViewById(R.id.test_times_edit);
        mTestTimesEdit.setText(DEFAULT_VALUE);
        mProgressBar = (NumberProgressBar) findViewById(R.id.progress_bar);
        mShowCount = (TextView) findViewById(R.id.show_count);
        mReportMessage = (TextView) findViewById(R.id.report_message);
        mReportMessageTitle = (TextView) findViewById(R.id.report_message_title);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mVancantTime = Integer.parseInt(mVancantTimeEdit.getText().toString());
        mTestTimes = Integer.parseInt(mTestTimesEdit.getText().toString());
        mRunningCount = 0;
        isOpen = false;
        mProgressBar.setMax(mTestTimes);
        mProgressBar.setProgress(0);
        mReportMessageTitle.setText(R.string.ct_report_test);
        isRunning = true;
        mStartCount = 0;
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 开启前置摄像头
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机 默认启动后置摄像头
//        intent.putExtra("camerasensortype", 2);
//        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);//启动前置摄像头
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * Toast
     *
     * @param content 内容
     */
    public void showToast(String content) {
        if (toast == null) {
            toast = Toast.makeText(CameraActivity.this, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            showToast(getString(R.string.ct_click_twice_quit));
            mExitTime = System.currentTimeMillis();
        } else {
            if (mTimer != null) {
                mTimer.cancel();
            }
            finish();
//            System.exit(0);
//            Intent home = new Intent(Intent.ACTION_MAIN);
//            home.addCategory(Intent.CATEGORY_HOME);
//            startActivity(home);
        }
    }
}
