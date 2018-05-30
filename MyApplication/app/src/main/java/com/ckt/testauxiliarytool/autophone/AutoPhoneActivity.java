package com.ckt.testauxiliarytool.autophone;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.CallLog.Calls;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.MyDataBaseHelper;
import com.ckt.testauxiliarytool.utils.AutoPhoneUtils;
import com.ckt.testauxiliarytool.utils.NumberProgressBar;
import com.ckt.testauxiliarytool.utils.OnProgressBarListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class AutoPhoneActivity extends AppCompatActivity implements OnProgressBarListener {

    public static final String TAG = "AutoPhoneActivity";
    private static final int REQUEST_CODE_ASK_WRITE_SETTINGS = 1;
    private static final int REQUEST_CODE_ASK_CALL_SETTINGS = 2;
    private static final int REQUEST_CODE_ASK_STORAGE_SETTINGS = 3;
    private static final int WAITING_TIME_OF_DIAL_NOT_SERVICE_NUMBER = 10000;
    private static final int WAITING_TIME_OF_DIAL_SERVICE_NUMBER = 5000;
    private AutoCompleteTextView mPhoneNumEdit = null; // 设置拨打的电话号码
    private EditText mSpaceTimeEdit = null; // 设置间隔时间
    private EditText mCallDurationTimeEdit = null; // 设置通话时长
    private EditText mCallCountsEdit = null; // 设置拨号次数
    private TextView mShowAlreadyCallCount = null;// 显示已打电话次数
    private NumberProgressBar mNumberProgressBar;// 百分比进度条
    private Button exportFile;//导出文件按钮

    private boolean endCall = false;  // 判断是否正常挂断
    private boolean callState = false; // 判断电话通话状态
    private boolean isGetReport = false; // 判断是否获得报告
    public static boolean isExportReport = false;// 判断是否导出报告

    private int spaceTime = 0; // 间隔时间
    private int durationTime = 0; // 拨号持续时间
    private int allCallCount = 0;  // 需要拨打次数
    private int alreadyCallCount = 0; // 已拨打次数
    private int sleepTimes; // 休眠时间

    private String phoneNum = null; // 电话号码
    private String sleepTimeString; // spinner里的休眠时间
    private String networkType; // 网络状态

    private ITelephony iPhoney = null;
    private TelephonyManager tm; // 电话管理器

    Thread mCallThread = null; // 打电话线程
    Thread mEndThread = null;  // 挂电话线程
    Handler handler = new Handler();

    private ArrayList<String> phoneNumberList = new ArrayList<>();// 存放电话号码
    private ArrayList<String> networkTypeList = new ArrayList<>(); // 存放信号状态
    private ArrayList<CallRecord> callRecordList = new ArrayList<>(); // 存放CallRecord对象

    private MyDataBaseHelper dbHelper;
    private ArrayAdapter<String> phoneNumberAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_phone);

        Spinner sleepSpinner = (Spinner) findViewById(R.id.sleep_spinner);
        /*
          设置下拉框格式
          建立数据源
          建立Adapter并且绑定数据源
          绑定 Adapter到控件
         */
        String[] mItems = getResources().getStringArray(R.array.ap_sleep_time_choose);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        sleepSpinner.setAdapter(adapter);

        Button setSleepTime = (Button) findViewById(R.id.start_sleep_time);
        final Button startBtn = (Button) findViewById(R.id.startBtn);
        Button stopBtn = (Button) findViewById(R.id.stopBtn);
        final Button getReports = (Button) findViewById(R.id.get_reports);
        exportFile = (Button) findViewById(R.id.export_file);

        mPhoneNumEdit = (AutoCompleteTextView) findViewById(R.id.phone_num);
        mSpaceTimeEdit = (EditText) findViewById(R.id.wait_time);
        mCallDurationTimeEdit = (EditText) findViewById(R.id.call_duration_time);
        mCallCountsEdit = (EditText) findViewById(R.id.call_times);
        mShowAlreadyCallCount = (TextView) findViewById(R.id.show_count);
        mNumberProgressBar = (NumberProgressBar) findViewById(R.id.numberbar1);
        mNumberProgressBar.setOnProgressBarListener(this);

        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE); // 手机通话状态监听器开启
        iPhoney = AutoPhoneUtils.getITelephony(this);//获取电话实例

        /*
         * 获取数据库数据
         */
        MyDataBaseHelper dbHelper = new MyDataBaseHelper(this, "Number.db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("database", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String Num = cursor.getString(cursor.getColumnIndex("number"));
                if (!phoneNumberList.contains(Num)) {
                    Log.i(TAG, "Num = " + Num);
                    phoneNumberList.add(Num);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        Collections.reverse(phoneNumberList);
        phoneNumberAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, phoneNumberList);//配置Adapter
        mPhoneNumEdit.setAdapter(phoneNumberAdapter);
        mPhoneNumEdit.setDropDownHeight(280);
        mPhoneNumEdit.setThreshold(1);

        /*
          开始拨号按钮
         */
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否插入SIM卡
                if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
                    //判断手机通话状态
                    if (!callState) {
                        //判断拨号后是否导出文件
                        if (alreadyCallCount != 0 && !isExportReport) {
                            startBtn.setClickable(false);
                            AlertDialog.Builder dialog = new AlertDialog.Builder(AutoPhoneActivity.this);
                            dialog.setTitle(R.string.ap_warning);
                            dialog.setIcon(android.R.drawable.ic_dialog_info);
                            dialog.setMessage(R.string.ap_dont_export_file_go_on_or_not);
                            dialog.setCancelable(false);
                            dialog.setPositiveButton(R.string.ap_go_on, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startBtn.setClickable(true);
                                    initCall();
                                }
                            });
                            dialog.setNegativeButton(R.string.ap_cancle, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startBtn.setClickable(true);
                                }
                            });
                            dialog.show();
                        } else {
                            initCall();
                        }
                    } else {
                        AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_please_try_again_after_dial_successfully));
                    }
                } else {
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_no_SIM_card));
                }
            }
        });

        /*
          获得下拉框数据
         */
        sleepSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) adapterView.getAdapter();
                sleepTimeString = adapter.getItem(i);
                switch (i) {
                    case 0:
                        sleepTimes = 15;
                        break;
                    case 1:
                        sleepTimes = 30;
                        break;
                    case 2:
                        sleepTimes = 60;
                        break;
                    case 3:
                        sleepTimes = 120;
                        break;
                    case 4:
                        sleepTimes = 300;
                        break;
                    case 5:
                        sleepTimes = 600;
                        break;
                    case 6:
                        sleepTimes = 1800;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        /*
          设置休眠时间
         */
        setSleepTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Settings.System.canWrite(AutoPhoneActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            Uri.parse("package:" + getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, REQUEST_CODE_ASK_WRITE_SETTINGS);
                } else {
                    Settings.System.putInt(getContentResolver(),
                            Settings.System.SCREEN_OFF_TIMEOUT,
                            sleepTimes * 1000);
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_set_duration_time_to_be) + sleepTimeString);
                }
            }
        });

        /*
          停止按钮
         */
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callState) {
                    callState = false;//切换监听器状态
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_stop_dial));
                }
            }
        });

        /*
          获取报告按钮
         */
        getReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyCallCount != 0) {
                    getReports.setClickable(false);
                    getCallInfo();//获取通话数据
                    isGetReport = true;
                    AutoPhoneUtils.getReport(AutoPhoneActivity.this, phoneNum, callRecordList, getReports);
                } else {
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_please_search_information_after_dialing));
                }
            }
        });
        /*
          导出通话记录文件
         */
        exportFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGetReport) {
                    exportFile.setClickable(false);
                    if(AutoPhoneUtils.isMobileNumber(phoneNum)){
                        AutoPhoneUtils.showToast(AutoPhoneActivity.this,getString(R.string.ap_no_report_when_dial_not_service_number));
                        exportFile.setClickable(true);
                    }else{
                        AutoPhoneUtils.exportFile(AutoPhoneActivity.this, callRecordList, exportFile); // 导出文件
                    }
                } else {
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_please_get_report_firstly));
                }

            }
        });

        /*
        拨打电话线程
         */
        mCallThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (callState && alreadyCallCount < allCallCount) {
                    // 获取拨号时的网络状态
                    networkType = AutoPhoneUtils.getNetworkClass(AutoPhoneActivity.this, tm.getNetworkType());
                    networkTypeList.add(networkType);
                    endCall = false;
                    alreadyCallCount++;// 拨打次数 + 1
                    AutoPhoneUtils.call(AutoPhoneActivity.this, phoneNum);// 拨打电话
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, networkType);
                    AutoPhoneUtils.showLog("call beginning");

                    mNumberProgressBar.setProgress(alreadyCallCount);
                    mShowAlreadyCallCount.setText(getString(R.string.ap_No_) + alreadyCallCount + "/" + allCallCount + getString(R.string.ap_counts_of_dial));
                    AutoPhoneUtils.showLog("dialing times:" + alreadyCallCount);
                } else {
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_stop_dial_already));
                    callState = false;
                }
            }
        });

        /*
        挂断电话线程
         */
        mEndThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    endCall = iPhoney.endCall();// 挂断电话
                    if (alreadyCallCount > allCallCount - 1) {// 判断打电话次数更改拨号状态
                        callState = false;
                    }
                    AutoPhoneUtils.showLog("if hang up successfully：" + endCall);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initCall() {
        //判断输入框是否有数据
        if (mPhoneNumEdit.getText().toString().length() != 0
                && mSpaceTimeEdit.getText().toString().length() != 0
                && mCallCountsEdit.getText().toString().length() != 0
                && mCallDurationTimeEdit.getText().toString().length() != 0
                && Integer.parseInt(mCallCountsEdit.getText().toString()) != 0) {
            phoneNum = mPhoneNumEdit.getText().toString().trim();

            //插入号码到数据库
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("number", phoneNum);
            if (!phoneNumberList.contains(phoneNum)) {
                db.insert("database", null, values);// 将手机号存入数据库
                Log.i(TAG, "insert");
                Log.i(TAG, "Num = " + phoneNum);
                phoneNumberList.add(0, phoneNum);
            } else {
                db.delete("database", "number = ?", new String[]{phoneNum});
                db.insert("database", null, values);
                phoneNumberList.remove(phoneNum);
                phoneNumberList.add(0, phoneNum);
            }
            initData();// 初始化号码
            values.clear();
            mPhoneNumEdit.setAdapter(phoneNumberAdapter);

            networkTypeList.clear();// 清空网络状态
            callRecordList.clear();// 清空通话数据
            AutoPhoneUtils.CheckSimCardState(AutoPhoneActivity.this);// Toast当前信号状态及运营商

            spaceTime = Integer.parseInt(mSpaceTimeEdit.getText().toString());//获取拨号间隔
            allCallCount = Integer.parseInt(mCallCountsEdit.getText().toString()); // 获取拨号次数
            durationTime = Integer.parseInt(mCallDurationTimeEdit.getText().toString()); // 获取通话时间

            mNumberProgressBar.setMax(allCallCount); // 设置进度条

            callState = true; // 开始设置拨号状态为true
            isGetReport = false;//开始设置获得报告为false
            isExportReport = false; //开始设置导出文件为false
            endCall = false;//设置正常挂断状态为false

            networkType = AutoPhoneUtils.getNetworkClass(AutoPhoneActivity.this, tm.getNetworkType());// 获取拨号前的信号状态
            networkTypeList.add(networkType);

            AutoPhoneUtils.call(AutoPhoneActivity.this, phoneNum);// 拨打电话
            alreadyCallCount = 1;
            AutoPhoneUtils.showLog("call beginning");
            AutoPhoneUtils.showLog("dialing times：" + alreadyCallCount);
            mNumberProgressBar.setProgress(alreadyCallCount); // 设置进度条
            mShowAlreadyCallCount.setText(getString(R.string.ap_No_) + alreadyCallCount + "/" + allCallCount + getString(R.string.ap_counts_of_dial));
        } else {
            AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_please_input_correct_para));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);//注销监听器
        AutoPhoneUtils.showLog("over");
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.ap_tips);
        alertDialog.setMessage(R.string.ap_is_exit_app_if_go_on);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        if (alreadyCallCount != 0) {
            alertDialog.setTitle(R.string.ap_warning);
            if (!isExportReport) {
                alertDialog.setMessage(R.string.ap_dont_export_file_go_on_or_not);
            }
            if (!isGetReport) {
                alertDialog.setMessage(R.string.ap_dont_get_report_if_exit_app);
            }
        }
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(R.string.ap_go_on, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AutoPhoneActivity.this.finish();
            }
        });
        alertDialog.setNegativeButton(R.string.ap_cancle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    /*
    电话监听器
     */
    PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // 注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            if (callState) {
                switch (state) {
                    // 挂断状态
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (allCallCount == alreadyCallCount) {
                            callState = false;
                        }
                        handler.removeCallbacks(mEndThread);
                        handler.postDelayed(mCallThread, spaceTime * 1000);
                        break;
                    // 接听状态
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (AutoPhoneUtils.isMobileNumber(phoneNum)) {
                            handler.postDelayed(mEndThread, durationTime * 1000 + WAITING_TIME_OF_DIAL_NOT_SERVICE_NUMBER);
                        } else {
                            handler.postDelayed(mEndThread, durationTime * 1000 + WAITING_TIME_OF_DIAL_SERVICE_NUMBER);
                        }
                        break;
                    // 响铃状态
                    case TelephonyManager.CALL_STATE_RINGING:
                        AutoPhoneUtils.showLog("ringing:incoming number" + incomingNumber);
                        // 输出来电号码
                        break;
                }
            } else {
                AutoPhoneUtils.showLog("listener is not running");
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_ASK_CALL_SETTINGS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_already_get_permission_of_dial));
                    AutoPhoneUtils.call(AutoPhoneActivity.this, phoneNum);
                } else {
                    mNumberProgressBar.setProgress(0);
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_already_refuse_permission_of_dial));
                    isExportReport = true;
                    callState = false;
                    mShowAlreadyCallCount.setText(R.string.ap_show_call_times);
                }
                break;
            case REQUEST_CODE_ASK_STORAGE_SETTINGS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_already_get_permission_of_export_file));
                    AutoPhoneUtils.exportFile(AutoPhoneActivity.this, callRecordList, exportFile);
                } else {
                    AutoPhoneUtils.showToast(AutoPhoneActivity.this, getString(R.string.ap_already_refuse_permission_of_export_file));
                    exportFile.setClickable(true);
                }
        }
    }

    /*
        获取通话数据
    */
    private void getCallInfo() {
        callRecordList.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, 20);
            return;
        }
        Cursor cursor = getContentResolver().query(Calls.CONTENT_URI,
                new String[]{Calls.DURATION, Calls.DATE},
                "type = 2",
                null,
                Calls.DEFAULT_SORT_ORDER);
        AutoPhoneActivity.this.startManagingCursor(cursor);
        assert cursor != null;
        boolean hasRecord = cursor.moveToFirst();
        int count = 0;
        while (hasRecord) {
            int duration = cursor.getInt(cursor.getColumnIndex(Calls.DURATION));
            long startLong = cursor.getLong(cursor.getColumnIndex(Calls.DATE));
            Date startDate = new Date(startLong);
            long endLong = startDate.getTime() + duration * 1000;
            String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate);
            String endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(endLong));

            CallRecord callRecord = null;
            callRecord = new CallRecord(
                    String.valueOf(alreadyCallCount - count),
                    startTime,
                    endTime,
                    String.valueOf(duration),
                    networkTypeList.get(alreadyCallCount - count - 1),
                    duration > 0 ? "pass" : "fail");

            count++;
            callRecordList.add(callRecord);
            hasRecord = cursor.moveToNext();
            if (count == alreadyCallCount) {
                Collections.reverse(callRecordList);
                break;
            }
        }
    }

    private void initData() {
        phoneNumberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, phoneNumberList);
    }

    @Override
    public void onProgressChange(int current, int max) {

    }
}
