package com.ckt.testauxiliarytool.sensortest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.bean.PSensorTestRecord;

import java.util.List;

/**
 * Created by D22400 on 2017/10/24.
 * Email:danfeng.qiu@ck-telecom.com
 * Describe:
 */

public class PRecordsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<PSensorTestRecord> mRecords;

    public PRecordsListAdapter(Context context, List<PSensorTestRecord> records) {
        mContext = context;
        mRecords = records;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PSensorTestViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.st_item_psensor_records, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PSensorTestViewHolder) holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }


    class PSensorTestViewHolder extends RecyclerView.ViewHolder {

        TextView mNoTextView;
        TextView mOffTimeTextView;
        TextView mOnTimeTextView;

        PSensorTestViewHolder(View itemView) {
            super(itemView);
            mNoTextView = (TextView) itemView.findViewById(R.id.tv_no);
            mOffTimeTextView = (TextView) itemView.findViewById(R.id.tv_off_time);
            mOnTimeTextView = (TextView) itemView.findViewById(R.id.tv_on_time);

        }

        void bind(int position) {
            mNoTextView.setText(String.valueOf(position + 1));
            mOffTimeTextView.setText(String.valueOf(mRecords.get(position).getScreenOffTime()));
            mOnTimeTextView.setText(String.valueOf(mRecords.get(position).getScreenOnTime()));
        }
    }
}
