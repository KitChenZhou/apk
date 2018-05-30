package com.ckt.testauxiliarytool.cameratest.capturetask;

import android.os.Message;

/**
 * Created by D22433 on 2017/9/1.
 */

public class MessageCreator {
    /**
     * 为了简化创建Message的过程
     * @param what
     * @return
     */
    public static Message create(int what){
        Message msg = new Message();
        msg.what = what;
        return msg;
    }
}
