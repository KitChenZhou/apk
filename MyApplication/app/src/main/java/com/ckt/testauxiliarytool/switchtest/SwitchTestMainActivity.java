package com.ckt.testauxiliarytool.switchtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.adapter.SwitchTestAdapter;
import com.ckt.testauxiliarytool.utils.GridSpacingItemDecoration;
import com.ckt.testauxiliarytool.BaseActivity;
/**
 * Created by wgp on 2017/12/11.
 */

public class SwitchTestMainActivity extends BaseActivity {
    private static final String TAG = "SwitchTestMainActivity";

    private RecyclerView mRecyclerView;//RecyclerView组件
    private SwitchTestAdapter mSwitchTestAdapter;//Recycler适配器
    private String[] mDataSet;//测试Acvitity名字数据源
    private int[] mPicDataSet;//测试Acvitity对应图片资源
    private Class<?>[] mSwitchTestActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_switch_test);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager glm = new GridLayoutManager(this, 3);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SwitchTestMainActivity.this);
        mRecyclerView.setLayoutManager(glm);
        int spanCount = 3;//跟布局里面的spanCount属性是一致的
        int spacing = 2;//每一个矩形的间距
        boolean includeEdge = false;//如果设置成false那边缘地带就没有间距s
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        initData();
        mSwitchTestAdapter = new SwitchTestAdapter(SwitchTestMainActivity.this, mDataSet, mPicDataSet);
        mRecyclerView.setAdapter(mSwitchTestAdapter);
        mSwitchTestAdapter.setItemClickListener(new SwitchTestAdapter.MyItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Intent it = new Intent(SwitchTestMainActivity.this, mSwitchTestActivities[position]);
                startActivity(it);
            }
        });
    }

    private void initData() {
        mDataSet = getResources().getStringArray(R.array.switch_name);
        mPicDataSet = new int[]{R.drawable.ic_airplanemode, R.drawable.ic_swap_vert, R.drawable.ic_bluetooth, R.drawable.ic_wifi_blue
                , R.drawable.ic_camera_alt, R.drawable.ic_more};
        mSwitchTestActivities = new Class<?>[]{APModeActivity.class, DataModeActivity.class, BlueToothActivity.class, WifiActivity.class
                , CameraActivity.class, MoreTestActivity.class};
    }
}
