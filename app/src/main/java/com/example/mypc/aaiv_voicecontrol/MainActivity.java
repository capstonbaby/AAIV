package com.example.mypc.aaiv_voicecontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.IdentifyResult;
import com.example.mypc.aaiv_voicecontrol.services.DataService;
import com.example.mypc.aaiv_voicecontrol.services.MainServices;
import com.example.mypc.aaiv_voicecontrol.services.SpeechServices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mypc.aaiv_voicecontrol.Constants.ACCEPT_COMMAND;
import static com.example.mypc.aaiv_voicecontrol.Constants.ADD_NEW_PERSON_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.CREATE_LOG_FILE;
import static com.example.mypc.aaiv_voicecontrol.Constants.DENY_COMMAND;
import static com.example.mypc.aaiv_voicecontrol.Constants.DETECT_OBJECT_COMMAND;
import static com.example.mypc.aaiv_voicecontrol.Constants.DETECT_PERSON_COMMAND;
import static com.example.mypc.aaiv_voicecontrol.Constants.DETECT_VIEW_COMMAND;
import static com.example.mypc.aaiv_voicecontrol.Constants.FACE_RECOGNITION_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.NEW_PERSON_COMMAND;
import static com.example.mypc.aaiv_voicecontrol.Constants.NO_PERSON_DETECTED;
import static com.example.mypc.aaiv_voicecontrol.Constants.OBJECT_RECOGNITION_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.PERSON_DETECTED_FAILED;
import static com.example.mypc.aaiv_voicecontrol.Constants.PERSON_DETECTED_SUCCESSFULLY;
import static com.example.mypc.aaiv_voicecontrol.Constants.REPEAT_RESULT_COMMAND;
import static com.example.mypc.aaiv_voicecontrol.Constants.SHOW_LOG_COMMAND;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_LANGUAGE_VIE;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_ONDONE_CONFIRMATION;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_ONDONE_NOREQUEST;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_PERSON_NAME_CODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_RECOGNITION_CODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.STREAM_DETECT;
import static com.example.mypc.aaiv_voicecontrol.Constants.VIEW_RECOGNITION_MODE;

public class MainActivity extends AppCompatActivity {


