package com.ckt.testauxiliarytool.tp.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.TPTestActivity;
import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.tp.adapters.VideoListAdapter;
import com.ckt.testauxiliarytool.tp.model.Constant;
import com.ckt.testauxiliarytool.tp.model.VideoItem;
import com.ckt.testauxiliarytool.tp.recorder.ScreenRecordConfig;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.ckt.testauxiliarytool.tp.recorder.ScreenRecordConfig.Contants.OUTPUT_FILE_DIR_DEFAULT;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/10/17
 * <br/>TODO: 录屏视频文件管理类
 */

public class VideoManageFragment extends BaseFragment implements VideoListAdapter.OnVideoItemClickListener, VideoListAdapter.OnVideoItemLongClickListener {
    private static final int SPAN_COUNT = 2;
    private static final String KEY_LAYOUT_MANAGER = "key_layout_manager";
    private RecyclerView mRecyclerView;
    private List<VideoItem> mItemList;
    private VideoListAdapter mAdapter;
    private ScanThread mScanThread;
    private TextView mTvEmpty;
    private ProgressDialog mLoadDialog;
    private LinearLayoutManager mLayoutManager;


    protected int mCurrentLayoutManagerType;
    private static final int GRID_LAYOUT_MANAGER = 0;
    private static final int LINEAR_LAYOUT_MANAGER = 1;

//    private enum LayoutManagerType {  // 慎用枚举
//        GRID_LAYOUT_MANAGER,
//        LINEAR_LAYOUT_MANAGER
//    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.tp_layout_video_list, null);
        mItemList = new ArrayList<>();
        mTvEmpty = (TextView) view.findViewById(R.id.id_tv_empty_list);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.id_rv_video);
        mAdapter = new VideoListAdapter(getActivity(), mItemList, this, this);

        mCurrentLayoutManagerType = LINEAR_LAYOUT_MANAGER;
        if (savedInstanceState != null) {  // Restore saved layout manager type if exists.
            mCurrentLayoutManagerType = savedInstanceState
                    .getInt(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);
        mRecyclerView.setAdapter(mAdapter);
        setHasOptionsMenu(true); // 设置使用menu
        return view;
    }

    @Override
    public int getTitleId() {
        return R.string.record_files_manager;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前布局状态
        outState.putInt(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
    }

    @Override
    public void initData() {
        super.initData();
        refreshData();
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        if (isVisible() && !isHidden()) {  // 可见时加载数据
            if (mLoadDialog == null) {  // 第一次加载时显示,其他情况直接后台加载
                mLoadDialog = new ProgressDialog(getActivity());
                mLoadDialog.setMessage("正在加载视频列表..");
                mLoadDialog.show();
            }
            if (mScanThread == null) {  /* 初始化线程*/
                mScanThread = new ScanThread();
                mScanThread.isFirstIn = true;
            }

            if (mScanThread.getState() != Thread.State.RUNNABLE) { // 没有运行时启动
                mScanThread.start();
            }
        }
    }

    @Override
    public void onPublishStateChange() {
        refreshData();
    }

    private class ScanThread extends Thread {
        public boolean isFirstIn;

        @Override
        public void run() {
            super.run();
            long lastTime = System.currentTimeMillis();
            final int result = scanfVideoFiles();
            long dt = System.currentTimeMillis() - lastTime;
            SystemClock.sleep(dt <= 500 ? 500 : 0);  // 强制显示500ms，防止闪屏影响用户体验

            MyApplication.getMainHander().post(new Runnable() {
                @Override
                public void run() {
                    if (mLoadDialog.isShowing()) mLoadDialog.dismiss(); // 取消进度对话框
                    mTvEmpty.setVisibility(result > 0 ? View.INVISIBLE : View.VISIBLE);

                    if (isFirstIn) {  // 每次初进入时才提示
                        Toast.makeText(MyApplication.getContext(), result > 0 ? "刷新成功" : "暂无录屏文件", Toast.LENGTH_SHORT).show();
                        mScanThread.isFirstIn = false;
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }


    /**
     * 扫描本地目录的视频文件
     *
     * @return 返回文件列表的长度
     */
    private int scanfVideoFiles() {
        File dir = new File(OUTPUT_FILE_DIR_DEFAULT);
        if (!dir.exists() || !dir.isDirectory()) return -1;
        mItemList.clear();
        File[] files = dir.listFiles();
        if (files == null || files.length <= 0) return -1;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory() && file.getName().endsWith(".mp4")) {
                Date date = new Date(file.lastModified());
                SimpleDateFormat formatter = new SimpleDateFormat("YYYY.MM.dd", Locale.CHINA);
                String time = formatter.format(date);
                VideoItem item = new VideoItem(file.getName(), file.getAbsolutePath(), time, getFileSize(file.length()));
                mItemList.add(item);
            }
        }
        return mItemList.size();
    }

    // 点击选择播放
    @Override
    public void onVideoItemClick(VideoListAdapter.VH holder, VideoItem item) {
        File file = new File(item.getVideoPath());
        if (!file.exists()) {
            Toast.makeText(MyApplication.getContext(), "打开文件不存在!", Toast.LENGTH_LONG).show();
            notifyItemRemoved(holder.getAdapterPosition());
            return;
        }
        //Toast.makeText(MyApplication.getContext(), item.getVideoPath(), Toast.LENGTH_SHORT).show();
        String authorities;
        if ((authorities = getString(R.string.TP_FILE_PROVIDER_AUTHORITIES)) == null) {
            authorities = Constant.FILE_PROVIDER_AUTHORITIES;
        }
        Uri uriForFile = FileProvider.getUriForFile(MyApplication.getContext(), authorities, file);
        // 播放视频
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        //对目标应用临时授权该Uri所代表的文件
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uriForFile, "video/*");
        startActivity(intent);
    }

    // 长按删除
    @Override
    public void onVideoItemLongClick(final VideoListAdapter.VH holder, VideoItem item) {
        final int position = holder.getAdapterPosition();
        //Log.e("TAG", "position=" + position);
        final File file = new File(item.getVideoPath());
        if (file.exists()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.warnings)
                    .setMessage("是否删除当前文件?" + "\n" + item.getVideoName())
                    .setPositiveButton(R.string.comfirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean delete = file.delete();
                            if (delete) {
                                Toast.makeText(MyApplication.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                notifyItemRemoved(position);
                            } else {
                                Toast.makeText(MyApplication.getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.cancel, null).show();
        } else {
            Toast.makeText(MyApplication.getContext(), "删除失败,文件不存在", Toast.LENGTH_LONG).show();
            notifyItemRemoved(position);
        }
    }

    private void notifyItemRemoved(int position) {
        if (position > mItemList.size()) return;
        mItemList.remove(position);
        mAdapter.notifyItemRemoved(position);
        if (mItemList.size() <= 0) mTvEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * 将长度换算为具体带单位的String
     *
     * @param length 文件长度 byte
     * @return String
     */
    public String getFileSize(long length) {
        String size = "";
        String suffix = "";
        if (length < 1024) {
            size = length + "B";
        } else if (length < 1024 * 1024) {
            suffix = "KB";
            size = String.valueOf(length * 1.0f / 1024);
        } else if (length < 1024 * 1024 * 1024) {
            suffix = "MB";
            size = String.valueOf(length * 1.0f / (1024 * 1024));
        } else if (length < Long.MAX_VALUE) {
            suffix = "GB";
            size = String.valueOf(length * 1.0f / (1024 * 1024 * 1024));
        }
        int i = size.indexOf(".");
        return size.substring(0, i + 3) + suffix;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onFragmentHiddenChanged(boolean showing) {
        super.onFragmentHiddenChanged(showing);
        if (showing) {
            refreshData();
        } else {
            mScanThread.isFirstIn = true;
            if (mLoadDialog != null && mLoadDialog.isShowing()) mLoadDialog.dismiss();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tp_menu_fragment_video_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_shift_layout:  // 切换布局
                shiftLayout();
                break;
            case R.id.id_clear_record_dir_menu_video:
                clearRecordDir();
                break;
        }
        return true;
    }

    /**
     * 切换布局
     */
    private void shiftLayout() {
        if (mCurrentLayoutManagerType == LINEAR_LAYOUT_MANAGER) {
            setRecyclerViewLayoutManager(GRID_LAYOUT_MANAGER);
        } else {
            setRecyclerViewLayoutManager(LINEAR_LAYOUT_MANAGER);
        }
    }

    /**
     * 清理录屏目录
     */
    private void clearRecordDir() {
        if (getActivity() instanceof TPTestActivity) {
            if (((TPTestActivity) getActivity()).isRecording()) {
                Toast.makeText(MyApplication.getContext(), R.string.try_later, Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(getActivity())  // 弹窗提示
                        .setTitle(R.string.warnings)
                        .setMessage(getString(R.string.clear_all_videos) +
                                "\n\npath = " + ScreenRecordConfig.Contants.OUTPUT_FILE_DIR_DEFAULT)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.comfirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doClear();
                                //刷新
                                refreshData();
                            }
                        }).create().show();
            }
        }

    }

    /**
     * 执行清除操作
     */
    private void doClear() {
        File dir = new File(ScreenRecordConfig.Contants.OUTPUT_FILE_DIR_DEFAULT);
        if (!dir.exists()) {
            return;
        }
        String[] list = dir.list();
        for (String name : list) {   // 循环遍历并删除
            File dyingFile = new File(dir, name);
            if (dyingFile.exists()) {
                boolean delete = dyingFile.delete();
                if (!delete) {
                    Toast.makeText(MyApplication.getContext(), dyingFile.getName() + getString(R.string.clear_videos_failed), Toast.LENGTH_SHORT).show();
                }
            }
        }

        // 清除最近录屏文件记录
        SharedPrefsUtil.name("record_config").remove("lastRecordFile").recycle("record_config");
        Toast.makeText(MyApplication.getContext(), R.string.clear_videos_successful, Toast.LENGTH_SHORT).show();
    }


    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(int layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mScanThread != null) {
            mScanThread.interrupt();
            mScanThread = null;
        }
    }
}
