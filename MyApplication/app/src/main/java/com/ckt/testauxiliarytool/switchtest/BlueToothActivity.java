package com.ckt.testauxiliarytool.switchtest;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.NumberProgressBar;

public class BlueToothActivity extends AppCompatActivity {

    private static final String TAG = "BlueToothActivity";

    private static Toast toast = null;
    private static final String DEFAULT_VALUE="1000";//EditText初始的默认值

    //退出时的时间
    private long mExitTime;

    private EditText mBTVancantTimeEdit;// 蓝牙空闲时间编辑
    private EditText mBTTestTimesEdit; // 蓝牙测试次数编辑
    private TextView mReportMessage;
    private TextView mReportMessageTitle;
    private Button BTStop;
    private Button BTStart;
    private Switch mBTSwitchButton; // 模拟蓝牙开关
    private NumberProgressBar mBTProgressBar;// 蓝牙百分比进度条

    private int mBTVancantTime; // 蓝牙空闲时间
    private int mBTTestTimes; // 蓝牙测试次数

    private int mBTSucceedCount; // 蓝牙成功次数
    private int mBTStartCount; // 蓝牙第几次测试
    private int mBTNotes = 0; // 蓝牙流程记录
    private boolean mBTState = false; // 蓝牙状态

    private Handler mHandler = new Handler();

    private Thread mBTOpenThread = null; // 蓝牙开启线程
    private Thread mBTCloseThread = null; // 蓝牙关闭线程

    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);

        initView();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /* 初始蓝牙状态 */
        mBluetoothAdapter.disable();
        mBTSwitchButton.setChecked(false);

        mBTSwitchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        /* 蓝牙开关测试开始 */
        BTStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBTState) {
                    if (mBluetoothAdapter != null) {
                        if (mBTVancantTimeEdit.getText().toString().length() != 0
                                && mBTTestTimesEdit.getText().toString().length() != 0) {
                            initBT();
                        } else {
                            showToast(getString(R.string.btt_enter_correct_parameters));
                        }
                    } else {
                        showToast(getString(R.string.btt_cant_support_bluetooth));
                    }
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BlueToothActivity.this);
                    dialog.setIcon(android.R.drawable.ic_dialog_info);
                    dialog.setTitle(R.string.btt_notice);
                    dialog.setMessage(R.string.btt_start_again);
                    dialog.setCancelable(true);
                    dialog.setPositiveButton(R.string.btt_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i(TAG, "蓝牙状态：" + mBluetoothAdapter.isEnabled());
                            showToast(getString(R.string.btt_show_restart_test));
                            initBT();
                        }
                    });
                    dialog.setNegativeButton(R.string.btt_cancle, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }
            }
        });

        BTStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBTState) {
                    mBTState = false;
                    showToast(getString(R.string.btt_stop_bluetooth_test));
                    mBluetoothAdapter.disable();
                    mBTSwitchButton.setChecked(false);
                } else {
                    showToast(getString(R.string.btt_show_not_start_test));
                }
            }
        });

        this.registerReceiver(mBTReceiver, makeBTFilter());

        mBTOpenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mBTState && mBTTestTimes > mBTStartCount) {
                    mBluetoothAdapter.enable();
                } else {
                    mBTState = false;
					showToast(getString(R.string.wt_test_finished));
                }
            }
        });

        mBTCloseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.disable();
            }
        });
    }

    private void initView() {
        mBTVancantTimeEdit = (EditText) findViewById(R.id.BT_switch_space_time);
        mBTTestTimesEdit = (EditText) findViewById(R.id.BT_switch_times);
        mBTTestTimesEdit.setText(DEFAULT_VALUE);
        BTStart = (Button) findViewById(R.id.BT_start);
        BTStop = (Button) findViewById(R.id.BT_stop);
        mBTSwitchButton = (Switch) findViewById(R.id.BT_switch_button);
        mBTProgressBar = (NumberProgressBar) findViewById(R.id.number_bar1);
        mReportMessage = (TextView) findViewById(R.id.report_message);
        mReportMessageTitle = (TextView) findViewById(R.id.report_message_title);
    }

    private void initBT() {
        if (mBluetoothAdapter.isEnabled()) {
            showToast(getString(R.string.btt_close_bluetooth_first));
        } else {
            mBluetoothAdapter.enable();
            mBTVancantTime = Integer.parseInt(mBTVancantTimeEdit.getText().toString());
            mBTTestTimes = Integer.parseInt(mBTTestTimesEdit.getText().toString());
            mBTStartCount = 0;
            mBTSucceedCount = 0;
            Log.i(TAG, "蓝牙状态init：" + mBluetoothAdapter.isEnabled());
            mBTNotes = 0;
            mBTState = true;
            mBTProgressBar.setMax(mBTTestTimes);
            mBTProgressBar.setProgress(mBTSucceedCount);
            mReportMessageTitle.setText(R.string.btt_report_bluetooth_test);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive() && getCurrentFocus() != null) {
                if (getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
            getTestMessage();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mBTReceiver);
    }

    BroadcastReceiver mBTReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (mBTState) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        mBTNotes++;
                        mBTStartCount++;
                        mBTSwitchButton.setText(getString(R.string.btt_No_) + mBTStartCount + "/" + mBTTestTimes + getString(R.string.btt_test_times));
                        mBTProgressBar.setProgress(mBTStartCount);
                        Log.i(TAG, "onReceive---------蓝牙正在开启");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        mBTNotes++;
                        Log.i(TAG, "onReceive---------蓝牙开启");
                        mBTSwitchButton.setChecked(true);
                        mHandler.removeCallbacks(mBTOpenThread);
                        mHandler.postDelayed(mBTCloseThread, mBTVancantTime * 1000 + 1000);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mBTNotes++;
                        Log.i(TAG, "onReceive---------蓝牙正在关闭");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        mBTNotes++;
                        Log.i(TAG, "onReceive---------蓝牙关闭");
                        mBTSwitchButton.setChecked(false);
                        mHandler.removeCallbacks(mBTCloseThread);
                        mHandler.postDelayed(mBTOpenThread, mBTVancantTime * 1000 + 1000);
                        if (mBTNotes == 4) {
                            mBTSucceedCount++;
                        }
                        getTestMessage();
                        Log.i(TAG, "mBTSucceedCount: " + mBTSucceedCount);
                        mBTNotes = 0;
                        break;
                }
            }
        }
    };

    private void getTestMessage() {
        double percentage;
        if (mBTStartCount == 0) {
            percentage = 0;
        } else {
            percentage = (mBTSucceedCount * 1.0) / mBTStartCount * 100;
        }
        mReportMessage.setText(getString(R.string.btt_successes_times) + mBTSucceedCount + "\n"
                + getString(R.string.btt_failures_times) + (mBTStartCount - mBTSucceedCount) + "\n"
                + getString(R.string.btt_all_times) + mBTStartCount + "\n"
                + getString(R.string.btt_successes_rate) + percentage + getString(R.string.btt_percentage));
    }

    private IntentFilter makeBTFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    /**
     * Toast
     *
     * @param content 内容
     */
    public void showToast(String content) {
        if (toast == null) {
            toast = Toast.makeText(BlueToothActivity.this, content, Toast.LENGTH_SHORT);
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
            showToast(getString(R.string.btt_click_twice_quit));
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
//            System.exit(0);
//            Intent home = new Intent(Intent.ACTION_MAIN);
//            home.addCategory(Intent.CATEGORY_HOME);
//            startActivity(home);
        }
    }
}
