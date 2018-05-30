package com.ckt.testauxiliarytool.cameratest.slrc.fragment;

import android.support.annotation.CallSuper;

import com.ckt.testauxiliarytool.cameratest.common.AutoFitTextureView;
import com.ckt.testauxiliarytool.cameratest.common.SoundPlayer;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IManageCamera;
import com.ckt.testauxiliarytool.utils.BuildUtil;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

/**
 * Created by Cc on 2017/11/21.
 */

public abstract class CameraFragment extends BaseFragment {

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    AutoFitTextureView mTextureView;

    IManageCamera mManageCamera;

    @Override
    public void onResume() {
        super.onResume();
        mManageCamera.onFragmentResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mManageCamera.onFragmentPause();
    }

    @Override
    @CallSuper
    void initData() {

        int useApiLevel = SharedPrefsUtil.name(IConstValue.CAMERA_PREFERENCE, getContext())
                .getInt(IConstValue.KEY_USE_API, IConstValue.VALUE_USE_API_DEFAULT);

        if (BuildUtil.isUseCamera2() && IConstValue.VALUE_USE_API_2 == useApiLevel) {
            initLOrLater();
        } else {
            initBeforeL();
        }
    }

    @Override
    @CallSuper
    void recycleObject() {
        SoundPlayer.release();
    }

    public IManageCamera getManageCamera() {
        return mManageCamera;
    }

    public boolean isPreview() {
        return mManageCamera != null && mManageCamera.isPreview();
    }

    abstract void initLOrLater();

    abstract void initBeforeL();
}
