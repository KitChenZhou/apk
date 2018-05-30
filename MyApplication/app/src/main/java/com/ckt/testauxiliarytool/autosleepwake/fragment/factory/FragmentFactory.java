package com.ckt.testauxiliarytool.autosleepwake.fragment.factory;


import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.ckt.testauxiliarytool.autosleepwake.fragment.RestartFragment;
import com.ckt.testauxiliarytool.autosleepwake.fragment.SleepWakeFragment;

public class FragmentFactory {

    private static  SparseArray<Fragment> fragmentSparseArray = new SparseArray<>();

    public static Fragment newFragment(int position){
        Fragment fragment = fragmentSparseArray.get(position);
        if (fragment == null){
            switch (position){
                case 0:
                    fragment = new SleepWakeFragment();
                    break;
                case 1:
                    fragment = new RestartFragment();
                    break;
            }
            fragmentSparseArray.put(position,fragment);
        }
        return fragment;
    }

}
