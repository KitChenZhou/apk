package com.ckt.testauxiliarytool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;

/**
 * Created by wgp on 2017/12/11.
 */

public class SwitchTestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private MyItemClickListener mItemClickListener;
    private Context mContext;
    private String[] mDataSet;
    private int[] mPicDataSet;
    private int[] mPicData;

    public SwitchTestAdapter(Context mContext, String[] mDataSet, int[] mPicDataSet) {
        this.mContext = mContext;
        this.mDataSet = mDataSet;
        this.mPicDataSet = mPicDataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.recyclerview_item, null);
        //将全局的监听传递给holder
        MyViewHolder holder = new MyViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.mTextView.setText(mDataSet[position]);
        myViewHolder.mImageView.setBackgroundResource(mPicDataSet[position]);
    }

    @Override
    public int getItemCount() {
        return mDataSet.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyItemClickListener mListener;
        private TextView mTextView;
        private ImageView mImageView;

        public MyViewHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_tv);
            mImageView = (ImageView) itemView.findViewById(R.id.item_iv);
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);
        }

        /**
         * 实现OnClickListener接口重写的方法
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }

        }
    }

    /**
     * 创建一个点击的回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 在主activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
}
