package com.ckt.testauxiliarytool.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.autophone.AutoPhoneActivity;
import com.ckt.testauxiliarytool.autophone.CallRecord;
import com.ckt.testauxiliarytool.utils.MyConstants;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;

public class AutoPhoneUtils {

    private static final String TAG = "AutoPhoneActivity";
    private static Toast toast = null;
    private static final int REQUEST_CODE_ASK_CALL_SETTINGS = 2;
    private static final int REQUEST_CODE_ASK_STORAGE_SETTINGS = 3;

    private static final int NETWORK_TYPE_GPRS = 1;
    private static final int NETWORK_TYPE_EDGE = 2;
    private static final int NETWORK_TYPE_UMTS = 3;
    private static final int NETWORK_TYPE_CDMA = 4;
    private static final int NETWORK_TYPE_EVDO_0 = 5;
    private static final int NETWORK_TYPE_EVDO_A = 6;
    private static final int NETWORK_TYPE_1xRTT = 7;
    private static final int NETWORK_TYPE_HSDPA = 8;
    private static final int NETWORK_TYPE_HSUPA = 9;
    private static final int NETWORK_TYPE_HSPA = 10;
    private static final int NETWORK_TYPE_IDEN = 11;
    private static final int NETWORK_TYPE_EVDO_B = 12;
    private static final int NETWORK_TYPE_LTE = 13;
    private static final int NETWORK_TYPE_EHRPD = 14;
    private static final int NETWORK_TYPE_HSPAP = 15;

    /**
     * Toast
     *
     * @param context 上下文
     * @param content 内容
     */
    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    /**
     * 拨打电话
     *
     * @param context   上下文
     * @param mPhoneNum 电话号码
     */
    public static void call(Context context, String mPhoneNum) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE_ASK_CALL_SETTINGS);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNum));
            context.startActivity(intent);
        }
    }

    /**
     * Log
     *
     * @param content 上下文
     */
    public static void showLog(String content) {
        Log.i(TAG, content);
    }

    /**
     * 导出EXCEL文件
     *
     * @param context        上下文
     * @param callRecordList 需要导出的数据
     */
    public static void exportFile(final Context context,
                                  final ArrayList<CallRecord> callRecordList,
                                  final Button btn) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_STORAGE_SETTINGS);
        } else {
            String PATH = MyConstants.getStorageRootDir(context) + File.separator + MyConstants.AUTOPHONE_DIR
                    + File.separator;
            File file = new File(PATH);
            if(!file.exists()){
                file.mkdir();
            }
            final String fileName = PATH + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
            Log.i(TAG, "fileName = " + fileName);

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(context.getString(R.string.ap_tips));
            dialog.setMessage(context.getString(R.string.ap_is_export_file_to) + fileName + context.getString(R.string.ap_if_go_on));
            dialog.setCancelable(false);
            dialog.setPositiveButton(context.getString(R.string.ap_go_on), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    File file = new File(fileName);
                    ExcelUtils.createExcel(file);//  创建表格
                    ExcelUtils.writeToExcel(file,callRecordList);//写数据到表格
                    AutoPhoneActivity.isExportReport = true;
                    showToast(context, context.getString(R.string.ap_export_successfully));
                    btn.setClickable(true);
                }

            });
            dialog.setNegativeButton(context.getString(R.string.ap_cancle), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AutoPhoneActivity.isExportReport = false;
                    btn.setClickable(true);
                }
            });
            dialog.show();
        }
    }

    /**
     * 获取报告
     *
     * @param context        上下文
     * @param phoneNumber    电话号码
     * @param callRecordList 通话记录List
     */
    public static void getReport(Context context, String phoneNumber, ArrayList<CallRecord> callRecordList, final Button btn) {
        int validCall = 0; //成功拨打次数
        for (int i = 0; i < callRecordList.size(); i++) {
            if (Integer.parseInt(callRecordList.get(i).getDuration()) > 0) {
                validCall++;
            }
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(context.getString(R.string.ap_call_information));
        if (isMobileNumber(phoneNumber)) {
            dialog.setMessage(context.getString(R.string.ap_check_test_machine_call_information));
        } else {
            dialog.setMessage(context.getString(R.string.ap_successful_dial_counts) + validCall + "\n"
                    + context.getString(R.string.ap_failure_dial_counts) + (callRecordList.size() - validCall) + "\n"
                    + context.getString(R.string.ap_total_dial_counts) + callRecordList.size() + "\n"
                    + context.getString(R.string.ap_rate_of_dial_successfully) + (validCall * 1.0) / callRecordList.size() * 100 + "%");
        }
        dialog.setCancelable(false);
        dialog.setPositiveButton(context.getString(R.string.ap_sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                btn.setClickable(true);
            }
        });
        dialog.setNegativeButton(context.getString(R.string.ap_cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                btn.setClickable(true);
            }
        });
        dialog.show();
    }

    /**
     * 检查SIM卡状态
     *
     * @param context 上下文
     */
    public static void CheckSimCardState(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String imsi = tm.getSimOperator();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
                AutoPhoneUtils.showToast(context, context.getString(R.string.ap_china_mobile) + AutoPhoneUtils.getNetworkClass(context, tm.getNetworkType()));
            } else if (imsi.startsWith("46001")) {
                AutoPhoneUtils.showToast(context, context.getString(R.string.ap_china_unicom) + AutoPhoneUtils.getNetworkClass(context, tm.getNetworkType()));
            } else if (imsi.startsWith("46003")) {
                //中国电信
                AutoPhoneUtils.showToast(context, context.getString(R.string.ap_china_telecom) + AutoPhoneUtils.getNetworkClass(context, tm.getNetworkType()));
            } else {
                AutoPhoneUtils.showToast(context, context.getString(R.string.ap_sim_card_is_unavailable));
            }
        } else {
            AutoPhoneUtils.showToast(context, context.getString(R.string.ap_sim_card_is_unavailable));
        }
    }

    /**
     * 获取ITelephony实例
     *
     * @param context 上下文
     * @return ITelephony实例
     */
    public static ITelephony getITelephony(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(TELEPHONY_SERVICE);
        Class<TelephonyManager> c = TelephonyManager.class;
        Method getITelephonyMethod = null;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null); // 获取声明的方法
            getITelephonyMethod.setAccessible(true);
        } catch (SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        ITelephony iTelephony;
        try {
            assert getITelephonyMethod != null;
            iTelephony = (ITelephony) getITelephonyMethod.invoke(
                    mTelephonyManager, (Object[]) null); // 获取实例
            return iTelephony;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前网络状态
     *
     * @param context     上下文
     * @param networkType 网络状态值
     * @return 网络状态
     */
    public static String getNetworkClass(Context context, int networkType) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.
                getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        String strSubTypeName = networkInfo.getSubtypeName();
        Log.i(TAG, "strSubTypeName :" + strSubTypeName);
        switch (networkType) {
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                return "2G";
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
                return "3G";
            case NETWORK_TYPE_LTE:
                return "4G";
            default:
                if (strSubTypeName.equalsIgnoreCase("TD-SCDMA")
                        || strSubTypeName.equalsIgnoreCase("WCDMA")
                        || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                    return "3G";
                }
                return "unknow";
        }
    }

    /**
     * 判断是否为手机号码
     *
     * @param phoneNumber 手机号码
     * @return true or false
     */
    public static boolean isMobileNumber(String phoneNumber) {
        Pattern p = Pattern.compile("^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17([0,1,6,7,]))|(18[0-2,5-9]))\\d{8}$");
        Matcher matcher = p.matcher(phoneNumber);
        return matcher.matches();
    }
}
