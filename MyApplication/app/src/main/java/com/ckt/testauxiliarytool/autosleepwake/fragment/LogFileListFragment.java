package com.ckt.testauxiliarytool.autosleepwake.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.autosleepwake.adapter.LogFileListAdapter;
import com.ckt.testauxiliarytool.autosleepwake.interfaces.Constants;
import com.ckt.testauxiliarytool.utils.FileUtils;

import java.io.File;
import java.util.List;

//import android.support.v7.widget.DividerItemDecoration;

public class LogFileListFragment extends BaseFragment {
    private RecyclerView mLogListRv;
    private List<File> mFiles;
    private LogFileListAdapter mLogFileListAdapter;
    private OnItemClickListener mOnItemClickListener;

    /**
     * 该回调接口需要宿主Activity来实现
     */
    public interface OnItemClickListener {
        void onItemClick(String filePath);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    protected void initData(@Nullable Bundle savedInstanceState) {
        mFiles = FileUtils.listFilesInDirWithFilter(Constants.LOG_DIR, ".log");
    }

    @Override
    protected void initListener() {
        mLogFileListAdapter.setOnItemClickListener(new LogFileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mOnItemClickListener.onItemClick(mFiles.get(position).getAbsolutePath()); // 回调宿主Activity的方法
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // 留着，长按列表项
            }
        });
    }

    @Override
    protected void initViews(View rootView) {
        mLogListRv = (RecyclerView) rootView.findViewById(R.id.rv_log_file_list);
        mLogListRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        //mLogListRv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mLogListRv.setAdapter(mLogFileListAdapter = new LogFileListAdapter(mFiles));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.asw_fragment_log_file_list;
    }
}
