package com.ckt.testauxiliarytool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.LogUtil;

import java.util.List;

/**
 * Created by ckt on 17-11-23.
 */

public class FunctionListAdapter extends RecyclerView.Adapter<FunctionListAdapter.FunctionHolder> {
    public static final String TAG = FunctionListAdapter.class.getSimpleName();
    private final Context mContext;
    private List<String> mItems;
    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener{
        /**
         *
         * @param pos
         */
        void onItemClick(int pos);
    }
    /**
     *
     * @param items
     */
    public FunctionListAdapter(Context context, List<String> items) {
        LogUtil.d(TAG,"FunctionListAdapter construction");
        mContext = context;
        this.mItems = items;
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
    }

    @Override
    public FunctionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtil.d(TAG,"onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.function_item_layout, parent, false);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new FunctionHolder(view);
    }

    @Override
    public void onBindViewHolder(FunctionHolder holder, final int position) {
        LogUtil.d(TAG,"onBindViewHolder");
        final String itemName = mItems != null ? ((position < mItems.size()) ? mItems.get(position) :  "over length" ): "error!";
        holder.mTextView.setText(itemName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                if(mItemClickListener != null){
                    mItemClickListener.onItemClick(position);
                }
                //Toast.makeText(mContext, "clicked", Toast.LENGTH_LONG);
                /*Snackbar.make(v, "click me snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            */}
        });
    }


    @Override
    public int getItemCount() {
        LogUtil.d(TAG,"getItemCount");
        return mItems != null ? mItems.size() : 0;
    }

    public static class FunctionHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public FunctionHolder(View view) {
            super(view);
            LogUtil.d(TAG,"FunctionHolder construction");
            mTextView = (TextView) view.findViewById(R.id.itemname);
        }


    }
}
