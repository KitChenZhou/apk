package com.ckt.testauxiliarytool.cameratest.capturetask.task;

import android.os.Handler;
import android.os.Message;

import com.ckt.testauxiliarytool.cameratest.common.ConstVar;

/**
 * Created by asahi on 2017/11/23.
 */

public class CaptureTaskHandler extends Handler {

    private CaptureTaskUIModifier mModifier;

    public CaptureTaskHandler(CaptureTaskUIModifier mModifier){
        this.mModifier = mModifier;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){

            // task begins
            case ConstVar.CT_MSG_TASK_BEGIN:{
                int index = msg.getData().getInt("index");
                mModifier.markTaskBtnStart(index);
                break;
            }

            // task completed
            case ConstVar.CT_MSG_TASK_COMPLETE:{
                int index = msg.getData().getInt("index");
                mModifier.markTaskComplete(index);
                break;
            }

            // task completed and returns to not start state
            case ConstVar.CT_MSG_TASK_NOT_START:{
                int index = msg.getData().getInt("index");
                mModifier.markTaskNotStart(index);
                break;
            }

            // 初始化textureview的监听事件。
            case ConstVar.CT_MSG_TASK_CRS_INIT:{
                mModifier.initCRSCamera();
                break;
            }

            case ConstVar.CT_MSG_TASK_UPDATE_TIP:{
                // to be added.
                String info = (String) msg.getData().get("info");
                if (info != null){
                    mModifier.changeTip(info);
                }
                break;
            }

            // show info of pic file on fragment.
            case ConstVar.CT_MSG_TASK_IMG_INFO:{
                StringBuilder sb = new StringBuilder("文件名 : ");
                sb.append(msg.getData().get("fileName").toString());
                sb.append("\n");
                sb.append("所在目录: ");
                sb.append(msg.getData().get("fileDir").toString().replace("\n", ""));
                mModifier.changeTip(sb.toString());
                break;
            }

            // switch camera between front camera and back camera
            case ConstVar.CT_MSG_TASK_SWITCH_CAM:{
                mModifier.switchCamera();
                break;
            }

            // restart fragment
            case ConstVar.CT_MSG_TASK_REOPEN_FRAGMENT:{
                mModifier.restartFragment();
                break;
            }
        }
    }
}
