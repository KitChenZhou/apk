package com.ckt.testauxiliarytool.sensortest;

import android.content.DialogInterface;

/**
 * Created by D22432 on 2017/8/30.
 */

public class ChoiceOnClickListener implements DialogInterface.OnClickListener{
    private int which = 0;
    @Override
    public void onClick(DialogInterface dialog, int which) {
        this.which=which;
    }
    public int getWhich(){
        return which;
    }
}
