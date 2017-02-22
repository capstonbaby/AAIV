package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;

import java.util.List;

public class ShowLogsActivity extends AppCompatActivity {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    private List<LogResponse> logList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_logs);

        context = getApplicationContext();

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if(bundle != null){
            logList = (List<LogResponse>) bundle.getSerializable("loglist");
        }
    }
}
