package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mypc.aaiv_voicecontrol.Adapters.LogAdapter;
import com.example.mypc.aaiv_voicecontrol.Utils.RecyclerTouchListener;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.services.DataService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowLogsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    private SessionManager session;

    private List<LogResponse> logList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private LogAdapter mLogAdapter;
    private TextView TvNoLog;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        setContentView(R.layout.activity_show_logs);
        ButterKnife.bind(this);
        initDrawer();
        context = getApplicationContext();

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_logs);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_show_logs);
        TvNoLog = (TextView) findViewById(R.id.tv_no_log);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);

                                        GetLogs();
                                    }
                                }
        );

        //mProgressBar.setVisibility(View.VISIBLE);

    }

    public void GetLogs() {
        final DataService dataService = new DataService();
        dataService.GetAllLogFromUser(Constants.getUserId()).enqueue(new Callback<List<LogResponse>>() {
            @Override
            public void onResponse(Call<List<LogResponse>> call, Response<List<LogResponse>> response) {
                if (response.isSuccessful()) {
                    if (response.body().size() > 0) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        logList = response.body();
                        if (logList.size() >= 1) {
                            mLogAdapter = new LogAdapter(logList);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            mRecyclerView.setLayoutManager(layoutManager);
                            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                            //mRecyclerView.addItemDecoration(new DividerItemDecoration(ShowLogsActivity.this, LinearLayoutManager.VERTICAL));
                            mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
                                @Override
                                public void onClick(View view, int position) {
                                    LogResponse log = logList.get(position);
                                    Intent intent = new Intent(ShowLogsActivity.this, UpdatePersonActivity.class);
                                    intent.putExtra("logfile", log);
                                    startActivity(intent);
                                }

                                @Override
                                public void onLongClick(View view, int position) {
                                    final LogResponse log = logList.get(position);

                                    new AlertDialog.Builder(ShowLogsActivity.this)
                                            .setTitle("Xóa")
                                            .setMessage("Bạn có chắc chắn muốn xóa ?")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    dataService.DeactiveLog(log.id).enqueue(new Callback<MessageResponse>() {
                                                        @Override
                                                        public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                                                            finish();
                                                            startActivity(getIntent());
                                                        }

                                                        @Override
                                                        public void onFailure(Call<MessageResponse> call, Throwable t) {
                                                            new AlertDialog.Builder(ShowLogsActivity.this)
                                                                    .setTitle("Thất bại")
                                                                    .setMessage("Xóa thất bại")
                                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                                    .setNegativeButton(android.R.string.cancel, null).show();
                                                        }
                                                    });
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, null).show();
                                }
                            }));

                            mRecyclerView.setAdapter(mLogAdapter);
                            // stopping swipe refresh
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    } else {
                        // stopping swipe refresh
                        swipeRefreshLayout.setRefreshing(false);
                        TvNoLog.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onFailure(Call<List<LogResponse>> call, Throwable t) {
                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);
            }
        });
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

    public void loadNavHeader(){
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
            case R.id.quota:
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
    public void onRefresh() {
        GetLogs();
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
        LayoutInflater layoutInflater = LayoutInflater.from(ShowLogsActivity.this);
        View promptView = layoutInflater.inflate(R.layout.ip_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ShowLogsActivity.this);
        alertDialogBuilder.setView(promptView);

        final TextInputLayout mTvIpAddress = (TextInputLayout) promptView.findViewById(R.id.input_layout_ip);
        if(Constants.getApiHost() != null){
            mTvIpAddress.getEditText().setText(Constants.getApiHost());
        }
        // setup a dialog window
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Constants.setApiHost(mTvIpAddress.getEditText().getText().toString());
                        Toast.makeText(ShowLogsActivity.this, Constants.getApiHost(), Toast.LENGTH_SHORT).show();
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
}
