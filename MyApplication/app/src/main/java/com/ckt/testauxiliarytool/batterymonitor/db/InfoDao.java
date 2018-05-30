package com.ckt.testauxiliarytool.batterymonitor.db;

import com.ckt.testauxiliarytool.batterymonitor.bean.BatteryInfo;

import java.util.List;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/11/23
 * <br/>TODO:
 */

public interface InfoDao {
    List<BatteryInfo> queryAll();

    boolean insert(BatteryInfo batteryInfo);

    boolean deleteAll();
}
