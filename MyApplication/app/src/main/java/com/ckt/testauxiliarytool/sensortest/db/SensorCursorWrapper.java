package com.ckt.testauxiliarytool.sensortest.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.ckt.testauxiliarytool.sensortest.bean.HSensor;
import com.ckt.testauxiliarytool.sensortest.bean.MSensor;

import static com.ckt.testauxiliarytool.sensortest.db.SensorDbSchema.SensorTable;

/**
 * Created by D22434 on 2017/7/24.
 */

public class SensorCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public SensorCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public MSensor getMSensor() {
        int type = Integer.parseInt(getString(getColumnIndex(SensorTable.Cols.TYPE)));
        int angle = getInt(getColumnIndex(SensorTable.Cols.FIELD));
        float deviation = getFloat(getColumnIndex(SensorTable.Cols.VALUE));
        return new MSensor(type, angle, deviation);
    }

    public HSensor getHSensor() {
        int type = Integer.parseInt(getString(getColumnIndex(SensorTable.Cols.TYPE)));
        String field = getString(getColumnIndex(SensorTable.Cols.FIELD));
        long value = getLong(getColumnIndex(SensorTable.Cols.VALUE));

        return new HSensor(type, field, value);
    }
}
