package com.example.mypc.aaiv_voicecontrol;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mypc.aaiv_voicecontrol.data_model.LoginResponse;
import com.example.mypc.aaiv_voicecontrol.services.DataService;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText pswd, usrusr;
    TextView sup, lin, error;
    ProgressBar pb_login;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Constants.setApiHost("192.168.0.34");

        sessionManager = new SessionManager(getApplicationContext());
        Log.d("isLogin", sessionManager.isLoggedIn() ? "logged in" : "not logged in");
        Log.d("isLogin", Constants.getPersonGroupId());
        Log.d("isLogin", Constants.getUserId());

        if(sessionManager.isLoggedIn()){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }


        lin = (TextView) findViewById(R.id.lin);
        usrusr = (EditText) findViewById(R.id.usrusr);
        pswd = (EditText) findViewById(R.id.pswrdd);
        sup = (TextView) findViewById(R.id.sup);
        error = (TextView) findViewById(R.id.tv_login_error);
        pb_login = (ProgressBar) findViewById(R.id.pb_login);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");

        lin.setTypeface(custom_font1);
        sup.setTypeface(custom_font);
        usrusr.setTypeface(custom_font);
        pswd.setTypeface(custom_font);
        sup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(it);
            }
        });
        lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb_login.setVisibility(View.VISIBLE);
                new DataService().Login(usrusr.getText().toString(), pswd.getText().toString()).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if(response.isSuccessful()){
                            if(response.body().success){
                                String personGroupId = response.body().data.personGroupId;
                                String userId = response.body().data.userId;
                                String username = response.body().data.username;

                                sessionManager.CreateLoginSession(personGroupId, userId, username);

                                pb_login.setVisibility(View.INVISIBLE);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                pb_login.setVisibility(View.INVISIBLE);
                                error.setText(response.body().message);
                                error.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        pb_login.setVisibility(View.INVISIBLE);
                        error.setText("Có lỗi xảy ra, vui lòng thử lại sau");
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}