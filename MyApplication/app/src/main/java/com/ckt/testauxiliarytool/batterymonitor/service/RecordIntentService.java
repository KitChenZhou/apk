package com.ckt.testauxiliarytool.batterymonitor.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.Nullable;

import com.ckt.testauxiliarytool.batterymonitor.bean.BatteryInfo;
import com.ckt.testauxiliarytool.batterymonitor.db.InfoDaoImpl;
import com.ckt.testauxiliarytool.utils.LogUtil;

/**
 * 用来记录数据的Service
 */
public class RecordIntentService extends IntentService {

    public static final String TAG = RecordIntentService.class.getSimpleName();

    public RecordIntentService() {
        super("RecordIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        LogUtil.d(TAG,"RecordIntentService in onHandleIntent");
        if (intent == null) return;
        final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0); // 电量
        final int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0); // 电压
        final int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10; // 温度
        final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0); // 充电方式
        // 当前充电电流，微安变毫安，该方式总是0，也可用cat指令来查看文件节点来获取，但只是部分手机可用
        final int chargeCurrent = ((BatteryManager) getSystemService(BATTERY_SERVICE))
                .getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * 1000;
        final int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN); // 健康状态
        final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN); // 充放电状态
        // 写入数据
        BatteryInfo batteryInfo = new BatteryInfo();
        batteryInfo.setCurrentMillis(System.currentTimeMillis());
        batteryInfo.setLevel(level);
        batteryInfo.setVoltage(voltage + "mV");
        batteryInfo.setTemperature(temperature);
        batteryInfo.setPlugged(convertPluggedValue(plugged));
        batteryInfo.setChargeCurrent(chargeCurrent + "mA");
        batteryInfo.setHealth(convertHealthValue(health));
        batteryInfo.setStatus(convertStatusValue(status));
        InfoDaoImpl.getInstance().insert(batteryInfo);
    }

    /**
     * 供外部开启本服务
     *
     * @param context 上下文
     * @param intent  封装了电池数据的Intent
     */
    public static void start(Context context, Intent intent) {
        intent.setClass(context, RecordIntentService.class);
        context.startService(intent);
    }

    /**
     * 将健康码转换成字符串值
     *
     * @param health 健康码
     * @return 该健康码所代表的含义
     */
    private String convertHealthValue(int health) {
        String healthStr = "";
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                healthStr = "Cold";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthStr = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthStr = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthStr = "OverVoltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthStr = "OverHeat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                healthStr = "Unknown";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthStr = "Failure";
                break;
        }
        return healthStr;
    }

    /**
     * 将电池的状态码转换成对应的字符串值
     *
     * @param status 电池状态码
     * @return 该状态码所对应的电池状态
     */
    private String convertStatusValue(int status) {
        String statusStr = "";
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusStr = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusStr = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusStr = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusStr = "NotCharging";
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                statusStr = "Unknown";
                break;
        }
        return statusStr;
    }

    /**
     * 将电池的充电方式状态码转换成字符串值
     *
     * @param plugged 充电方式状态码
     * @return 充电状态字符串
     */
    private String convertPluggedValue(int plugged) {
        String pluggedStr = "";
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                pluggedStr = "AC";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                pluggedStr = "USB";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                pluggedStr = "WLS";
                break;
            case 0:
                pluggedStr = "Not";
                break;
        }
        return pluggedStr;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG,"RecordIntentService is onDestroy！");
    }
}
