package com.ckt.testauxiliarytool.batterymonitor.fragment;

import android.Manifest;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.batterymonitor.bean.BatteryInfo;
import com.ckt.testauxiliarytool.batterymonitor.db.InfoDaoImpl;
import com.ckt.testauxiliarytool.utils.DateTimeUtils;
import com.ckt.testauxiliarytool.utils.GetRamRomSdUtil;
import com.ckt.testauxiliarytool.utils.PermissionUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * 显示曲线图的Fragment
 */
public class ChartFragment extends BaseFragment {
    private LineChart line_chart; // 折线图
    private LineData mLineData; // 点数据集，可包含多条曲线的数据集
    private BatteryInfo mFirstData, mLastData; // 第一项和最后一项数据

    @Override
    protected int getLayoutResourceId() {
        return R.layout.bm_fragment_chart;
    }

    @Override
    protected void initData() {
//        mRealm = Realm.getDefaultInstance();
//        RealmResults<BatteryInfo> allData = mRealm.where(BatteryInfo.class).findAll();
        List<BatteryInfo> allData = InfoDaoImpl.getInstance().queryAll();
        if (allData != null && !allData.isEmpty()) {
            mFirstData = allData.get(0);
            mLastData = allData.get(allData.size() - 1);
            ArrayList<Entry> levelValues = new ArrayList<>(); // 电量值集合
            ArrayList<Entry> temperatureValues = new ArrayList<>(); // 温度值集合
            int size = allData.size();
            BatteryInfo currentData;
            for (int i = 0; i < size; i++) {
                currentData = allData.get(i);
                // x的值，以分钟为单位
                int xValue = Math.round((currentData.getCurrentMillis() - mFirstData.getCurrentMillis()) / 60000);
                // y的值分别为电量和温度
                levelValues.add(new Entry(xValue, currentData.getLevel()));
                temperatureValues.add(new Entry(xValue, currentData.getTemperature()));
            }
            // 将电量数据集和温度数据集整合在一个折线数据集中，也就是说将会包含多条线的数据（当前2条）
            mLineData = new LineData(createDataSet(levelValues, "电量%", Color.rgb(244, 67, 54)),
                    createDataSet(temperatureValues, "温度℃", Color.rgb(76, 175, 80)));
            mLineData.setValueTextColor(Color.RED); // 绘制的值的颜色，将会作用于它包含的所有数据集
            mLineData.setValueTextSize(9f); // 绘制的值的字体大小
            // 设置显示的值的格式，将小数点去掉
            mLineData.setValueFormatter(new IValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return (int) value + "";
                }
            });
        } else {
            line_chart.setNoDataText("没有数据可供显示！"); // 当没有数据时显示的文本
        }
    }

    /**
     * 创建一条曲线的数据集
     *
     * @param values 一个包含所有点数据的List集合
     * @param label  标签，将会显示在图表下方，用于描述该曲线的作用
     * @param color  颜色值，该颜色将会作用于3个地方：折线条、折线条下方的填充区域、点上的小圆圈，当然也可以自定义
     * @return 一条曲线的数据集
     */
    private LineDataSet createDataSet(List<Entry> values, String label, int color) {
        LineDataSet lineDataSet = new LineDataSet(values, label);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(color); // 折线条的颜色
//        lineDataSet.setValueTextColor(Color.rgb(244, 67, 54)); // 设置绘制的值的颜色，仅作用于该数据集
        lineDataSet.setLineWidth(2.0f); // 折线的宽度
        lineDataSet.setDrawCircles(true); // 是否在点上绘制小圆圈
        lineDataSet.setDrawCircleHole(true); // 在圈中绘制小孔
        lineDataSet.setCircleColor(color); // 小圆圈的颜色
        lineDataSet.setDrawValues(true); // 是否在点上绘制值
        lineDataSet.setFillColor(color); // 设置填充域的颜色
        lineDataSet.setFillAlpha(65); // 设置填充区域的透明度
        lineDataSet.setDrawFilled(true); // 设置填充，即在折线下绘制填充域
        lineDataSet.setHighLightColor(Color.BLUE); // 当点击某个点时，横竖两条线的颜色
        return lineDataSet;
    }

    @Override
    protected void findViews(View rootView) {
        line_chart = (LineChart) rootView.findViewById(R.id.line_chart);
        line_chart.setData(mLineData); // 给折线图形设置数据
        line_chart.setTouchEnabled(true); // 设置可触摸
        line_chart.setDragEnabled(true); // 设置可拖拽
        line_chart.setSaveEnabled(true); // 设置图表可保存
        setChartDescription();
        setHasOptionsMenu(true); // 让Fragment可添加选项菜单
    }

    // 设置图表的描述
    private void setChartDescription() {
        Description description = new Description();
        // 以时间段来给表格添加描述
        description.setText(DateTimeUtils.millis2String(mFirstData.getCurrentMillis()) + "至"
                + DateTimeUtils.millis2String(mLastData.getCurrentMillis()));
        description.setTextColor(Color.RED);
        description.setTextSize(15);
        line_chart.setDescription(description);
    }

    @Override
    protected void setListeners() {
        // 给图表设置点击值的事件回调
        line_chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // 大致估算，存在误差：先取得当前的X值转换为毫秒，再加上第一个数据的毫秒值，即为当前点的毫秒值，
                // 然后按指定格式将毫秒值转换为日期格式，把秒钟干掉，没啥用
                // 如果需要精确值可去数据库匹配，但需考虑如下情况：level值可能不唯一，如先放电后充电
                getActivity().setTitle(DateTimeUtils.millis2String((Math.round(e.getX() * 60000) + mFirstData.getCurrentMillis()),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())) + "   " + Math.round(e.getX()) + "minute");
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bm_menu_chart_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_chart) {
            if (line_chart.isSaveEnabled() && GetRamRomSdUtil.externalMemoryAvailable()) {
                PermissionUtils.requestPermissions(getActivity(), 0, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, new PermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        // 直接保存在内置SD卡根目录，也可保存到Gallery中去...
                        boolean isSaved = line_chart.saveToPath(DateTimeUtils.millis2String(System.currentTimeMillis()), "");
                        if (isSaved) {
                            showToast("保存成功！");
                        } else {
                            showToast("保存失败！");
                        }
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                        showToast("读写外部存储权限被拒绝！");
                    }
                });
            }
        }
        return true;
    }

    private void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("曲线图");
    }

}
