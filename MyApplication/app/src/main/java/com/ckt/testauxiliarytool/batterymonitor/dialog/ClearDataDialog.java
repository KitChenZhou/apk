package com.ckt.testauxiliarytool.batterymonitor.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.batterymonitor.db.InfoDaoImpl;

/**
 * 清空数据库的对话框
 */
public class ClearDataDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_clear_data_message)
                .setNegativeButton(R.string.dialog_negative_btn_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.dialog_positive_btn_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Realm realm = Realm.getDefaultInstance();
//                        realm.beginTransaction();
//                        realm.deleteAll();
//                        realm.commitTransaction();
                        InfoDaoImpl.getInstance().deleteAll();
                    }
                });
        return builder.create();
    }
}
