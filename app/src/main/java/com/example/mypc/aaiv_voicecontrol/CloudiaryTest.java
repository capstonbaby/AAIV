package com.example.mypc.aaiv_voicecontrol;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;

public class CloudiaryTest extends AppCompatActivity {

    private TextView mTextView;
    private int i = 0;
    final Timer time = new Timer();

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_2);

        context = getApplicationContext();



        CameraStreamFragment fragment = CameraStreamFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.container,fragment);
        fragmentTransaction.commit();

    }


    public void CancelTimer(View view) {
        time.cancel();
    }


}
