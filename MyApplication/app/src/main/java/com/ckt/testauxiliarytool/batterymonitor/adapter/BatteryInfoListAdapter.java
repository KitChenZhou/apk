package com.ckt.testauxiliarytool.batterymonitor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.batterymonitor.bean.BatteryInfo;
import com.ckt.testauxiliarytool.utils.DateTimeUtils;

import java.util.List;

/**
 * 电池信息列表的Adapter，在查看历史记录页面展示
 */
public class BatteryInfoListAdapter extends RecyclerView.Adapter<BatteryInfoListAdapter.BatteryInfoListViewHolder> {
    private List<BatteryInfo> mAllBatteryData; // 从Realm数据库中查询的全部数据
    private LayoutInflater mLayoutInflater;


    public BatteryInfoListAdapter(List<BatteryInfo> allBatteryData, Context context) {
        mAllBatteryData = allBatteryData;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public BatteryInfoListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.bm_item_battery_info_list, parent, false);
        return new BatteryInfoListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BatteryInfoListViewHolder holder, int position) {
        holder.bind(mAllBatteryData.get(position));
    }

    @Override
    public int getItemCount() {
        return mAllBatteryData == null || mAllBatteryData.isEmpty() ? 0 : mAllBatteryData.size();
    }

    static class BatteryInfoListViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_battery_info, tv_time;

        BatteryInfoListViewHolder(View itemView) {
            super(itemView);
            tv_battery_info = (TextView) itemView.findViewById(R.id.tv_battery_info);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
        }

        void bind(BatteryInfo batteryInfo) {
            tv_time.setText(DateTimeUtils.millis2String(batteryInfo.getCurrentMillis()) + "     " + batteryInfo.getStatus());
            tv_battery_info.setText(batteryInfo.getLevel() + "%      " + batteryInfo.getTemperature()
                    + "℃      " + batteryInfo.getVoltage() + "     " + batteryInfo.getPlugged()
                    + "     " + batteryInfo.getHealth()
            );
        }
    }
}