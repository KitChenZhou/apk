package com.ckt.testauxiliarytool.cameratest.slrc.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.slrc.CameraBaseActivity;

/**
 * Created by Cc on 2017/8/21.
 * <p>
 * Shows OK/Cancel confirmation dialog about cancel activity.
 */

public class CloseMessageDialog extends DialogFragment {

    public static CloseMessageDialog newInstance() {
        return new CloseMessageDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CameraBaseActivity activity = (CameraBaseActivity) getActivity();

        final AlertDialog alertDialog = new AlertDialog.Builder(activity).setTitle(R.string
                .ct_quit_dialog_title).setMessage(R.string.ct_quit_dialog_message).setPositiveButton(R
                .string.ct_quit, null).setNegativeButton(R.string.ct_cancel, null).create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Button positionButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activity.whetherCanBeClosed()) {
                            activity.finish();
                        } else {
                            alertDialog.setMessage(getString(R.string.ct_quit_dialog_message_changing));
                        }
                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

}
