package com.mycompany.john.pickaplace.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mycompany.john.pickaplace.R;

import java.util.ArrayList;
import java.util.Map;

public class LocationHistoryAdapter extends RecyclerView.Adapter<LocationHistoryAdapter.MyViewHolder> {
    private ArrayList<Map<String, String>> mDataset;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mCode, mMessage, mLive;

        public MyViewHolder(View itemView) {
            super(itemView);

            this.mCode = itemView.findViewById(R.id.code_txt_id);
            this.mMessage = itemView.findViewById(R.id.msg_txt_id);
            this.mLive = itemView.findViewById(R.id.live_txt_id);
        }
    }

    public LocationHistoryAdapter(ArrayList<Map<String, String>> mDataset) {
        this.mDataset = mDataset;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_history_adapter,
                parent, false);

        mContext = parent.getContext();

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Map<String, String> location = mDataset.get(position);
        final String code = location.get("code");
        final String msg = location.get("msg");
        final String live = location.get("live");

        holder.mCode.setText(code);
        if (msg.equals("null")) {
            holder.mMessage.setText("");
        } else {
            holder.mMessage.setText(msg);
        }
        holder.mLive.setText(live);

        if (live.equals("false")) {
            holder.mLive.setTextColor(ResourcesCompat.getColor(mContext.getResources(),
                    R.color.colorOrange,
                    null));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
