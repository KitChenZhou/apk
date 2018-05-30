package com.ckt.testauxiliarytool.autosleepwake;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.autosleepwake.fragment.LogFileListFragment;
import com.ckt.testauxiliarytool.autosleepwake.fragment.LogInfoFragment;
import com.ckt.testauxiliarytool.autosleepwake.interfaces.Constants;

public class ResultActivity extends AppCompatActivity implements LogFileListFragment.OnItemClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asw_activity_result);
        // 默认加载显示Log列表的Fragment
        LogFileListFragment logFileListFragment = new LogFileListFragment();
        logFileListFragment.setOnItemClickListener(this);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_container, logFileListFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(String filePath) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FILE_PATH, filePath);
        LogInfoFragment logInfoFragment = new LogInfoFragment();
        logInfoFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .addToBackStack("LogFileListFragment")
                .replace(R.id.fl_container, logInfoFragment);
        fragmentTransaction.commit();
    }
}
