package com.ckt.testauxiliarytool.sensortest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.bean.LSensorTestRecord;

import java.util.List;

/**
 * Created by D22400 on 2017/11/2.
 * Email:danfeng.qiu@ck-telecom.com
 * Describe:
 */

public class LRecordsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<LSensorTestRecord> mRecords;
    private OnLongClickListener mOnLongClickListener;

    public LRecordsListAdapter(Context context, List<LSensorTestRecord> records) {
        mContext = context;
        mRecords = records;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LSensorTestViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.st_item_lsensor_records, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((LSensorTestViewHolder) holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    class LSensorTestViewHolder extends RecyclerView.ViewHolder {

        TextView mNoTextView;
        TextView mTextNameTextView;
        TextView mLuxTextView;
        TextView mRangeTextView;
        TextView mTimeTextView;

        public LSensorTestViewHolder(View itemView) {
            super(itemView);
            mNoTextView = (TextView) itemView.findViewById(R.id.tv_no);
            mTextNameTextView = (TextView) itemView.findViewById(R.id.tv_testName);
            mLuxTextView = (TextView) itemView.findViewById(R.id.tv_lux);
            mRangeTextView = (TextView) itemView.findViewById(R.id.tv_range);
            mTimeTextView = (TextView) itemView.findViewById(R.id.tv_time);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnLongClickListener != null) {
                        mOnLongClickListener.onLongClick(getAdapterPosition());
                    }
                    return true;
                }
            });
        }

        void bind(int position) {
            mNoTextView.setText(String.valueOf(position + 1));
            mTextNameTextView.setText(mRecords.get(position).getTestName());
            mLuxTextView.setText(String.valueOf(mRecords.get(position).getLux()));
            mRangeTextView.setText(mRecords.get(position).getRange());
            mTimeTextView.setText(String.valueOf(mRecords.get(position).getTime() / 1000f));

        }
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        mOnLongClickListener = listener;
    }

    public interface OnLongClickListener {
        void onLongClick(int position);
    }
}
