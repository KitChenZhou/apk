package com.ckt.testauxiliarytool.cameratest.capturetask.task;

import android.os.Handler;
import android.os.Message;

import com.ckt.testauxiliarytool.cameratest.capturetask.api.ICameraApi;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.cameratest.capturetask.Params;
import com.ckt.testauxiliarytool.cameratest.capturetask.Sleeper;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by asahi on 2017/11/23.
 */

public class CaptureRecordSwitcher extends CaptureTask {

    public CaptureRecordSwitcher(
            int taskId, String taskName, ICameraApi capi, Handler mHandler) {
        super.taskId = taskId;
        super.taskName = taskName;
        super.mCamApi = capi;
        super.mHandler = mHandler;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void run() {
        mObservable.subscribe(createObserver());
    }

    private Observable<Message> mObservable = Observable.create(new ObservableOnSubscribe<Message>() {
        @Override
        public void subscribe(ObservableEmitter<Message> observableEmitter) throws Exception {
            setEmitter(observableEmitter);
            onTaskExecuted();

            if (mCamApi != null){
                mCamApi.setReqName(taskName);
            } else {
                return;
            }

            if (currentCount <= totalCount){
                Sleeper.sleep(ConstVar.SLEEP_TIME_DEFAULT);
                //由摄像模式的预览转向摄像模式的预览。
                updateTip("录像模式");
                if (mCamApi != null)
                    mCamApi.change2Record();
                else
                    return;
                Sleeper.sleep(ConstVar.SLEEP_TIME_DEFAULT);

                if (mCamApi != null)
                    mCamApi.closeCamera();
                else
                    return;
                updateTip("拍照模式");

                initCRSCam();

                Sleeper.sleep(ConstVar.SLEEP_TIME_DEFAULT);

                if (mDisposable != null && !mDisposable.isDisposed()) {

                    Params.set(getCurKey(),++ currentCount);
                    //updateTip(produceCountTip());
                    //totalCount = Params.get(ConstVar.CAPTURE_TASK_TOTCNT,ConstVar.CAPTURE_TASK_TOTCNT_DEFAULT);
                    //重启活动
                    reopenFragment();
                }
            } else {
                /**
                 * 测试结束，设置标志位，并修改Button样式
                 */
                taskComplete();

                // 震动提示
                //TaskFinishedVibrator.vibrate(getContext().getApplicationContext());
                Params.set(ConstVar.CAPTURE_TASK_STATUS, ConstVar.CAPTURE_TASK_NONE);

                /**
                 * 休眠5s
                 */
                Sleeper.sleep(ConstVar.SLEEP_TIME_FINISH_NOTIFICATION);

                /**
                 * 恢复到没有开始之前的样式。
                 */
                taskNotStart();
            }
        }
    }).observeOn(AndroidSchedulers.mainThread());
}
