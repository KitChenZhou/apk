package com.ckt.testauxiliarytool.sensortest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.ckt.testauxiliarytool.sensortest.db.SensorDbSchema.SensorTable;

/**
 * Created by D22434 on 2017/8/22.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DB_NAME = "sensor.db";
    private static DBHelper instance;

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SensorTable.MHSENSOR + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SensorTable.Cols.TYPE + "  , "
                + SensorTable.Cols.FIELD + " , "
                + SensorTable.Cols.VALUE + " )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}

