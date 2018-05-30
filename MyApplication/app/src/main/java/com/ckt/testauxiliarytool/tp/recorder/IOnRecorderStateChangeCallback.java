package com.ckt.testauxiliarytool.tp.recorder;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/9/13
 * TODO: 录屏状态改变回调
 */

public interface IOnRecorderStateChangeCallback {
    /**
     * 当开始录屏时调用
     * <br/>called in WorkerThread
     */
    void onRecordStart();

    /**
     * 当有数据更新时调用
     * <br/>called in WorkerThread
     */
    void onRecordUpdate(String time);

    /**
     * 结束录屏时调用
     * @param error 是否停止出错
     */
    void onRecordStop(boolean error);
}
