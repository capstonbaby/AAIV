package com.example.mypc.aaiv_voicecontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class StartUpActivity extends AppCompatActivity {


    private TextToSpeech mTextToSpeech;
    private String textDetectResult;
    private String requestString;
    private String resultString;
    private String requestLanguage = "vi-VN";
    private int processStep = 1;
    private Button button_start;
    private Button button_confirm;
    private Button button_retry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        SetUpText2Speech();

        button_start = (Button) findViewById(R.id.btn_Start);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Speak("Vui lòng cài đặt khẩu lệnh trước khi bắt đầu sử dụng!", "Start");
            }
        });

        button_confirm = (Button) findViewById(R.id.btn_confirm);
        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                processStep++;

                switch (processStep) {
                    case 2: {
                        getDetectPersonCommand();
                        break;
                    }
                    case 3: {
                        getDetectObjectCommand();
                        break;
                    }
                    case 4: {
                        getDetectViewCommand();
                        break;
                    }
                    case 5: {
                        getRepeatResultCommand();
                        break;

                    }
                    case 6: {
                        getNewPersonCommand();
                        break;

                    }
                    case 7: {
                        getAcceptCommand();
                        break;
                    }
                    case 8: {
                        getDenyCommand();
                        break;

                    }
                    case 9: {
                        getShowLogCommand();
                        break;
                    }
                    default: {
                        showResult();
                        break;
                    }
                }
            }
        });

        button_retry = (Button) findViewById(R.id.btn_retry);
        button_retry.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                switch (processStep) {
                                                    case 1: {
                                                        getShootingCommand();
                                                        break;
                                                    }
                                                    case 2: {
                                                        getDetectPersonCommand();
                                                        break;
                                                    }
                                                    case 3: {
                                                        getDetectObjectCommand();
                                                        break;
                                                    }
                                                    case 4: {
                                                        getDetectViewCommand();
                                                        break;
                                                    }
                                                    case 5: {
                                                        getRepeatResultCommand();
                                                        break;

                                                    }
                                                    case 6: {
                                                        getNewPersonCommand();
                                                        break;

                                                    }
                                                    case 7: {
                                                        getAcceptCommand();
                                                        break;
                                                    }
                                                    case 8: {
                                                        getDenyCommand();
                                                        break;

                                                    }
                                                    case 9: {
                                                        getShowLogCommand();
                                                        break;
                                                    }
                                                }
                                            }
                                        }
        );


    }


    protected void getShootingCommand() {
        requestString = "Đọc khẩu lệnh để chụp ảnh!";
        button_start.post(new Runnable() {
            @Override
            public void run() {
                button_start.setVisibility(View.INVISIBLE);
                showGetVoice(requestString);
                Speak(requestString, "1");
            }
        });

    }

    protected void getDetectPersonCommand() {
        requestString = "Đọc khẩu lệnh để nhận diện người!";
        showGetVoice(requestString);
        Speak(requestString, "2");

    }

    protected void getDetectObjectCommand() {
        requestString = "Đọc khẩu lệnh để nhận diện vật thể!";
        showGetVoice(requestString);
        Speak(requestString, "3");
    }

    protected void getDetectViewCommand() {
        requestString = "Đọc khẩu lệnh để nhận diện khung cảnh!";
        showGetVoice(requestString);
        Speak(requestString, "4");
    }

    protected void getRepeatResultCommand() {
        requestString = "Đọc khẩu lệnh để lặp lại kết quả!";
        showGetVoice(requestString);
        Speak(requestString, "5");
    }

    protected void getNewPersonCommand() {
        requestString = "Đọc khẩu lệnh để thêm người quen!";
        showGetVoice(requestString);
        Speak(requestString, "6");
    }

    protected void getAcceptCommand() {
        requestString = "Đọc khẩu lệnh để chọn xác nhận!";
        showGetVoice(requestString);
        Speak(requestString, "7");
    }

    protected void getDenyCommand() {
        requestString = "Đọc khẩu lệnh để chọn hủy!";
        showGetVoice(requestString);
        Speak(requestString, "8");
    }

    protected void getShowLogCommand() {
        requestString = "Đọc khẩu lệnh để xem lịch sử!";
        showGetVoice(requestString);
        Speak(requestString, "9");
    }

    protected void showResult() {

        TextView temp = new TextView(this);

        temp = (TextView) findViewById(R.id.tv_shootingCommand);
        temp.setText(Constants.getShootingCommand());

        temp = (TextView) findViewById(R.id.tv_detectPersonCommand);
        temp.setText(Constants.getDetectPersonCommand());

        temp = (TextView) findViewById(R.id.tv_detectObjectCommand);
        temp.setText(Constants.getDetectObjectCommand());

        temp = (TextView) findViewById(R.id.tv_detectViewCommand);
        temp.setText(Constants.getDetectViewCommand());

        temp = (TextView) findViewById(R.id.tv_repeatResultCommand);
        temp.setText(Constants.getRepeatResultCommand());

        temp = (TextView) findViewById(R.id.tv_newPersonCommand);
        temp.setText(Constants.getNewPersonCommand());

        temp = (TextView) findViewById(R.id.tv_acceptCommand);
        temp.setText(Constants.getAcceptCommand());

        temp = (TextView) findViewById(R.id.tv_denyCommand);
        temp.setText(Constants.getDenyCommand());

        temp = (TextView) findViewById(R.id.tv_showLogCommand);
        temp.setText(Constants.getShowLogCommand());

        TableLayout tableResult = (TableLayout) findViewById(R.id.tb_result);
        tableResult.setVisibility(View.VISIBLE);

        //Hide Confirmation
        TextView tv_temp;
        tv_temp = (TextView) findViewById(R.id.tv_confirmation);
        hideTextView(tv_temp);
        tv_temp = (TextView) findViewById(R.id.tv_contentConfirmation);
        hideTextView(tv_temp);

        hideButton();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            ArrayList<String> results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String text = results.get(0).toLowerCase();
            //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            textDetectResult = text;
            switch (requestCode) {
                case 1: {
                    Constants.setShootingCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    //Toast.makeText(StartUpActivity.this,resultString, Toast.LENGTH_SHORT).show();
                    break;
                }

                case 2: {
                    Constants.setDetectPersonCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    // Toast.makeText(StartUpActivity.this,resultString, Toast.LENGTH_SHORT).show();
                    break;
                }
                case 3: {
                    Constants.setDetectObjectCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    //Toast.makeText(StartUpActivity.this, resultString, Toast.LENGTH_SHORT).show();
                    break;
                }
                case 4: {
                    Constants.setDetectViewCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    //Toast.makeText(StartUpActivity.this, resultString, Toast.LENGTH_SHORT).show();
                    break;
                }
                case 5: {
                    Constants.setRepeatResultCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    //Toast.makeText(StartUpActivity.this, resultString, Toast.LENGTH_SHORT).show();
                    break;
                }
                case 6: {
                    Constants.setNewPersonCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    //Toast.makeText(StartUpActivity.this, resultString, Toast.LENGTH_SHORT).show();
                    break;
                }
                case 7: {
                    Constants.setAcceptCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    //Toast.makeText(StartUpActivity.this, resultString, Toast.LENGTH_SHORT).show();
                    break;
                }
                case 8: {
                    Constants.setDenyCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    break;
                }
                case 9: {
                    Constants.setShowLogCommand(textDetectResult);
                    resultString = textDetectResult;
                    showConfirm(resultString);
                    break;
                }
                case 0: {
                    if (resultString.equals(textDetectResult)) {
                        Speak("Khẩu lệnh trùng khớp", "matched");
                        break;
                    } else {
                        Speak("Khẩu lệnh không trùng khớp", "no_matched");
                        break;
                    }
                }
            }
        }
    }

    protected void hideTextView(TextView tv) {
        tv.setVisibility(View.INVISIBLE);
    }

    protected void showTextView(TextView tv) {
        tv.setVisibility(View.VISIBLE);
    }

    protected void showButton() {
        Button button = new Button(this);

        button = (Button) findViewById(R.id.btn_confirm);
        button.setVisibility(View.VISIBLE);

        button = (Button) findViewById(R.id.btn_retry);
        button.setVisibility(View.VISIBLE);
    }

    protected void hideButton() {
        Button button = new Button(this);

        button = (Button) findViewById(R.id.btn_confirm);
        button.setVisibility(View.INVISIBLE);

        button = (Button) findViewById(R.id.btn_retry);
        button.setVisibility(View.INVISIBLE);
    }

    protected void showConfirm(String content) {

        Speak("Vui lòng đọc lại khẩu lệnh", "confirm");
    }

    protected void showGetVoice(String content) {

        TextView tv_temp = new TextView(this);
        hideButton();

        tv_temp = (TextView) findViewById(R.id.tv_setupTitle);
        showTextView(tv_temp);
        tv_temp = (TextView) findViewById(R.id.tv_contentTitle);
        tv_temp.setText(content);
        showTextView(tv_temp);

        tv_temp = (TextView) findViewById(R.id.tv_confirmation);
        hideTextView(tv_temp);
        tv_temp = (TextView) findViewById(R.id.tv_contentConfirmation);
        hideTextView(tv_temp);

    }

