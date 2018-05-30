package com.ckt.testauxiliarytool.switchtest;

import android.os.Bundle;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;

/**
 * Created by D22395 on 2017/12/15.
 */

public class MoreTestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(MoreTestActivity.this, R.string.new_funtion_waiting, Toast.LENGTH_LONG).show();
        finish();
    }
}
