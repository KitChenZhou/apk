package com.ckt.testauxiliarytool.batterymonitor;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.batterymonitor.db.InfoDaoImpl;
import com.ckt.testauxiliarytool.batterymonitor.fragment.MainFragment;

public class BatteryMonitorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 完成数据库的初始化
        InfoDaoImpl.init(this);
        //透明状态栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.bm_activity_battery_monitor);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_container_main, new MainFragment());
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InfoDaoImpl.release();
    }
}