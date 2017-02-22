package com.example.mypc.aaiv_voicecontrol;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class CameraActivity_2 extends AppCompatActivity {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_2);

        context = getApplicationContext();

        //Constants.setApiHost("192.168.1.99");
        Toast.makeText(this, Constants.getApiHost(), Toast.LENGTH_LONG).show();

        Intent intent = getIntent();
        if(intent != null && intent.getExtras() != null){
            String capture_mode = intent.getStringExtra("capture_mode");

            if (null == savedInstanceState) {
                Camera2Fragment fragment = Camera2Fragment.newInstance();
                Bundle bundle = new Bundle();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                bundle.putString("capture_mode", capture_mode);
                fragment.setArguments(bundle);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container,fragment);
                fragmentTransaction.commit();
            }
        }
    }
}