    private MainServices mainServices = new MainServices();
    Map uploadResult = null;
    private Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "debwqzo2g",
            "api_key", "852288139213848",
            "api_secret", "qsuCuMnpTZ11_WxuIuQ5kPZmdr4"));

    private FloatingActionButton fab;
    private ImageView iv_preview;
    private ImageButton mVoiceButton;
    private TextView mTvResult;
    private EditText mTxtIp;

    private File returnCompressedImageFile;

    String imgUrl;

    private SpeechServices mSpeechServices = new SpeechServices();
    String result = "";
    IdentifyResult identifyResult;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    String capture_mode;

    private TextToSpeech mTextToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startBackgroundThread();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SetUpText2Speech();

        iv_preview = (ImageView) findViewById(R.id.iv_preview);
        mTvResult = (TextView) findViewById(R.id.txtResult);
        mTxtIp = (EditText) findViewById(R.id.et_ip);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            returnCompressedImageFile = (File) bundle.get("imagefile");
            if (returnCompressedImageFile != null) {
                Glide.with(this).load(returnCompressedImageFile.getAbsolutePath()).into(iv_preview);
                mBackgroundHandler.post(new ImageUploader(cloudinary, returnCompressedImageFile));
            }
        }

        mVoiceButton = (ImageButton) findViewById(R.id.button_voice);
        mVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText("Sẵn sàng", SPEECH_RECOGNITION_CODE, SPEECH_LANGUAGE_VIE);
            }
        });
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

    public void setIpAddress(View view) {
        String ip = String.valueOf(mTxtIp.getText());
        Constants.setApiHost(ip);
        Toast.makeText(this, Constants.getApiHost(), Toast.LENGTH_LONG).show();
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
                                if (identifyResult != null) {
                                    result = identifyResult.getIdentifyResponse();
                                    final int status = identifyResult.getIdentifyStatus();

                                    mTvResult.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTvResult.setText(result);
                                        }
                                    });

                                    switch (status) {
                                        case PERSON_DETECTED_FAILED: {
                                            Speak(result, SPEECH_ONDONE_CONFIRMATION);
                                            break;
                                        }
                                        case NO_PERSON_DETECTED: {
                                            Speak(result, SPEECH_ONDONE_NOREQUEST);
                                            break;
                                        }
                                        case PERSON_DETECTED_SUCCESSFULLY: {
                                            Speak(result, SPEECH_ONDONE_NOREQUEST);
                                            break;
                                        }
                                        default:
                                            break;
                                    }
                                } else {
                                    Speak("Nhận diện thất bại", SPEECH_ONDONE_NOREQUEST);
                                }

                                break;
                            }
                            case OBJECT_RECOGNITION_MODE: {
                                result = mainServices.DetectObject(imgUrl);
                                mTvResult.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTvResult.setText(result);
                                    }
                                });
                                //mSpeechServices.sendGet(result, mTextToSpeech);
                                mSpeechServices.sendPost(result);
                                break;
                            }
                            case VIEW_RECOGNITION_MODE: {
                                result = mainServices.DetectVision(imgUrl).description.captions.get(0).text;
                                mTvResult.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTvResult.setText(result);
                                    }
                                });
                                mSpeechServices.sendGet(result, mTextToSpeech);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> results = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String text = results.get(0).toLowerCase();
                    if (text.equals(DETECT_PERSON_COMMAND)) {
                        Intent intent = new Intent(this, CameraActivity_2.class);
                        intent.putExtra("capture_mode", FACE_RECOGNITION_MODE);
                        startActivity(intent);
                    } else if (text.equals(DETECT_OBJECT_COMMAND)) {
                        Intent intent = new Intent(this, CameraActivity_2.class);
                        intent.putExtra("capture_mode", OBJECT_RECOGNITION_MODE);
                        startActivity(intent);
                    } else if (text.equals(DETECT_VIEW_COMMAND)) {
                        Intent intent = new Intent(this, CameraActivity_2.class);
                        intent.putExtra("capture_mode", VIEW_RECOGNITION_MODE);
                        startActivity(intent);
                    } else if (text.equals(REPEAT_RESULT_COMMAND)) {
                        if (result != "") {
                            mSpeechServices.sendGet(result, mTextToSpeech);
                        }
                    } else if (text.equals(NEW_PERSON_COMMAND)) {
                        Intent intent = new Intent(this, AddPersonActivity.class);
                        intent.putExtra("mode", ADD_NEW_PERSON_MODE);
                        startActivity(intent);
                    } else if (text.equals(ACCEPT_COMMAND)) {
                        startSpeechToText("Hãy nói tên", SPEECH_PERSON_NAME_CODE, SPEECH_LANGUAGE_VIE);
                    } else if (text.equals(CREATE_LOG_FILE)) {
                        startSpeechToText("Hãy nói tên", SPEECH_PERSON_NAME_CODE, SPEECH_LANGUAGE_VIE);
                    } else if (text.equals(DENY_COMMAND)) {
                    } else if (text.equals(SHOW_LOG_COMMAND)) {
                        Intent intent = new Intent(MainActivity.this, ShowLogsActivity.class);
                        startActivity(intent);
                    } else if (text.equals(STREAM_DETECT)) {
                        Intent intent = new Intent(this, CloudiaryTest.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Không hỗ trợ: " + text, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case SPEECH_PERSON_NAME_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> results = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String text = results.get(0).toLowerCase();
                    Log.d("name", text);

                    DataService dataService = new DataService();
                    dataService.CreateLog(imgUrl, text).enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Log created at " + response.body().response.createdate, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {
                            Log.d("createLog", "Create Log Fail");
                            t.printStackTrace();
                        }
                    });
                }
                break;
            }
            default:
                break;
        }
    }

    public void startSpeechToText(String promt, int mode, String language) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, promt);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, "9000");

        try {
            Speak(promt, SPEECH_ONDONE_NOREQUEST);
            Thread.sleep(1000);
            startActivityForResult(intent, mode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Speech recognition is not support for this device.", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void SetUpText2Speech() {
        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            switch (utteranceId) {
                                case SPEECH_ONDONE_CONFIRMATION: {
                                    startSpeechToText("Bạn có muốn thêm người này ?", SPEECH_RECOGNITION_CODE, SPEECH_LANGUAGE_VIE);
                                    break;
                                }
                                case SPEECH_ONDONE_NOREQUEST: {
                                    break;
                                }
                                default:
                                    break;
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });

                    mTextToSpeech.setLanguage(new Locale("vi", "VN"));

                    Log.d("setupt2s", "Setup finished");
                } else if (status == TextToSpeech.ERROR) {
                    Toast.makeText(MainActivity.this, "Setup Speech Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void Speak(String text, final String request) {
        if (mTextToSpeech != null) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, request);
        }
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








