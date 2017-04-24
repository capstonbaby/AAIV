package com.example.mypc.aaiv_voicecontrol;

import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.services.DataService;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mypc.aaiv_voicecontrol.Constants.ADD_NEW_PERSON_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.CREATE_LOG_FILE;
import static com.example.mypc.aaiv_voicecontrol.Constants.FACE_RECOGNITION_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.OBJECT_RECOGNITION_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_LANGUAGE_VIE;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_ONDONE_CONFIRMATION;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_ONDONE_NOREQUEST;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_PERSON_NAME_CODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_RECOGNITION_CODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.VIEW_RECOGNITION_MODE;

public class CameraActivity_2 extends AppCompatActivity {

    private static Context context;


    public static Context getContext() {
        return context;
    }

    private TextToSpeech mTextToSpeech;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_2);

        context = getApplicationContext();

        SetUpText2Speech();

        //Shake Listener
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                if (count == 2) {
                    startSpeechToText("Sẵn sàng", SPEECH_RECOGNITION_CODE, SPEECH_LANGUAGE_VIE);
                }
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String capture_mode = intent.getStringExtra("capture_mode");

            if (null == savedInstanceState) {
                Camera2Fragment fragment = Camera2Fragment.newInstance();
                Bundle bundle = new Bundle();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                bundle.putString("capture_mode", capture_mode);
                fragment.setArguments(bundle);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, fragment);
                fragmentTransaction.commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            switch (requestCode) {
                case SPEECH_RECOGNITION_CODE: {
                    ArrayList<String> results = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String text = results.get(0).toLowerCase();
                    if (text.equals(Constants.getDetectPersonCommand().toLowerCase())) {
                        Intent intent = new Intent(this, CameraActivity_2.class);
                        intent.putExtra("capture_mode", FACE_RECOGNITION_MODE);
                        finish();
                        startActivity(intent);
                    } else if (text.equals(Constants.getDetectObjectCommand().toLowerCase())) {
                        Intent intent = new Intent(this, CameraActivity_2.class);
                        intent.putExtra("capture_mode", OBJECT_RECOGNITION_MODE);
                        finish();
                        startActivity(intent);
                    } else if (text.equals(Constants.getDetectViewCommand().toLowerCase())) {
                        Intent intent = new Intent(this, CameraActivity_2.class);
                        intent.putExtra("capture_mode", VIEW_RECOGNITION_MODE);
                        finish();
                        startActivity(intent);
                    } else if (text.equals(Constants.getNewPersonCommand().toLowerCase())) {
                        Intent intent = new Intent(this, AddPersonActivity.class);
                        intent.putExtra("mode", ADD_NEW_PERSON_MODE);
                        finish();
                        startActivity(intent);
                    } else if (text.equals(Constants.getShowLogCommand().toLowerCase())) {
                        Intent intent = new Intent(CameraActivity_2.this, ShowLogsActivity.class);
                        finish();
                        startActivity(intent);
                    } else if (text.equals("thoát")) {
                        Intent intent = new Intent(this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    } else if (text.equals(Constants.getStreamDetectCommand().toLowerCase())) {
                        Intent intent = new Intent(this, FaceTrackerActivity.class);
                        finish();
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Không hỗ trợ: " + text, Toast.LENGTH_SHORT).show();
                        Speak("Không hiểu lệnh", SPEECH_ONDONE_NOREQUEST);
                    }
                    break;
                }
                default:
                    break;
            }
        } else {
            Speak("Không hiểu lệnh", SPEECH_ONDONE_NOREQUEST);
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
                    Speak("Chạm vào màn hình để nhận diện", SPEECH_ONDONE_NOREQUEST);
                    Log.d("setupt2s", "Setup finished");
                } else if (status == TextToSpeech.ERROR) {
                    Toast.makeText(CameraActivity_2.this, "Setup Speech Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void Speak(String text, final String request) {
        if (mTextToSpeech != null) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, request);
        }
    }
}
