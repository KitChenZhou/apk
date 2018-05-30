package com.ckt.testauxiliarytool.sensortest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.bean.MSensor;

import java.util.List;

/**
 * Created by D22434 on 2017/8/24.
 * MSensor的RecyclerView适配器
 */

public class MAdapter extends RecyclerView.Adapter<MAdapter.MyViewHolder> {

    private List<MSensor> mSensors;
    private Context mContext;

    public MAdapter(Context context, List<MSensor> mSensors) {
        mContext = context;
        this.mSensors = mSensors;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.st_item_msensor_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MSensor mSensor = mSensors.get(position);
        holder.mTvID.setText(String.valueOf(mSensors.size() - position));
        holder.mTvTest.setText(mSensor.getAngle() + "°");

        holder.mTvValue.setText(mSensor.getDeviation() + "°");

        if (mSensor.getDeviation() <= 5) {
            holder.mTvResult.setText("pass");
            holder.mTvResult.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.mTvResult.setText("fail");
            holder.mTvResult.setTextColor(Color.parseColor("#F44336"));
        }

    }

    @Override
    public int getItemCount() {
        return mSensors.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mTvTest, mTvValue, mTvID, mTvResult;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTvTest = (TextView) itemView.findViewById(R.id.tv_test);
            mTvValue = (TextView) itemView.findViewById(R.id.tv_value);
            mTvID = (TextView) itemView.findViewById(R.id.tv_id);
            mTvResult = (TextView) itemView.findViewById(R.id.tv_result);
        }
    }

}
