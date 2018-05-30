package com.ckt.testauxiliarytool.sensortest.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.ChoiceOnClickListener;
import com.ckt.testauxiliarytool.sensortest.bean.CommendResult;
import com.ckt.testauxiliarytool.utils.ExcelUtils;
import com.ckt.testauxiliarytool.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends BaseActivity {

    private static final String TAG = "GSSonsorActivity";
    //选择执行的次数
    private int numberOfTimes;
    //开始按钮
    private Button mStartButton;
    //结束按钮
    private Button mEndButton;

    //文本的显示
    private TextView mTextView;

    //设备横竖屏转换时间实时记录
    private long[] data = new long[1001];

    //保存excel的数据
    List<CommendResult> result = new ArrayList<CommendResult>();


    //运行次数的选择
    final String[] times = {"1", "10", "50"};

    //计数器
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.st_activity_album);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mTextView = (TextView) findViewById(R.id.message);
        mStartButton = (Button) findViewById(R.id.start_button);
        mEndButton = (Button) findViewById(R.id.end_button);
    }

    private void initData() {
        mTextView.setText(String.valueOf(count));
    }

    private void initListener() {
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice();
            }
        });
        mEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfTimes == 0) {
                    Toast.makeText(AlbumActivity.this, "请先点击开始按钮", Toast.LENGTH_SHORT).show();
                } else if (count < numberOfTimes) {
                    Toast.makeText(AlbumActivity.this, "请继续旋转手机，次数还不够。", Toast.LENGTH_SHORT).show();
                } else {
                    long sum = 0L;
                    int times = 1;
                    for (int i = numberOfTimes; i > 0; i--) {
                        int a = (int) (data[i] - data[i - 1]);
                        CommendResult commendResult = new CommendResult();
                        commendResult.setTimes(times);
                        commendResult.setTime(a);
                        result.add(commendResult);
                        sum += data[i] - data[i - 1];
                        times++;
                    }
                    mTextView.setText("响应时间: \n" + String.valueOf(sum / numberOfTimes) + "ms");
                }
            }
        });
    }

    public void choice() {
        final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
        AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this)
                .setTitle("please select times for test:")
                .setSingleChoiceItems(times, 0, choiceListener);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int choiceWhich = choiceListener.getWhich();
                count = 0;
                mTextView.setText(String.valueOf(count));
                if (choiceWhich == 0) {
                    numberOfTimes = 1;
                } else if (choiceWhich == 1) {
                    numberOfTimes = 10;
                } else {
                    numberOfTimes = 50;
                }
            }
        });
        builder.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (count <= numberOfTimes) {
            data[count] = System.currentTimeMillis();
            Log.d(TAG, "data[" + count + "]" + data[count]);
            mTextView.setText(String.valueOf(count));
            count++;
        } else {
            Toast.makeText(this, "请点击结束按钮来获取结果", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gsensor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveExcel:
                if (result.size() == 0) {
                    Toast.makeText(AlbumActivity.this, "测试还未开始或未完成，请先测试再保存！", Toast.LENGTH_SHORT).show();
                } else {
                    PermissionUtils.requestPermissions(this, 0, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, new PermissionUtils.OnPermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            try {
                                String exportInfo = ExcelUtils.createExcelForGSensor("Gsensor_Album", result);
                                Toast.makeText(AlbumActivity.this, exportInfo, Toast.LENGTH_LONG).show();
                                numberOfTimes = 0;
                                result.clear();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(AlbumActivity.this, "导出失败！", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionDenied(String[] deniedPermissions) {
                            Toast.makeText(AlbumActivity.this, "读写外部存储权限被拒绝！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
