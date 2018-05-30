package com.ckt.testauxiliarytool.tp.recorder;

import android.view.View;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/9/19
 * TODO: 录屏状态控制
 */

public interface IOnRecordStateControlListener {
    /**
     * 开始录屏时
     */
    void startRecordScreen();

    /**
     * 结束录屏时
     */
    void stopRecordScreen(View view);

    /**
     * 通知录屏功能可用状态
     */
    void notifyEnableState(boolean enabled);

    /**
     * 是否正在录屏
     * @return  boolean
     */
    boolean isRecording();

    /**
     * 是否可以录屏
     * @return boolean
     */
    boolean isCanRecord();

    /**
     * 获取录屏文件的绝对路径
     */
    String getStoreFileAbsolutePath();
}
