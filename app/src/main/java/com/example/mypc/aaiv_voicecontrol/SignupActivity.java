package com.example.mypc.aaiv_voicecontrol;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mypc.aaiv_voicecontrol.data_model.ResponseModel;
import com.example.mypc.aaiv_voicecontrol.services.DataService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    EditText email, password, confirm_password;
    TextView lin, sup, error;
    ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sup = (TextView) findViewById(R.id.sup);
        lin = (TextView) findViewById(R.id.lin);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        error = (TextView) findViewById(R.id.tv_signup_error);
        pbLoading = (ProgressBar) findViewById(R.id.pb_signup);


        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");

        sup.setTypeface(custom_font1);
        email.setTypeface(custom_font);
        password.setTypeface(custom_font);
        lin.setTypeface(custom_font);
        confirm_password.setTypeface(custom_font);

        lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(it);
            }
        });

        sup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataService service = new DataService();
                pbLoading.setVisibility(View.VISIBLE);
                service.Register(email.getText().toString(), password.getText().toString(), confirm_password.getText().toString())
                        .enqueue(new Callback<ResponseModel>() {
                            @Override
                            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                if (response.isSuccessful()) {
                                    ResponseModel registerResponse = response.body();
                                    if (registerResponse.success) {
                                        pbLoading.setVisibility(View.INVISIBLE);
                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    } else {
                                        pbLoading.setVisibility(View.INVISIBLE);
                                        error.setText(registerResponse.error);
                                        error.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseModel> call, Throwable t) {
                                pbLoading.setVisibility(View.INVISIBLE);
                                error.setText("Đã có lỗi xảy ra, vui lòng thử lại sau");
                                error.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to quit.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}