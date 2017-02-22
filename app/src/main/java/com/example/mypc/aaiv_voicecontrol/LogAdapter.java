package com.example.mypc.aaiv_voicecontrol;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;

import java.util.List;

/**
 * Created by MyPC on 02/22/2017.
 */

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.MyViewHolder> {
    private List<LogResponse> logList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView iv_image;
        public TextView tv_createdate;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_image = (ImageView) itemView.findViewById(R.id.iv_log_image);
            tv_createdate = (TextView) itemView.findViewById(R.id.tv_createdate);
        }
    }

    public LogAdapter(List<LogResponse> logList) {
        this.logList = logList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        LogResponse log = logList.get(position);
        Glide.with(ShowLogsActivity.getContext()).load(log.imgUrl).into(holder.iv_image);
        holder.tv_createdate.setText(log.createdate);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }


}
