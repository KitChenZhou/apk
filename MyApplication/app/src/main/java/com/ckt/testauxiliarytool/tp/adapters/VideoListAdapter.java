package com.ckt.testauxiliarytool.tp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.model.VideoItem;

import java.util.List;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/10/16
 * <br/>TODO: Video条目列表的adapter
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VH> {
    private List<VideoItem> mItemList;
    private Context mContext;
    private OnVideoItemClickListener clickListener;
    private OnVideoItemLongClickListener longClickListener;

    public VideoListAdapter(Context context, List<VideoItem> videoItemList, OnVideoItemClickListener listener) {
        this.mItemList = videoItemList;
        this.mContext = context;
        this.clickListener = listener;
    }

    public VideoListAdapter(Context context, List<VideoItem> videoItemList, OnVideoItemClickListener clickListener, OnVideoItemLongClickListener longClickListener) {
        this.mItemList = videoItemList;
        this.mContext = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tp_layout_item_video, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        VideoItem videoItem = mItemList.get(position);
        holder.name.setText(videoItem.getVideoName());
        holder.time.setText(videoItem.getTime());
        holder.size.setText(videoItem.getSize());
        holder.itemView.setOnClickListener(new MyClickListener(holder,videoItem));
        holder.itemView.setOnLongClickListener(new MyLongClickListener(holder,videoItem));
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class MyClickListener implements View.OnClickListener {
        private VideoItem item;
        private long lastTime = 0;
        private VH holder;
        public MyClickListener(VH holder, VideoItem item) {
            this.item = item;
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            //long lastTime = System.currentTimeMillis();
            if (clickListener != null && System.currentTimeMillis() - lastTime > 500) {
                lastTime = System.currentTimeMillis();
                clickListener.onVideoItemClick(holder,item);
            }
        }
    }

    private class MyLongClickListener implements View.OnLongClickListener {
        private final VideoItem item;
        private VH holder;

        public MyLongClickListener(VH holder, VideoItem item) {
            this.item = item;
            this.holder = holder;
        }

        @Override
        public boolean onLongClick(View v) {
            if (longClickListener != null) longClickListener.onVideoItemLongClick(holder,item);
            return false;
        }
    }

    public interface OnVideoItemClickListener {
        void onVideoItemClick(VH holder, VideoItem item);
    }

    public interface OnVideoItemLongClickListener {
        void onVideoItemLongClick(VH holder, VideoItem item);
    }

    public class VH extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView time;
        private TextView size;

        public VH(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.id_tv_video_name);
            time = (TextView) itemView.findViewById(R.id.id_tv_video_time);
            size = (TextView) itemView.findViewById(R.id.id_tv_video_size);
        }
    }
}
