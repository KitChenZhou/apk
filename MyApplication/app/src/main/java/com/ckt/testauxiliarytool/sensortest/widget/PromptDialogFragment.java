package com.ckt.testauxiliarytool.sensortest.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by D22434 on 2017/8/29.
 */

public class PromptDialogFragment extends DialogFragment {

    private static String KEY = "prompt";
    private static String FLAG = "flag";

    public interface mCallBack {
        void confirm(int type);
    }

    public static PromptDialogFragment newInstance(String msg, int type) {

        Bundle args = new Bundle();
        args.putString(KEY, msg);
        args.putInt(FLAG, type);
        PromptDialogFragment fragment = new PromptDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setMessage(getArguments().getString(KEY));
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mCallBack callBack = (mCallBack) getActivity();
                callBack.confirm(getArguments().getInt(FLAG));
            }
        });
        builder.setNegativeButton("取消", null);
        return builder.create();
    }

}
