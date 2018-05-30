package com.ckt.testauxiliarytool.cameratest.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.ckt.testauxiliarytool.R;

/**
 * Created by D22431 on 2017/9/29.
 */

public class BackConfirmDialog {
    private Activity mActivity = null;

    public BackConfirmDialog(Activity mActivity) {
        this.mActivity = mActivity;
    }

    DialogInterface.OnClickListener mDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int buttonId) {
            // TODO Auto-generated method stub
            switch (buttonId) {
                //"Confirm" button clicked
                case AlertDialog.BUTTON_POSITIVE:
                    //Finish activity
                    mActivity.finish();
                    break;
                //"Cancel" button clicked
                case AlertDialog.BUTTON_NEGATIVE:
                    //Do nothing
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Alert dialog
     */
    public void alertConfirmDialog() {
        AlertDialog.Builder mIsExit = new AlertDialog.Builder(mActivity);
        mIsExit.setMessage(mActivity.getResources().getString(R.string.ct_quit_dialog_title));
        mIsExit.setPositiveButton(mActivity.getResources().getString(R.string.ct_quit), mDialogClickListener);
        mIsExit.setNegativeButton(mActivity.getResources().getString(R.string.ct_cancel), mDialogClickListener);
        mIsExit.show();
    }
}
