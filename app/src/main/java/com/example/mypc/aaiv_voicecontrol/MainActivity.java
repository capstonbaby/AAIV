package com.example.mypc.aaiv_voicecontrol;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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
import com.example.mypc.aaiv_voicecontrol.services.DataService;
import com.example.mypc.aaiv_voicecontrol.services.MainServices;
import com.example.mypc.aaiv_voicecontrol.services.ObjectService;
import com.example.mypc.aaiv_voicecontrol.services.SpeechServices;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mypc.aaiv_voicecontrol.Constants.ADD_NEW_PERSON_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.ADD_PERSON_VIEW;
import static com.example.mypc.aaiv_voicecontrol.Constants.AFFIRMATIVE;
import static com.example.mypc.aaiv_voicecontrol.Constants.CREATE_LOG_FILE;
import static com.example.mypc.aaiv_voicecontrol.Constants.FACE_RECOGNITION_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.NEGATIVE;
import static com.example.mypc.aaiv_voicecontrol.Constants.OBJECT_RECOGNITION_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.PERSON_DETECTED_FAILED;
import static com.example.mypc.aaiv_voicecontrol.Constants.PERSON_DETECTED_SUCCESSFULLY;
import static com.example.mypc.aaiv_voicecontrol.Constants.PersonGroupId;
import static com.example.mypc.aaiv_voicecontrol.Constants.REPEAT;
import static com.example.mypc.aaiv_voicecontrol.Constants.SHOW_LOGS;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_LANGUAGE_VIE;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_ONDONE_CONFIRMATION;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_ONDONE_NOREQUEST;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_PERSON_NAME_CODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_RECOGNITION_CODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.STREAM_DETECT;
import static com.example.mypc.aaiv_voicecontrol.Constants.VIEW_RECOGNITION_MODE;

public class MainActivity extends AppCompatActivity {

