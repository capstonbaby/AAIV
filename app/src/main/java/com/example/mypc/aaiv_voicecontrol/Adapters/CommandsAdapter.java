package com.example.mypc.aaiv_voicecontrol.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mypc.aaiv_voicecontrol.R;

import java.util.List;

/**
 * Created by MyPC on 04/03/2017.
 */

public class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.MyViewHolder> {

    private List<Commands> CommandList;

    public CommandsAdapter(List<Commands> commandList) {
        CommandList = commandList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.command_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommandsAdapter.MyViewHolder holder, int position) {
        Commands command = CommandList.get(position);
        holder.tv_command_title.setText(command.getCommand_title());
        holder.tv_command_value.setText(command.getCommand_value());
    }

    @Override
    public int getItemCount() {
        return CommandList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_command_title;
        public TextView tv_command_value;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_command_title = (TextView) itemView.findViewById(R.id.tv_command_title);
            tv_command_value = (TextView) itemView.findViewById(R.id.tv_command_value);
        }
    }
}
