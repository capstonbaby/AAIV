package com.example.mypc.aaiv_voicecontrol;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mypc.aaiv_voicecontrol.Adapters.Commands;
import com.example.mypc.aaiv_voicecontrol.Adapters.CommandsAdapter;
import com.example.mypc.aaiv_voicecontrol.Utils.DividerItemDecoration;
import com.example.mypc.aaiv_voicecontrol.Utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreferenceActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SessionManager session;
    private TextToSpeech mTextToSpeech;
    private String requestLanguage = "vi-VN";

    private final int PERSON_RECOGNITION_CODE = 0;
    private final int OBJECT_RECOGNITION_CODE = 1;
    private final int VIEW_RECOGNITION_CODE = 2;
    private final int STREAM_RECOGNITION_CODE = 3;
    private final int NEW_PERSON_CODE = 4;
    private final int SAVE_LOG_RECOGNITION_CODE = 5;
    private final int HISTORY_RECOGNITION_CODE = 6;
    private final int CONFIRM_RECOGNITION_CODE = 7;
    private final int DENY_RECOGNITION_CODE = 8;
    private final int REPEAT_CODE = 9;
    private final String NO_REQUEST = "NO REQUEST";

    private int speech_time;
    private String speech_language;
    private int speech_code;
    private String speech_promt;
    private String temp_command;

    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.nvNavigation)
    NavigationView nvNavigation;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private List<Commands> mCommandList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private CommandsAdapter mCommandsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        ButterKnife.bind(this);
        setTitle("Thiết Lập Khẩu Lệnh");
        initDrawer();
        SetUpText2Speech();

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_commands);


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);

                                        GetCommands();
                                    }
                                }
        );
    }

    @Override
    public void onRefresh() {
        GetCommands();
    }

    public void GetCommands() {
        Commands commands = new Commands();
        mCommandList.clear();

        commands.command_title = "Nhận diện người";
        commands.command_value = Constants.getDetectPersonCommand();
        mCommandList.add(commands);

        commands = new Commands();
        commands.command_title = "Nhận diện đồ vật";
        commands.command_value = Constants.getDetectObjectCommand();
        mCommandList.add(commands);

        commands = new Commands();
        commands.command_title = "Miêu tả khung cảnh";
        commands.command_value = Constants.getDetectViewCommand();
        mCommandList.add(commands);

        commands = new Commands();
        commands.command_title = "Nhận dạng streaming";
        commands.command_value = Constants.getStreamDetectCommand();
        mCommandList.add(commands);

        commands = new Commands();
        commands.command_title = "Thêm người mới";
        commands.command_value = Constants.getNewPersonCommand();
        mCommandList.add(commands);

        commands = new Commands();
        commands.command_title = "Lưu lại kết quả";
        commands.command_value = Constants.getCreateLogFile();
        mCommandList.add(commands);

        commands = new Commands();
        commands.command_title = "Xem lịch sử";
        commands.command_value = Constants.getShowLogCommand();
        mCommandList.add(commands);

        commands = new Commands();
        commands.command_title = "Đồng ý";
        commands.command_value = Constants.getAcceptCommand();
        mCommandList.add(commands);

        commands = new Commands();
        commands.command_title = "Từ chối";
        commands.command_value = Constants.getDenyCommand();
        mCommandList.add(commands);

        mCommandsAdapter = new CommandsAdapter(mCommandList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(PreferenceActivity.this, LinearLayoutManager.VERTICAL));
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int position) {
                Commands command = mCommandList.get(position);
                //showCommandDialog(command, command.command_title);

                startSpeechToText("Nói khẩu lệnh " + command.command_title, requestLanguage, position, 1);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        mRecyclerView.setAdapter(mCommandsAdapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void initDrawer() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        nvNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
        loadNavHeader();
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.open, R.string.close);
        drawer.addDrawerListener(actionBarDrawerToggle);
    }

    public void loadNavHeader() {
        View header = nvNavigation.getHeaderView(0);

        ImageView mHeaderImage = (ImageView) header.findViewById(R.id.img_header_bg);
        TextView mUserEmail = (TextView) header.findViewById(R.id.user_email);
        ImageView mUserProfile = (ImageView) header.findViewById(R.id.img_profile);

        //load header background
        Glide.with(this)
                .load(R.drawable.nav_menu_header_bg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mHeaderImage);

        //load User Image
        Glide.with(this)
                .load(R.drawable.user_avatar)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mUserProfile);

        mUserEmail.setText(Constants.getUsername());
    }

    private void selectDrawerItem(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.setting:
                intent = new Intent(this, StartUpActivity.class);
                startActivity(intent);
                break;
            case R.id.logs:
                intent = new Intent(this, ShowLogsActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_out:
                session.logoutUser();
                break;
            case R.id.people_in_group:
                intent = new Intent(this, UpdatePersonActivity.class);
                startActivity(intent);
                break;
            default:
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_ip: {
                showInputDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(PreferenceActivity.this);
        View promptView = layoutInflater.inflate(R.layout.ip_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
        alertDialogBuilder.setView(promptView);

        final TextInputLayout mTvIpAddress = (TextInputLayout) promptView.findViewById(R.id.input_layout_ip);
        if (Constants.getApiHost() != null) {
            mTvIpAddress.getEditText().setText(Constants.getApiHost());
        }
        // setup a dialog window
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Constants.setApiHost(mTvIpAddress.getEditText().getText().toString());
                        Toast.makeText(PreferenceActivity.this, Constants.getApiHost(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    protected void showCommandDialog(final Commands command, final String mode) {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(PreferenceActivity.this);
        View promptView = layoutInflater.inflate(R.layout.command_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
        alertDialogBuilder.setView(promptView);

        final TextView mCommandTitle = (TextView) promptView.findViewById(R.id.title);
        final TextInputLayout mCommandValue = (TextInputLayout) promptView.findViewById(R.id.input_layout_command);
        mCommandTitle.setText(command.command_title);
        mCommandValue.getEditText().setText(command.command_value);

        // setup a dialog window
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch (mode) {
                            case "Nhận diện người": {
                                Constants.setDetectPersonCommand(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            case "Nhận diện đồ vật": {
                                Constants.setDetectObjectCommand(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            case "Miêu tả khung cảnh": {
                                Constants.setDetectViewCommand(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            case "Nhận dạng streaming": {
                                Constants.setStreamDetectCommand(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            case "Thêm người mới": {
                                Constants.setNewPersonCommand(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            case "Lưu lại kết quả": {
                                Constants.setCreateLogFile(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            case "Xem lịch sử": {
                                Constants.setShowLogCommand(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            case "Đồng ý": {
                                Constants.setAcceptCommand(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            case "Từ chối": {
                                Constants.setDenyCommand(mCommandValue.getEditText().getText().toString());
                                mCommandsAdapter.notifyDataSetChanged();
                                GetCommands();
                                break;
                            }
                            default:
                                break;
                        }

                        Toast.makeText(PreferenceActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            ArrayList<String> results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String text = results.get(0).toLowerCase();

            switch (requestCode) {
                case PERSON_RECOGNITION_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, PERSON_RECOGNITION_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setDetectPersonCommand(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                case OBJECT_RECOGNITION_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, OBJECT_RECOGNITION_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setDetectObjectCommand(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                case VIEW_RECOGNITION_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, VIEW_RECOGNITION_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setDetectViewCommand(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                case NEW_PERSON_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, NEW_PERSON_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setNewPersonCommand(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                case SAVE_LOG_RECOGNITION_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, SAVE_LOG_RECOGNITION_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setCreateLogFile(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                case HISTORY_RECOGNITION_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, HISTORY_RECOGNITION_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setShowLogCommand(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                case STREAM_RECOGNITION_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, STREAM_RECOGNITION_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setStreamDetectCommand(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                case CONFIRM_RECOGNITION_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, CONFIRM_RECOGNITION_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setAcceptCommand(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                case DENY_RECOGNITION_CODE: {
                    if (speech_time == 1) {
                        temp_command = text;
                        startSpeechToText("Xin nhắc lại.", requestLanguage, DENY_RECOGNITION_CODE, 2);
                        break;
                    } else if (speech_time == 2) {
                        if (text.equals(temp_command)) {
                            Constants.setDenyCommand(text);
                            mCommandsAdapter.notifyDataSetChanged();
                            GetCommands();
                            break;
                        } else {
                            Speak("Khẩu lệnh không trùng khớp", NO_REQUEST);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
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
                            if(utteranceId.equals("speech2text")){
                                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, speech_language);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, speech_promt);
                                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, "9000");

                                try {
                                    startActivityForResult(intent, speech_code);
                                } catch (ActivityNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(PreferenceActivity.this, "Speech recognition is not support for this device.", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });

                    mTextToSpeech.setLanguage(new Locale("vi", "VN"));

                    Log.d("setupt2s", "Setup finished");
                } else if (status == TextToSpeech.ERROR) {
                    Toast.makeText(PreferenceActivity.this, "Setup Speech Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void Speak(String text, String requestCode) {
        if (mTextToSpeech != null) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, requestCode);
        }
    }

    public void startSpeechToText(String promt, String language, int SPEECH_CODE, int time) {
        Speak(promt, "speech2text");

        this.speech_time = time;
        this.speech_language  =language;
        this.speech_code = SPEECH_CODE;
        this.speech_promt = promt;

    }
}
