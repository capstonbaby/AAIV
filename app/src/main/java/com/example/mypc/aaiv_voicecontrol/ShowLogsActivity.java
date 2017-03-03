package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mypc.aaiv_voicecontrol.Adapters.LogAdapter;
import com.example.mypc.aaiv_voicecontrol.Utils.DividerItemDecoration;
import com.example.mypc.aaiv_voicecontrol.Utils.RecyclerTouchListener;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.services.DataService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowLogsActivity extends AppCompatActivity {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    private List<LogResponse> logList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private LogAdapter mLogAdapter;
    private TextView TvNoLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_logs);

        context = getApplicationContext();

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_logs);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_show_logs);
        TvNoLog = (TextView) findViewById(R.id.tv_no_log);

        mProgressBar.setVisibility(View.VISIBLE);
        final DataService dataService = new DataService();
        dataService.GetAllLogFromUser("36a65953-8d12-46cd-9500-fc33e9123aaf").enqueue(new Callback<List<LogResponse>>() {
            @Override
            public void onResponse(Call<List<LogResponse>> call, Response<List<LogResponse>> response) {
                if (response.isSuccessful()) {
                    if(response.body().size() > 0){
                        mProgressBar.setVisibility(View.INVISIBLE);
                        logList = response.body();
                        if(logList.size() >= 1){
                            mLogAdapter = new LogAdapter(logList);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            mRecyclerView.setLayoutManager(layoutManager);
                            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                            mRecyclerView.addItemDecoration(new DividerItemDecoration(ShowLogsActivity.this, LinearLayoutManager.VERTICAL));
                            mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
                                @Override
                                public void onClick(View view, int position) {
                                    LogResponse log = logList.get(position);
                                    Intent intent = new Intent(ShowLogsActivity.this, UpdatePersonActivity.class);
                                    intent.putExtra("logfile", log);
                                    startActivity(intent);
                                }

                                @Override
                                public void onLongClick(View view, int position) {
                                    final LogResponse log = logList.get(position);

                                    new AlertDialog.Builder(ShowLogsActivity.this)
                                            .setTitle("Xóa")
                                            .setMessage("Bạn có chắc chắn muốn xóa ?")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    dataService.DeactiveLog(log.id).enqueue(new Callback<MessageResponse>() {
                                                        @Override
                                                        public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                                                            finish();
                                                            startActivity(getIntent());
                                                        }

                                                        @Override
                                                        public void onFailure(Call<MessageResponse> call, Throwable t) {
                                                            new AlertDialog.Builder(ShowLogsActivity.this)
                                                                    .setTitle("Thất bại")
                                                                    .setMessage("Xóa thất bại")
                                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                                    .setNegativeButton(android.R.string.cancel, null).show();
                                                        }
                                                    });
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, null).show();
                                }
                            }));

                            mRecyclerView.setAdapter(mLogAdapter);
                        }
                    }else{
                        mProgressBar.setVisibility(View.INVISIBLE);
                        TvNoLog.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onFailure(Call<List<LogResponse>> call, Throwable t) {

            }
        });
    }
}
