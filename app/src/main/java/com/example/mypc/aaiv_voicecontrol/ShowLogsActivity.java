package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;

import java.util.ArrayList;
import java.util.List;

public class ShowLogsActivity extends AppCompatActivity {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    private List<LogResponse> logList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private LogAdapter mLogAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_logs);

        context = getApplicationContext();

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if(bundle != null){
            logList = (List<LogResponse>) bundle.getSerializable("loglist");
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_logs);

        if(logList.size() >= 1){
            mLogAdapter = new LogAdapter(logList);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mLogAdapter);
        }
    }
}
