package com.example.mypc.aaiv_voicecontrol.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mypc.aaiv_voicecontrol.R;
import com.microsoft.projectoxford.face.contract.Person;

import java.util.List;

/**
 * Created by MyPC on 02/27/2017.
 */

public class PersonsAdapter extends RecyclerView.Adapter<PersonsAdapter.MyViewHolder>{

    private List<Person> mPersonList;

    public PersonsAdapter(List<Person> mPersonList) {
        this.mPersonList = mPersonList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.persons_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Person person = mPersonList.get(position);
        holder.mTvPersonName.setText(person.name);
    }

    @Override
    public int getItemCount() {
        return this.mPersonList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvPersonName;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTvPersonName = (TextView) itemView.findViewById(R.id.tv_person_name);
        }
    }

}
