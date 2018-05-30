package com.ckt.testauxiliarytool.sensortest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.bean.HSensor;

import java.util.List;

import static com.ckt.testauxiliarytool.sensortest.activities.HallTestActivity.MaxIntervalForClose;
import static com.ckt.testauxiliarytool.sensortest.activities.HallTestActivity.MaxIntervalForOpen;

/**
 * Created by D22434 on 2017/8/24.
 * HSensor的RecyclerView适配器
 */

public class HAdapter extends RecyclerView.Adapter<HAdapter.MyViewHolder> {


    private List<HSensor> results;
    private Context mContext;

    public HAdapter(Context context, List<HSensor> results) {
        mContext = context;
        this.results = results;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.st_item_hall_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        HSensor hSensor = results.get(position);
        holder.mTvID.setText(String.valueOf(results.size() - position));
        holder.mTvValue.setText(String.valueOf(hSensor.getInterval()));

        holder.mTvTest.setText(hSensor.getStatus());
        if (hSensor.getStatus().equals("合盖/灭屏") || hSensor.getStatus().equals("合盖->皮套应用出现")) {
            holder.itemView.setBackgroundColor(Color.parseColor("#CFD8DC"));

            holder.mTvResult.setText(hSensor.getInterval() <= MaxIntervalForClose ? "pass" : "fail");
            holder.mTvResult.setTextColor(hSensor.getInterval() <= MaxIntervalForClose ? Color.parseColor("#4CAF50")
                    : Color.parseColor("#F44336"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));

            holder.mTvResult.setText(hSensor.getInterval() <= MaxIntervalForOpen ? "pass" : "fail");
            holder.mTvResult.setTextColor(hSensor.getInterval() <= MaxIntervalForOpen ? Color.parseColor("#4CAF50")
                    : Color.parseColor("#F44336"));
        }
    }


    @Override
    public int getItemCount() {
        return results.size();
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
