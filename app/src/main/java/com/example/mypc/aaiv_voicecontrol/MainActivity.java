package com.example.mypc.aaiv_voicecontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.mypc.aaiv_voicecontrol.data_model.CreateLogResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.IdentifyResult;
import com.example.mypc.aaiv_voicecontrol.services.DataService;
import com.example.mypc.aaiv_voicecontrol.services.MainServices;
import com.example.mypc.aaiv_voicecontrol.services.SpeechServices;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final int SPEECH_RECOGNITION_CODE = 1;
    private final String FACE_RECOGNITION_MODE = "face";
    private final String OBJECT_RECOGNITION_MODE = "object";
    private final String VIEW_RECOGNITION_MODE = "view";
    private final String REPEAT = "repeat";
    private final String ADD_PERSON_VIEW = "new person";
    private final String AFFIRMATIVE = "yes";
    private final String NEGATIVE = "no";
    private final String SHOW_LOGS = "history";

    private final int PERSON_DETECTED_SUCCESSFULLY = 1;
    private final int PERSON_DETECTED_FAILED = 2;
    private final int NO_PERSON_DETECTED = 3;

    private MainServices mainServices = new MainServices();
    Map uploadResult = null;
    private Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "debwqzo2g",
            "api_key", "852288139213848",
            "api_secret", "qsuCuMnpTZ11_WxuIuQ5kPZmdr4"));

    private TextView txtOutput;
    private FloatingActionButton fab;
    private ImageView iv_preview;
    private ImageButton mVoiceButton;
    private TextView mTvResult;

    private File returnCompressedImageFile;

    String imgUrl;

    SpeechServices mSpeechServices = new SpeechServices();
    String result = "";
    IdentifyResult identifyResult;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    String capture_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startBackgroundThread();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Constants.setApiHost("192.168.43.51");
        Toast.makeText(this, Constants.getApiHost(), Toast.LENGTH_LONG).show();

        iv_preview = (ImageView) findViewById(R.id.iv_preview);
        mTvResult = (TextView) findViewById(R.id.txtResult);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            returnCompressedImageFile = (File) bundle.get("imagefile");
            if(returnCompressedImageFile != null){
                Glide.with(this).load(returnCompressedImageFile.getAbsolutePath()).into(iv_preview);
                mBackgroundHandler.post(new ImageUploader(cloudinary, returnCompressedImageFile));
            }
        }

        mVoiceButton = (ImageButton) findViewById(R.id.button_voice);
        mVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText("Sẵn sàng");
            }
        });
    }

    private class ImageUploader implements Runnable {
        private Cloudinary mCloudinary;
        private File mImageFile;

        public ImageUploader(Cloudinary mCloudinary, File mImageFile) {
            this.mCloudinary = mCloudinary;
            this.mImageFile = mImageFile;
        }

        @Override
        public void run() {
            try {
                uploadResult = mCloudinary.uploader().upload(mImageFile, ObjectUtils.emptyMap());
                if (uploadResult != null) {
                    imgUrl = (String) uploadResult.get("url");
                    Log.d("url", imgUrl);

                    Intent intent = getIntent();
                    if (intent != null && intent.getExtras() != null) {
                        capture_mode = intent.getStringExtra("capture_mode");
                        switch (capture_mode) {
                            case FACE_RECOGNITION_MODE: {
                                identifyResult = mainServices.IdentifyPerson(imgUrl);
                                result = identifyResult.getIdentifyResponse();
                                int status = identifyResult.getIdentifyStatus();

                                mTvResult.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSpeechServices.sendGet(result);
                                        mTvResult.setText(result);
                                    }
                                });

                                if(status == PERSON_DETECTED_FAILED){
                                    startSpeechToText("Bạn có muốn thêm người này ?");
                                }

                                break;
                            }
                            case OBJECT_RECOGNITION_MODE: {
                                result = mainServices.DetectObject(imgUrl);
                                mTvResult.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSpeechServices.sendGet(result);
                                        mTvResult.setText(result);
                                    }
                                });
                                break;
                            }
                            case VIEW_RECOGNITION_MODE: {
                                result = mainServices.DetectVision(imgUrl).description.captions.get(0).text;
                                mTvResult.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSpeechServices.sendGet(result);
                                        mTvResult.setText(result);
                                    }
                                });
                                break;
                            }

                            default:
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startSpeechToText(String promt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, promt);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, "9000");

        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Speech recognition is not support for this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> results = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String text = results.get(0).toLowerCase();
                    switch (text) {
                        case FACE_RECOGNITION_MODE: {
                            Intent intent = new Intent(this, CameraActivity_2.class);
                            intent.putExtra("capture_mode", FACE_RECOGNITION_MODE);
                            startActivity(intent);
                            break;
                        }
                        case OBJECT_RECOGNITION_MODE: {
                            Intent intent = new Intent(this, CameraActivity_2.class);
                            intent.putExtra("capture_mode", OBJECT_RECOGNITION_MODE);
                            startActivity(intent);
                            break;
                        }
                        case VIEW_RECOGNITION_MODE: {
                            Intent intent = new Intent(this, CameraActivity_2.class);
                            intent.putExtra("capture_mode", VIEW_RECOGNITION_MODE);
                            startActivity(intent);
                            break;
                        }
                        case REPEAT:{
                            mSpeechServices.sendGet(result);
                            break;
                        }
                        case ADD_PERSON_VIEW:{
                            Intent intent = new Intent(this, AddPersonActivity.class);
                            startActivity(intent);
                        }
                        case AFFIRMATIVE:{
                            DataService dataService = new DataService();
                            dataService.CreateLog(imgUrl).enqueue(new Callback<CreateLogResponse>() {
                                @Override
                                public void onResponse(Call<CreateLogResponse> call, Response<CreateLogResponse> response) {
                                    if(response.isSuccessful()){
                                        Toast.makeText(MainActivity.this, "Log created at " + response.body().response.createdate, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<CreateLogResponse> call, Throwable t) {
                                    Log.d("createLog", "Create Log Fail");
                                    t.printStackTrace();
                                }
                            });
                            break;
                        }
                        case NEGATIVE:{
                            MainServices m = new MainServices();
                            m.DetectObject("http://res.cloudinary.com/debwqzo2g/image/upload/v1487612998/sample.jpg");
                            break;
                        }
                        case SHOW_LOGS:{
                            DataService dataService = new DataService();
                            dataService.GetAllLogFromUser("36a65953-8d12-46cd-9500-fc33e9123aaf").enqueue(new Callback<List<LogResponse>>() {
                                @Override
                                public void onResponse(Call<List<LogResponse>> call, Response<List<LogResponse>> response) {
                                    if(response.isSuccessful()){
                                        Intent intent = new Intent(MainActivity.this, ShowLogsActivity.class);
                                        Bundle args = new Bundle();
                                        args.putSerializable("loglist", (Serializable) response.body());
                                        intent.putExtra("bundle", args);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<LogResponse>> call, Throwable t) {

                                }
                            });
                            break;
                        }
                        default:
                            startSpeechToText("Không hỗ trợ: " + text);
                            break;
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        Log.d("thread", "start background thread");
        mBackgroundThread = new HandlerThread("MainActivityBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
}








