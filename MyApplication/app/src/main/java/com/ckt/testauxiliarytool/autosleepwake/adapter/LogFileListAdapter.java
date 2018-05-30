package com.ckt.testauxiliarytool.autosleepwake.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;

import java.io.File;
import java.util.List;

/**
 * 显示指定目录下的所有.log文件
 */
public class LogFileListAdapter extends RecyclerView.Adapter<LogFileListAdapter.LogFileListViewHolder> {
    private List<File> mFiles;
    private OnItemClickListener mOnItemClickListener;

    public LogFileListAdapter(List<File> files) {
        this.mFiles = files;
    }

    // 事件回调接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public LogFileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(), R.layout.asw_item_log_file_list, null);
        return new LogFileListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final LogFileListViewHolder holder, int position) {
        holder.bind(mFiles.get(position).getName());
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, holder.getLayoutPosition());
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(v, holder.getLayoutPosition());
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mFiles == null || mFiles.isEmpty() ? 0 : mFiles.size();
    }

    static class LogFileListViewHolder extends RecyclerView.ViewHolder {
        private ImageView mLogFileIv;
        private TextView mFileNameTv;

        LogFileListViewHolder(View itemView) {
            super(itemView);
            mFileNameTv = (TextView) itemView.findViewById(R.id.tv_file_name);
            mLogFileIv = (ImageView) itemView.findViewById(R.id.iv_log_file);
        }

        void bind(String fileName) {
            mFileNameTv.setText(fileName);
            mLogFileIv.setImageResource(R.drawable.asw_icon_log_text);
        }
    }
}
