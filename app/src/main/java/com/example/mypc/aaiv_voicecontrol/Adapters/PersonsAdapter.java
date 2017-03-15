package com.example.mypc.aaiv_voicecontrol.Adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mypc.aaiv_voicecontrol.R;
import com.example.mypc.aaiv_voicecontrol.UpdatePersonActivity;
import com.example.mypc.aaiv_voicecontrol.data_model.PersonModel;
import com.microsoft.projectoxford.face.contract.Person;

import java.util.List;

/**
 * Created by MyPC on 02/27/2017.
 */

public class PersonsAdapter extends RecyclerView.Adapter<PersonsAdapter.MyViewHolder>{

    private List<PersonModel> mPersonList;
    private Context mContext;

    public PersonsAdapter(List<PersonModel> mPersonList, Context mContext) {
        this.mPersonList = mPersonList;
        this.mContext = mContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.persons_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        PersonModel person = mPersonList.get(position);
        holder.mTvPersonName.setText(person.name);
        holder.mTvUserData.setText(person.userData);
        Glide.with(UpdatePersonActivity.getContext())
                .load(person.faces.get(0).imageUrl)
                .into(holder.mPersonAvatar);
        holder.mOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.mOverflow);
            }
        });
    }

    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_person, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_delete:
                    Toast.makeText(mContext, "Delete Person", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }

    }

    @Override
    public int getItemCount() {
        return this.mPersonList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView mPersonAvatar, mOverflow;
        private TextView mTvPersonName;
        private TextView mTvUserData;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTvPersonName = (TextView) itemView.findViewById(R.id.tv_person_name);
            mPersonAvatar = (ImageView) itemView.findViewById(R.id.person_avatar);
            mTvUserData = (TextView) itemView.findViewById(R.id.tv_userData);
            mOverflow = (ImageView) itemView.findViewById(R.id.overflow);
        }
    }

}
