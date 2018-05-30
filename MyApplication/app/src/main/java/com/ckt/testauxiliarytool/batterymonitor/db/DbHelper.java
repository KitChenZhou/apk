package com.ckt.testauxiliarytool.batterymonitor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/11/23
 * <br/>TODO:
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "battery_infos.db";
    public static final int VERSION = 1;
    public static final String CREATE_DB = "create table battery_info(id integer primary key autoincrement," +
            "currentMillis long," +
            "level int," +
            "voltage text," +
            "temperature float," +
            "chargeCurrent text," +
            "health text," +
            "status text," +
            "plugged text)";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