    public static Intent newInstance(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    private SharedPreferences permissionStatus;

    private SessionManager session;

    private MainServices mainServices = new MainServices();
    private Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "debwqzo2g",
            "api_key", "852288139213848",
            "api_secret", "qsuCuMnpTZ11_WxuIuQ5kPZmdr4"));

    private ImageView iv_preview;
    private ImageButton mVoiceButton;
    private TextView mTvResult;
    private EditText mTxtIp;

    private File returnCompressedImageFile;

    private String imgUrl;

    private SpeechServices mSpeechServices = new SpeechServices();
    String result = "";

    String capture_mode;

    private TextToSpeech mTextToSpeech;

    private int status;
    private String personDetectResultText = "";
    private String personIdentifyResultText = "";

    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.nvNavigation)
    NavigationView nvNavigation;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        session = new SessionManager(getApplicationContext());
        Log.d("isLogin", session.isLoggedIn() ? "logged in" : "not logged in");

        session.checkLoggedIn();


        HashMap<String, String> user = session.getUserDetails();
        Constants.setPersonGroupId(user.get(session.KEY_PERSON_GROUP_ID));
        Constants.setUserId(user.get(session.KEY_USER_ID));
        Constants.setUsername(user.get(session.KEY_USERNAME));

        initDrawer();
        SetUpText2Speech();

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        AskPermissions();

        iv_preview = (ImageView) findViewById(R.id.iv_preview);
        mTvResult = (TextView) findViewById(R.id.txtResult);
        mTxtIp = (EditText) findViewById(R.id.et_ip);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            returnCompressedImageFile = (File) bundle.get("imagefile");
            capture_mode = bundle.getString("capture_mode");
            if (returnCompressedImageFile != null) {
                Glide.with(this).load(returnCompressedImageFile.getAbsolutePath()).into(iv_preview);
                //mBackgroundHandler.post(new ImageUploader(cloudinary, returnCompressedImageFile));
                new Detector(returnCompressedImageFile).execute();
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

    private void initDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        nvNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
        View header = nvNavigation.getHeaderView(0);
        TextView tvUsername = (TextView) header.findViewById(R.id.tvUsername);
        tvUsername.setText(Constants.getUsername());

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.open, R.string.close);
        drawer.addDrawerListener(actionBarDrawerToggle);
    }

    private void selectDrawerItem(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.setting:
                intent = new Intent(this, StartUpActivity.class);
                startActivity(intent);
                break;
            case R.id.logs:
                intent = new Intent(this, ShowLogsActivity.class);
                startActivity(intent);
                break;
            case R.id.quota:
                break;
            case R.id.sign_out:
                session.logoutUser();
                break;
        }
        drawer.closeDrawers();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void setIpAddress(View view) {
        String ip = String.valueOf(mTxtIp.getText());
        Constants.setApiHost(ip);
        Toast.makeText(this, Constants.getApiHost(), Toast.LENGTH_LONG).show();
    }

    private class Detector extends AsyncTask<Void, Void, Void> {
        private File mImageFile;

        public Detector(File mImageFile) {
            this.mImageFile = mImageFile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            new Uploader(mImageFile).execute();
            return null;
        }
    }

    public class Uploader extends AsyncTask<Void, Void, Map> {

        private File compressedFile;

        public Uploader(File compressedFile) {
            this.compressedFile = compressedFile;
        }

        @Override
        protected Map doInBackground(Void... params) {
            try {
                return cloudinary.uploader().upload(compressedFile, ObjectUtils.emptyMap());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Map map) {
            if (map != null) {
                String url = (String) map.get("url");
                imgUrl = url;
                switch (capture_mode) {
                    case FACE_RECOGNITION_MODE: {
                        new FaceDetection().execute(url);
                        break;
                    }
                    case OBJECT_RECOGNITION_MODE: {
                        Log.d("imgurl", imgUrl);
                        new DetectObject(imgUrl).execute();
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
    }

    private class FaceDetection extends AsyncTask<String, String, Face[]> {

        @Override
        protected Face[] doInBackground(String... params) {
            FaceServiceClient client = Constants.getmFaceServiceClient();
            Log.d("identify", "Detecting");
            try {

                return client.detect(
                        params[0],
                        true,
                        false,
                        new FaceServiceClient.FaceAttributeType[]{
                                FaceServiceClient.FaceAttributeType.Gender
                        }
                );
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Face[] faces) {
            if (faces != null || faces.length != 0) {
                List<UUID> faceids = new ArrayList<>();
                for (Face face : faces) {
                    faceids.add(face.faceId);
                }

                new FaceIdentify(PersonGroupId).execute(faceids.toArray(new UUID[faceids.size()]));

                if (faces.length == 1) {
                    personDetectResultText = "Có một người " + (faces[0].faceAttributes.gender.equals("male") ? "đàn ông" : "phụ nữ");
                } else if (faces.length > 1) {
                    personDetectResultText = "Có " + faces.length + "người";
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvResult.setText("Không có ai cả");
                    }
                });
                Speak("Không có ai cả", SPEECH_ONDONE_NOREQUEST);
            }
        }
    }

    private class FaceIdentify extends AsyncTask<UUID, Void, com.microsoft.projectoxford.face.contract.IdentifyResult[]> {

        String mPersonGroupId;

        public FaceIdentify(String mPersonGroupId) {
            this.mPersonGroupId = mPersonGroupId;
        }

        @Override
        protected com.microsoft.projectoxford.face.contract.IdentifyResult[] doInBackground(UUID... params) {
            Log.d("identify", "Identifying");

            FaceServiceClient client = Constants.getmFaceServiceClient();
            try {
                return client.identity(
                        this.mPersonGroupId,
                        params,
                        1
                );
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(com.microsoft.projectoxford.face.contract.IdentifyResult[] identifyResults) {
            if (identifyResults != null) {
                new PersonInfo(identifyResults).execute();
            }
        }
    }

    public class PersonInfo extends AsyncTask<Void, Void, Void> {

        com.microsoft.projectoxford.face.contract.IdentifyResult[] identifyResults;

        public PersonInfo(com.microsoft.projectoxford.face.contract.IdentifyResult[] identifyResults) {
            this.identifyResults = identifyResults;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("identify", "Person");

            final FaceServiceClient client = Constants.getmFaceServiceClient();
            status = PERSON_DETECTED_FAILED;

            try {

                for (final com.microsoft.projectoxford.face.contract.IdentifyResult identifyResult :
                        identifyResults) {
                    if (identifyResult.candidates.size() > 0) {
                        final String personname = client.getPerson(PersonGroupId, identifyResult.candidates.get(0).personId).name;
                        personIdentifyResultText += personname + ", ";

                        status = PERSON_DETECTED_SUCCESSFULLY;
                    }
                }
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            switch (status) {
                case PERSON_DETECTED_SUCCESSFULLY: {
                    mTvResult.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText(personIdentifyResultText);
                            Speak(personIdentifyResultText, SPEECH_ONDONE_NOREQUEST);
                        }
                    });
                    break;
                }
                case PERSON_DETECTED_FAILED: {
                    mTvResult.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText(personDetectResultText);
                            Speak(personDetectResultText, SPEECH_ONDONE_CONFIRMATION);
                        }
                    });
                    break;
                }
                default: {
                    mTvResult.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText("Nhận diện thất bại");
                            Speak("Nhận diện thất bại", SPEECH_ONDONE_NOREQUEST);
                        }
                    });
                    break;
                }
            }

        }

    }

    public class DetectObject extends AsyncTask<Void, Void, String>{

        private String url;

        public DetectObject(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            String returnValue = "";

            ObjectService objectService = new ObjectService();
            try {
                Response<ResponseBody> response = objectService.DetectObject(url).execute();

                String jsonStr = response.body().string();
                Log.i("jsonStr", jsonStr);
                if(jsonStr.trim().equals("error")){
                    objectService.CreateLog(url).enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                            Log.d("createLog", "onReponse function");
                        }

                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {
                            Log.d("createLog", "Create Log Fail");
                            t.printStackTrace();
                        }
                    });
                    returnValue = "Không xác định được vật thể";
                }else{
                    returnValue = jsonStr;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("detectobject", "error when detect object MainService");
                returnValue = "Máy chủ bị lỗi";
            }

            return returnValue;
        }

        @Override
        protected void onPostExecute(final String returnValue) {
            super.onPostExecute(returnValue);
            if(returnValue != null){
                mTvResult.post(new Runnable() {
                    @Override
                    public void run() {
                        mTvResult.setText(returnValue);
                    }
                });
                mSpeechServices.sendPost(returnValue);
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
                        case REPEAT: {
                            if (result != "") {
                                mSpeechServices.sendGet(result, mTextToSpeech);
                            }
                            break;
                        }
                        case ADD_PERSON_VIEW: {
                            Intent intent = new Intent(this, AddPersonActivity.class);
                            intent.putExtra("mode", ADD_NEW_PERSON_MODE);
                            startActivity(intent);
                            break;
                        }
                        case AFFIRMATIVE: {
                            startSpeechToText("Hãy nói tên", SPEECH_PERSON_NAME_CODE, SPEECH_LANGUAGE_VIE);
                            break;
                        }
                        case CREATE_LOG_FILE: {
                            startSpeechToText("Hãy nói tên", SPEECH_PERSON_NAME_CODE, SPEECH_LANGUAGE_VIE);
                            break;
                        }
                        case NEGATIVE: {
                            break;
                        }
                        case SHOW_LOGS: {
                            Intent intent = new Intent(MainActivity.this, ShowLogsActivity.class);
                            startActivity(intent);

                            break;
                        }
                        case STREAM_DETECT: {
                            Intent intent = new Intent(this, CloudiaryTest.class);
                            startActivity(intent);
                            break;
                        }
                        default:
                            Toast.makeText(this, "Không hỗ trợ: " + text, Toast.LENGTH_SHORT).show();
                            break;
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
                                if (response != null) {
                                    Toast.makeText(MainActivity.this, "Log created at " + response.body().response.createdate, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Create log failed", Toast.LENGTH_SHORT).show();
                                }
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
            case REQUEST_PERMISSION_SETTING: {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {

                }
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

    private void AskPermissions() {
        if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[3])) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Quyền truy cập");
                builder.setMessage(" Ứng dụng cần quyền truy cập Camera và Thư mục ");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Quyền truy cập");
                builder.setMessage("Ứng dụng cần quyền truy cập Camera và Thư mục");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[2])) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Quyền truy cập");
                builder.setMessage("Ứng dụng cần quyền truy cập Camera và Thư mục");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        }
    }
}








