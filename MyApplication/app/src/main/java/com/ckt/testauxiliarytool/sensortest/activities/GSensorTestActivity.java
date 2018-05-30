package com.ckt.testauxiliarytool.sensortest.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;

public class GSensorTestActivity extends BaseActivity {
    public static String TAG = "GSensor";
    //相册场景按钮
    private Button mAlbumButton;
    //播放器场景按钮
    private Button mPlayerButton;
    //浏览器场景按钮
//    private Button mBrowserButton;

    private final int REQUEST_CODE_ASK_WRITE_SETTINGS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.st_activity_gsensor);
        initView();
        initListener();
        initPermission();
    }

    private void initView() {
        mAlbumButton = (Button) findViewById(R.id.album_button);
        mPlayerButton = (Button) findViewById(R.id.play_button);
//        mBrowserButton = (Button) findViewById(R.id.browser_button);

    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                applyWriteSettingsPermission();
            } else {
                //设置自动横竖屏开启
                Settings.System.putInt(getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 1);
                Toast.makeText(this, "横竖屏自动切换已开启!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initListener() {
        mAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GSensorTestActivity.this, AlbumActivity.class);
                startActivity(intent);
            }
        });
        mPlayerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GSensorTestActivity.this, PlayerActivity.class);
                startActivity(intent);
            }
        });
//        mBrowserButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(GSensorTestActivity.this, BrowserActivity.class);
//                startActivity(intent);
//            }
//        });

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void applyWriteSettingsPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_ASK_WRITE_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ASK_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Toast.makeText(this, "该测试必须WRITE_SETTINGS权限！！", Toast.LENGTH_SHORT).show();
                    applyWriteSettingsPermission();
                }
            }
        }
    }


}
