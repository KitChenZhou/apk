package com.ckt.testauxiliarytool.sensortest.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.adapter.HAdapter;
import com.ckt.testauxiliarytool.sensortest.bean.HSensor;
import com.ckt.testauxiliarytool.sensortest.db.SensorLab;
import com.ckt.testauxiliarytool.sensortest.service.HSensorTestService;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by D22434 on 2017/9/18.
 */

public class HallFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private Button mBtnStart;
    private View rootView;

    private int HALL_MODE;
    private HAdapter myAdapter;
    private SensorLab mSensorLab;
    private List<HSensor> results = new ArrayList<>();
    private HSensorTestService mService = null;
    private ServiceConnection mConnection = null;

    public static HallFragment newInstance(int flag) {

        Bundle args = new Bundle();
        args.putInt("flag", flag);
        HallFragment fragment = new HallFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.st_fragment_hall, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HALL_MODE = getArguments().getInt("flag");
        mRecyclerView =(RecyclerView) rootView.findViewById(R.id.rv_record);
        mBtnStart =(Button) rootView.findViewById(R.id.btn_start);
        if (HALL_MODE == 1) {
            mBtnStart.setEnabled(false);
            mBtnStart.setText("当前模式正在测试中");
        } else {
            mBtnStart.setEnabled(true);
            mBtnStart.setText("开启后台服务测试");
        }
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnection == null) {
                    mConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            HSensorTestService.MyBinder myBinder = (HSensorTestService.MyBinder) service;
                            mService = myBinder.getService();
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            Toast.makeText(getActivity(), "测试服务异常关闭，测试已停止", Toast.LENGTH_SHORT).show();
                        }
                    };
                    Intent service = new Intent(getContext(), HSensorTestService.class);
                    getActivity().bindService(service, mConnection, Context.BIND_AUTO_CREATE);
                }
                //开始测试后自动跳转到拨号
                Intent toDial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
                startActivity(toDial);
            }
        });
        updateUI();
    }

    public void updateUI() {
        //获取实例化
        mSensorLab = SensorLab.get(getActivity());
        results = mSensorLab.getHRecords(HALL_MODE);
        myAdapter = new HAdapter(getActivity(), results);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(myAdapter);
    }

    public void addRecord(HSensor hSensor) {
        results.add(0, hSensor);
        myAdapter.notifyDataSetChanged();
    }


    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        if (mConnection != null) {
            getActivity().unbindService(mConnection);
            mConnection = null;
        }

    }

}
