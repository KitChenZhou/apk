package com.ckt.testauxiliarytool.batterymonitor.fragment;

//import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.batterymonitor.adapter.BatteryInfoListAdapter;
import com.ckt.testauxiliarytool.batterymonitor.bean.BatteryInfo;
import com.ckt.testauxiliarytool.batterymonitor.db.InfoDaoImpl;

import java.util.List;

/**
 * 历史记录页面，从Realm数据库查询到所有数据，用RecyclerView来展示
 */
public class BatteryInfoListFragment extends BaseFragment {
    private List<BatteryInfo> mAllBatteryData;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.bm_fragment_battery_info_list;
    }

    @Override
    protected void initData() {
        // 查询数据
        mAllBatteryData = InfoDaoImpl.getInstance().queryAll();
    }

    @Override
    protected void findViews(View rootView) {
        RecyclerView rv_battery_info_list = (RecyclerView) rootView.findViewById(R.id.rv_battery_info_list);
        rv_battery_info_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        //rv_battery_info_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        rv_battery_info_list.setAdapter(new BatteryInfoListAdapter(mAllBatteryData, getActivity()));
    }

    @Override
    protected void setListeners() {

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("历史记录");
    }

}