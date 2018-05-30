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

public class CaptureInDarkBright extends CaptureTask {

    public CaptureInDarkBright(
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

            if (currentCount <= totalCount) {
                refreshCurrentAndTotalCount();
                initCRSCam();
                /**
                 * 修改提示
                 */
                Sleeper.sleep(ConstVar.SLEEP_TIME_DEFAULT);
                //明处拍照
                if (mCamApi != null)
                    mCamApi.takePicture();
                else
                    return;
                Sleeper.sleep(ConstVar.SLEEP_TIME_DEFAULT);
                mCamApi.closeCamera();
                initCRSCam();
                //暗处拍照
                Sleeper.sleep(ConstVar.SLEEP_TIME_DEFAULT);
                if (mCamApi != null) {
                    mCamApi.takePicture();
                } else {
                    return;
                }
                Sleeper.sleep(ConstVar.SLEEP_TIME_DEFAULT);

                //Params.set(getCurKey(), ++currentCount);

                if (mDisposable != null && !mDisposable.isDisposed()) {
                    //写数据
                    Params.set(getCurKey(), ++currentCount);
                    //updateTip(produceCountTip());

                    //重启活动
                    reopenFragment();
                }
            } else {

                taskComplete();
                /**
                 * 测试结束，设置标志位，并修改Button样式
                 */

                Params.set(ConstVar.CAPTURE_TASK_STATUS,ConstVar.CAPTURE_TASK_NONE);

                // 震动提示
                //TaskFinishedVibrator.vibrate(getContext().getApplicationContext());
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
