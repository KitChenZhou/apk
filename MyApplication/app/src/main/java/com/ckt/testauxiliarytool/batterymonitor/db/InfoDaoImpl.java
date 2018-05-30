package com.ckt.testauxiliarytool.batterymonitor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ckt.testauxiliarytool.batterymonitor.bean.BatteryInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/11/23
 * <br/>TODO:电池信息数据库操作类
 */

public class InfoDaoImpl implements InfoDao {
    private DbHelper helper;
    private static InfoDaoImpl sImpl;

    private InfoDaoImpl(Context context) {
        helper = new DbHelper(context);
    }

    public static void init(Context context) {
        if (sImpl == null) {
            sImpl = new InfoDaoImpl(context);
        }
    }

    public static InfoDaoImpl getInstance() {
        if (sImpl == null) {
            throw new NullPointerException("you should init the InfoDaoImpl");
        }
        return sImpl;
    }


    @Override
    public List<BatteryInfo> queryAll() {
        if (helper == null) return null;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("battery_info", null, null, null, null, null, null);
        if (cursor != null) {
            List<BatteryInfo> batteryInfos = new ArrayList<>();
            while (cursor.moveToNext()) {
                long currentMillis = cursor.getLong(cursor.getColumnIndex("currentMillis"));
                int level = cursor.getInt(cursor.getColumnIndex("level"));
                String voltage = cursor.getString(cursor.getColumnIndex("voltage"));
                float temperature = cursor.getFloat(cursor.getColumnIndex("temperature"));
                String chargeCurrent = cursor.getString(cursor.getColumnIndex("chargeCurrent"));
                String health = cursor.getString(cursor.getColumnIndex("health"));
                String status = cursor.getString(cursor.getColumnIndex("status"));
                String plugged = cursor.getString(cursor.getColumnIndex("plugged"));
                BatteryInfo batteryInfo = new BatteryInfo(currentMillis, level, voltage, temperature, chargeCurrent, health, status, plugged);
                batteryInfos.add(batteryInfo);
            }
            cursor.close();
            db.close();
            return batteryInfos;
        }
        return null;
    }

    @Override
    public boolean insert(BatteryInfo batteryInfo) {
        if (helper == null) return false;
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("currentMillis", batteryInfo.getCurrentMillis());
        values.put("level", batteryInfo.getLevel());
        values.put("voltage", batteryInfo.getVoltage());
        values.put("temperature", batteryInfo.getTemperature());
        values.put("chargeCurrent", batteryInfo.getChargeCurrent());
        values.put("health", batteryInfo.getHealth());
        values.put("status", batteryInfo.getStatus());
        values.put("plugged", batteryInfo.getPlugged());
        long rawId = db.insert("battery_info", null, values);
        db.close();
        return rawId != -1;

    }

    @Override
    public boolean deleteAll() {
        if (helper == null) return false;
        SQLiteDatabase db = helper.getWritableDatabase();
        int raws = db.delete("battery_info", null, null);
        db.close();
        return raws > 0;
    }

    public static void release() {
        sImpl = null;
    }
}
