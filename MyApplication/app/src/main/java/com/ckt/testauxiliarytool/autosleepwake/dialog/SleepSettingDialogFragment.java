package com.ckt.testauxiliarytool.autosleepwake.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ckt.testauxiliarytool.R;


// 设置手机本身休眠时间的对话框
public class SleepSettingDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_setting_title)
                .setMessage(R.string.dialog_sleep_setting_message)
                .setPositiveButton(R.string.dialog_setting_pos_btn_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.dialog_setting_neg_btn_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
