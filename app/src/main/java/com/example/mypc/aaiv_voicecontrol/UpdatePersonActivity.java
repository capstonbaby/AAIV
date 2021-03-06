package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mypc.aaiv_voicecontrol.Adapters.PersonsAdapter;
import com.example.mypc.aaiv_voicecontrol.Utils.RecyclerTouchListener;
import com.example.mypc.aaiv_voicecontrol.data_model.GetPeopleModel;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.PersonModel;
import com.example.mypc.aaiv_voicecontrol.data_model.ResponseModel;
import com.example.mypc.aaiv_voicecontrol.person_model.Person;
import com.example.mypc.aaiv_voicecontrol.services.DataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mypc.aaiv_voicecontrol.Constants.ADD_NEW_PERSON_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.UPDATE_PERSON_MODE;

public class UpdatePersonActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static Context context;

    public static Context getContext() {
        return context;
    }

    private SessionManager session;
    private List<PersonModel> mPersonList = new ArrayList<>();
    private PersonsAdapter mPersonsAdapter;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private LogResponse log;
    private TextView tvNoPerson;
    private SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.fabAdd)
    FloatingActionButton fabAdd;
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
        setContentView(R.layout.activity_update_person);
        ButterKnife.bind(this);
        context = getApplicationContext();
        setTitle("Người thân");

        session = new SessionManager(getApplicationContext());
        Log.d("isLogin", session.isLoggedIn() ? "logged in" : "not logged in");

        session.checkLoggedIn();


        HashMap<String, String> user = session.getUserDetails();
        Constants.setPersonGroupId(user.get(session.KEY_PERSON_GROUP_ID));
        Constants.setUserId(user.get(session.KEY_USER_ID));
        Constants.setUsername(user.get(session.KEY_USERNAME));

        mProgressBar = (ProgressBar) findViewById(R.id.pb_show_persons);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_persons);
        tvNoPerson = (TextView) findViewById(R.id.tv_no_person);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            log = (LogResponse) bundle.get("logfile");
        }

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdatePersonActivity.this, AddPersonActivity.class);
                intent.putExtra("logFile", log);
                intent.putExtra("mode", ADD_NEW_PERSON_MODE);
                startActivity(intent);
            }
        });
        initNavigation();

        GetPeople();
        mPersonsAdapter = new PersonsAdapter(mPersonList, UpdatePersonActivity.this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                PersonModel person = mPersonList.get(position);
                Intent intent = new Intent(UpdatePersonActivity.this, AddPersonActivity.class);
                if (log != null) {
                    intent.putExtra("logFile", log);
                }
                intent.putExtra("mode", UPDATE_PERSON_MODE);
                intent.putExtra("person", person);

                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, final int position) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(UpdatePersonActivity.this);

                alertDialog.setTitle("Xóa");

                alertDialog.setMessage("Bạn có chắc chắn muốn xóa người này");

                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.cancel();

                        DataService service = new DataService();
                        PersonModel person = mPersonList.get(position);
                        swipeRefreshLayout.setRefreshing(true);
                        service.DeletePerson(person.personid).enqueue(new Callback<ResponseModel>() {
                            @Override
                            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                if(response.isSuccessful()){
                                    if(response.body().success){
                                        GetPeople();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseModel> call, Throwable t) {
                                swipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(UpdatePersonActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                alertDialog.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
        }));

        mRecyclerView.setAdapter(mPersonsAdapter);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        GetPeople();
                                    }
                                }
        );
    }

    public void GetPeople(){
        tvNoPerson.setVisibility(View.INVISIBLE);

        DataService service = new DataService();
        service.GetPeopleInGroup(Constants.getPersonGroupId()).enqueue(new Callback<GetPeopleModel>() {
            @Override
            public void onResponse(Call<GetPeopleModel> call, Response<GetPeopleModel> response) {
                if (response.isSuccessful()) {
                    final GetPeopleModel getPersonInGroupModel = response.body();
                    if (getPersonInGroupModel.success) {
                        if (getPersonInGroupModel.data.size() > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPersonList.clear();
                                    mPersonList.addAll(getPersonInGroupModel.data);
                                    mPersonsAdapter.notifyDataSetChanged();
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        } else {
                            mPersonList.clear();
                            mPersonsAdapter.notifyDataSetChanged();
                            tvNoPerson.setText("Nhấn '+' để thêm người mới");
                            tvNoPerson.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                        tvNoPerson.setText("Error");
                        tvNoPerson.setVisibility(View.VISIBLE);
                    }
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    tvNoPerson.setText("Error");
                    tvNoPerson.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<GetPeopleModel> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                tvNoPerson.setText("Error");
                tvNoPerson.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initNavigation() {
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
        GetPeople();
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
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
        LayoutInflater layoutInflater = LayoutInflater.from(UpdatePersonActivity.this);
        View promptView = layoutInflater.inflate(R.layout.ip_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UpdatePersonActivity.this);
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
                        Toast.makeText(UpdatePersonActivity.this, Constants.getApiHost(), Toast.LENGTH_SHORT).show();
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
