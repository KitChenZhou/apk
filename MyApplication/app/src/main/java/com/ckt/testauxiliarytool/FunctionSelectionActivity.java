package com.ckt.testauxiliarytool;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.ckt.testauxiliarytool.adapter.FunctionListAdapter;
import com.ckt.testauxiliarytool.autophone.AutoPhoneActivity;
import com.ckt.testauxiliarytool.autosleepwake.ASWMainActivity;
import com.ckt.testauxiliarytool.batterymonitor.BatteryMonitorActivity;
import com.ckt.testauxiliarytool.cameratest.slrc.CameraActivity;
import com.ckt.testauxiliarytool.contentfill.FillToolActivity;
import com.ckt.testauxiliarytool.sensortest.activities.CompassActivity;
import com.ckt.testauxiliarytool.sensortest.activities.GSensorTestActivity;
import com.ckt.testauxiliarytool.sensortest.activities.GyroscopeTestActivity;
import com.ckt.testauxiliarytool.sensortest.activities.HallTestActivity;
import com.ckt.testauxiliarytool.sensortest.activities.LSensorTestActivity;
import com.ckt.testauxiliarytool.sensortest.activities.PSensorTestActivity;
import com.ckt.testauxiliarytool.switchtest.SwitchTestMainActivity;
import com.ckt.testauxiliarytool.tp.TPTestActivity;
import com.ckt.testauxiliarytool.utils.LogCatchUtil;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.webviewtest.WebViewTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionSelectionActivity extends BaseActivity {
    public static final String TAG = FunctionListAdapter.class.getSimpleName();

    private RecyclerView mFunctionListView;
    private Button mAutoTest;
    private FunctionListAdapter mAdapter;
    private List<String> mItems = new ArrayList<String>();
    private Class<?>[] mActivities = {
            ASWMainActivity.class, BatteryMonitorActivity.class, TPTestActivity.class,
            CompassActivity.class, HallTestActivity.class, GSensorTestActivity.class,
            GyroscopeTestActivity.class, LSensorTestActivity.class, PSensorTestActivity.class,
            FillToolActivity.class, AutoPhoneActivity.class, SwitchTestMainActivity.class, CameraActivity.class, WebViewTest.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		LogCatchUtil.getInstance().start(getBaseContext());
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "FunctionSelectionActivity onCreate");
        setContentView(R.layout.function_selection_activity);
        //baseSetContentView(R.layout.function_selection_activity);
        mFunctionListView = (RecyclerView) findViewById(R.id.functionlist);

        String[] functionArray = getResources().getStringArray(R.array.function_list);
        for (int i = 0; i < functionArray.length; i++) {
            LogUtil.d(TAG, "functionArray[" + i + "] = " + functionArray[i]);
        }
        mItems = Arrays.asList(functionArray);
        mAdapter = new FunctionListAdapter(getApplicationContext(), mItems);
        mAdapter.setOnItemClickListener(new FunctionListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                Intent it = new Intent(FunctionSelectionActivity.this, mActivities[pos]);
                startActivity(it);
            }
        });
        mFunctionListView.setHasFixedSize(true);
        mFunctionListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mFunctionListView.addItemDecoration(new FunctionListDecoration(this, LinearLayoutManager.VERTICAL));
        mFunctionListView.setAdapter(mAdapter);

/*暂时没用到
        mAutoTest = (Button) findViewById(R.id.autotest);
        mAutoTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(FunctionSelectionActivity.this, ASWMainActivity.class), 1);
            }
        });
*/

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);  //获取系统的传感器服务并创建实例

        List<Sensor> list = sm.getSensorList(Sensor.TYPE_ALL);  //获取传感器的集合
        for (Sensor sensor : list) {
        }

    }

    @Override
    protected void onStart() {
        LogUtil.d(TAG, "FunctionSelectionActivity onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        LogUtil.d(TAG, "FunctionSelectionActivity onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "FunctionSelectionActivity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, "FunctionSelectionActivity onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "FunctionSelectionActivity onDestroy");
		LogCatchUtil.getInstance().stop();
    }
}
