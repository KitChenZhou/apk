package com.ckt.testauxiliarytool.autosleepwake.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ckt.testauxiliarytool.R;


/**
 * 没有日志信息可供显示对话框
 */
public class NotLogShowDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_setting_title)
                .setMessage(R.string.dialog_not_log_show_message)
                .setPositiveButton(R.string.dialog_pos_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
