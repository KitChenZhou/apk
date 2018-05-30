package com.ckt.testauxiliarytool.cameratest.capturetask.task;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ckt.testauxiliarytool.cameratest.capturetask.api.ICameraApi;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.cameratest.capturetask.Params;

import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by asahi on 2017/11/23.
 */

public abstract class CaptureTask extends Thread {

    protected Handler mHandler;

    public ICameraApi mCamApi;
    public String taskName;

    private String[] mKeys = new String[]{
            null,
            ConstVar.CAPTURE_TASK_CURCNT_CFBS,
            ConstVar.CAPTURE_TASK_CURCNT_CIDB,
            ConstVar.CAPTURE_TASK_CURCNT_CRS
    };

    public int currentCount, totalCount;

    /**
     * Task ID.
     */
    public int taskId;

    public String getCurKey(){
        return mKeys[taskId];
    }

    public void sendMsg(int msgId){
        sendMsg(msgId, new Bundle());
    }

    public void sendMsg(int msgId, String info){
        Bundle bundle = new Bundle();
        bundle.putString("info", info);
        sendMsg(msgId, bundle);
    }

    private void sendMsg(int msgId, int index){
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        sendMsg(msgId, bundle);
    }

    private void sendMsg(int msgId, Bundle bundle){
        Message msg = new Message();
        msg.what = msgId;
        msg.setData(bundle);
        mEmitter.onNext(msg);
    }

    public String produceCountTip() {
        StringBuffer sb = new StringBuffer("目前次数为：【");
        sb.append(currentCount);
        sb.append("】\n总的次数为：【");
        sb.append(totalCount);
        sb.append("】");
        return sb.toString();
    }

    public void refreshCurrentAndTotalCount(){
        currentCount = Params.get(getCurKey(),ConstVar.CAPTURE_TASK_CURCNT_DEFAULT);
        totalCount = Params.get(ConstVar.CAPTURE_TASK_TOTCNT,ConstVar.CAPTURE_TASK_TOTCNT_DEFAULT);
    }

    public void switchCamera(){
        sendMsg(ConstVar.CT_MSG_TASK_SWITCH_CAM);
    }

    public void initCRSCam(){
        sendMsg(ConstVar.CT_MSG_TASK_CRS_INIT);
    }

    public void reopenFragment(){
        sendMsg(ConstVar.CT_MSG_TASK_REOPEN_FRAGMENT);
    }

    public void updateTip(String tip){
        sendMsg(ConstVar.CT_MSG_TASK_UPDATE_TIP, tip);
    }

    public void taskBegin(){
        sendMsg(ConstVar.CT_MSG_TASK_BEGIN, taskId);
    }

    public void taskComplete(){
        sendMsg(ConstVar.CT_MSG_TASK_COMPLETE, taskId);
    }

    public void taskNotStart(){
        taskFinished();
        sendMsg(ConstVar.CT_MSG_TASK_NOT_START, taskId);
    }

    /**
     * Called before task's executing to do some preparations for coming task.
     */
    public void onTaskExecuted(){
        Params.set(ConstVar.CAPTURE_TASK_STATUS, taskId);
        //sendMsg(ConstVar.CT_MSG_TASK_UPDATE_TIP, produceCountTip());

        refreshCurrentAndTotalCount();
        updateTip(produceCountTip());
        taskBegin();
    }

    /**
     * Called after task's executing to do some finish work for ended task.
     */
    public void taskFinished(){
        Params.set(ConstVar.CAPTURE_TASK_STATUS, ConstVar.CAPTURE_TASK_NONE);
        sendMsg(ConstVar.CT_MSG_TASK_UPDATE_TIP, "");
    }

    /**
     * Do works for the special task when Fragment called onResume().
     */
    public abstract void onResume();

    /**
     * Do works for the special task when Fragment called onPause().
     */
    public void onPause(){
        if(mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
        interrupt();
    }

    /**
     * Reset the current task environment.
     */
    public void reset(){
        if(mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
        interrupt();
    }

    private Observer<Message> mObserver;
    public Disposable mDisposable;
    private ObservableEmitter<Message> mEmitter;

    public void setEmitter(ObservableEmitter<Message> emitter){
        mEmitter = emitter;
    }

    public Observer createObserver(){
        mObserver = new Observer<Message>(){

            @Override
            public void onSubscribe(Disposable disposable) {
                mDisposable = disposable;
            }

            @Override
            public void onNext(Message message) {
                //Log.e("TAG", Thread.currentThread().getName());
                mHandler.handleMessage(message);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("CT_Observer", throwable.toString());
            }

            @Override
            public void onComplete() {}
        };
        return mObserver;
    }

    /**
     * works run on the sub thread.
     */
    public abstract void run();
}
