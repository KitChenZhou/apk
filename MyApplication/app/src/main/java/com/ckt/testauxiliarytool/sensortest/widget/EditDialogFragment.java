package com.ckt.testauxiliarytool.sensortest.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

/**
 * Created by D22434 on 2017/8/29.
 */

public class EditDialogFragment extends DialogFragment {

    private static String MAX_INTERVAL = "maxInterval";
    private String[] COVER_STATUS = new String[]{"close", "open"};
    private String[] Mode = new String[]{"合盖", "开盖"};

    private int interval = 0;
    private int flag = 0;

    public interface mCallBack {
        //设置自定义最大响应时间
        void setMaxTime(int flag, int time);
    }

    public static EditDialogFragment newInstance(int flag) {
        Bundle args = new Bundle();
        args.putInt(MAX_INTERVAL, flag);
        EditDialogFragment fragment = new EditDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        flag = getArguments().getInt(MAX_INTERVAL);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.st_item_hall, null);
        final EditText editText = (EditText) view.findViewById(R.id.et);
        editText.setText(String.valueOf(SharedPrefsUtil.name("max_interval").getInt(COVER_STATUS[flag], 0)));
        editText.setSelection(editText.getText().length());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.mipmap.ic_launcher)
                .setTitle("自定义 " + Mode[flag] + " 最大响应时间")
                .setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = editText.getText().toString();
                if (!value.equals("")) {
                    interval = Integer.parseInt(value);
                }
                mCallBack callBack = (mCallBack) getActivity();
                callBack.setMaxTime(flag, interval);
            }
        });
        builder.setNegativeButton("取消", null);
        return builder.create();
    }

}
