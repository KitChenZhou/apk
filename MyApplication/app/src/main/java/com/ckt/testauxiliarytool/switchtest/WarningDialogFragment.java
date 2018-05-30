package com.ckt.testauxiliarytool.switchtest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.ckt.testauxiliarytool.R;

/**
 * Created by wgp on 2017/8/21.
 * 警告对话框，当用户输入不当时候弹出用于警告信息
 */

public class WarningDialogFragment extends DialogFragment {
    private static final String TAG = "WarningDialogFragment";
    private AlertDialog mAlertDialog;

    public static WarningDialogFragment newInstance() {
        WarningDialogFragment warningDialogFragment = new WarningDialogFragment();
        return warningDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: ");
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.warning_dialog_fragment, null);
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Warning")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .setNegativeButton("Cancle", null)
                .create();
        mAlertDialog.setCanceledOnTouchOutside(true);//设置点击空白处对话框消失
        return mAlertDialog;
    }

    /**
     * 防止因用户点击过快Dialog多次弹出
     *
     * @param fragmentManager fragment管理者
     * @param activity        Activity上下文
     * @return
     */
    public static WarningDialogFragment showDialog(FragmentManager fragmentManager, FragmentActivity activity) {
        WarningDialogFragment warningDialogFragment =
                (WarningDialogFragment) fragmentManager.findFragmentByTag(TAG);
        if (null == warningDialogFragment) {
            warningDialogFragment = newInstance();
        }

        if (!activity.isFinishing()
                && null != warningDialogFragment
                && !warningDialogFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(warningDialogFragment, TAG)
                    .commitAllowingStateLoss();
        }

        return warningDialogFragment;
    }
}
