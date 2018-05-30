package com.ckt.testauxiliarytool.sensortest.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ckt.testauxiliarytool.sensortest.bean.HSensor;
import com.ckt.testauxiliarytool.sensortest.bean.MSensor;

import java.util.ArrayList;
import java.util.List;

import static com.ckt.testauxiliarytool.sensortest.db.SensorDbSchema.SensorTable;

/**
 * Created by D22434 on 2017/8/23.
 */

public class SensorLab {
    @SuppressLint("StaticFieldLeak")
    private static SensorLab mSensorLab;

    private Context mContext;
    private SQLiteDatabase mDataBase;

    public static SensorLab get(Context context) {
        if (mSensorLab == null) {
            mSensorLab = new SensorLab(context);
        }
        return mSensorLab;
    }

    private SensorLab(Context context) {
        mContext = context.getApplicationContext();
        mDataBase = DBHelper.getInstance(mContext).getWritableDatabase();
    }


    /**
     * 插入HSensor
     *
     * @param sensor
     */
    public void addHRecord(HSensor sensor) {
        ContentValues values = new ContentValues();
        values.put(SensorTable.Cols.TYPE, sensor.getType() + "");
        values.put(SensorTable.Cols.FIELD, sensor.getStatus());
        values.put(SensorTable.Cols.VALUE, sensor.getInterval() + "");
        mDataBase.insert(SensorTable.MHSENSOR, null, values);
    }

    /**
     * 插入MSensor
     *
     * @param sensor
     */
    public void addMRecord(MSensor sensor) {
        ContentValues values = new ContentValues();
        values.put(SensorTable.Cols.TYPE, sensor.getType() + "");
        values.put(SensorTable.Cols.FIELD, sensor.getAngle() + "");
        values.put(SensorTable.Cols.VALUE, sensor.getDeviation() + "");
        mDataBase.insert(SensorTable.MHSENSOR, null, values);
    }

    /**
     * 获取
     */
    public List<HSensor> getHRecords(int type) {
        List<HSensor> hSensors = new ArrayList<>();
        SensorCursorWrapper cursor = querySensors(SensorTable.Cols.TYPE + " = ?",
                new String[]{type + ""});
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                hSensors.add(cursor.getHSensor());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return hSensors;
    }

    /**
     * 获取MSensor的数据
     *
     * @param type
     * @return
     */
    public List<MSensor> getMRecords(int type) {
        List<MSensor> mSensors = new ArrayList<>();
        SensorCursorWrapper cursor = querySensors(SensorTable.Cols.TYPE + " = ?",
                new String[]{type + ""});
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mSensors.add(cursor.getMSensor());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return mSensors;
    }

    /**
     * 删除记录
     */
    public void delete(int type) {
        mDataBase.delete(SensorTable.MHSENSOR, SensorTable.Cols.TYPE + " = ?",
                new String[]{type + ""});
    }

    public void close() {
//        if (mDataBase!=null){
//            mDataBase.close();
//        }
    }

    /**
     * 使用cursor封装方法
     *
     * @param whereClause
     * @param whereArgs
     * @return
     */
    private SensorCursorWrapper querySensors(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                SensorTable.MHSENSOR,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                "_id desc"
        );
        return new SensorCursorWrapper(cursor);
    }


}