//    private void SetUpText2Speech() {
//        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status != TextToSpeech.ERROR) {
//                    mTextToSpeech.setLanguage(new Locale("vi", "VN"));
//                    Log.d("setupt2s", "Setup finished");
//                } else if (status == TextToSpeech.ERROR) {
//                    Toast.makeText(StartUpActivity.this, "Setup Speech Failed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

//    private void Speak(String text) {
//        if (mTextToSpeech != null) {
//            //mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH,null);
//        }
//    }

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
                                case "Start": {
                                    getShootingCommand();
                                    break;
                                }
                                case "1": {
                                    startSpeechToText(requestString, requestLanguage, 1);
                                    break;
                                }
                                case "2": {
                                    startSpeechToText(requestString, requestLanguage, 2);
                                    break;
                                }
                                case "3": {
                                    startSpeechToText(requestString, requestLanguage, 3);
                                    break;
                                }
                                case "4": {
                                    startSpeechToText(requestString, requestLanguage, 4);
                                    break;

                                }
                                case "5": {
                                    startSpeechToText(requestString, requestLanguage, 5);
                                    break;

                                }
                                case "6": {
                                    startSpeechToText(requestString, requestLanguage, 6);
                                    break;
                                }
                                case "7": {
                                    startSpeechToText(requestString, requestLanguage, 7);
                                    break;
                                }
                                case "8": {
                                    startSpeechToText(requestString, requestLanguage, 8);
                                    break;
                                }
                                case "9": {
                                    startSpeechToText(requestString, requestLanguage, 9);
                                    break;
                                }
                                case "confirm": {
                                    startSpeechToText(requestString, requestLanguage, 0);
                                    break;
                                }
                                case "matched": {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            button_confirm.performClick();
                                        }
                                    });
                                    break;
                                }
                                case "no_matched": {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            button_retry.performClick();

                                        }
                                    });
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
                    Toast.makeText(StartUpActivity.this, "Setup Speech Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void Speak(String text, final String request) {
        if (mTextToSpeech != null) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, request);
        }
    }

    public void startSpeechToText(String promt, String language, int SPEECH_CODE) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, promt);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, "9000");

        try {
            startActivityForResult(intent, SPEECH_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Speech recognition is not support for this device.", Toast.LENGTH_SHORT).show();

        }
    }
}
