package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.mypc.aaiv_voicecontrol.Adapters.GridViewAdapter;
import com.example.mypc.aaiv_voicecontrol.data_model.Data;
import com.example.mypc.aaiv_voicecontrol.data_model.FaceModel;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.PersonModel;
import com.example.mypc.aaiv_voicecontrol.data_model.ResponseModel;
import com.example.mypc.aaiv_voicecontrol.person_model.AddPersonFaceResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.AddPersonResponse;
import com.example.mypc.aaiv_voicecontrol.services.DataService;
import com.example.mypc.aaiv_voicecontrol.services.MainServices;
import com.example.mypc.aaiv_voicecontrol.services.PersonServices;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.rest.ClientException;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;
import me.nereo.multi_image_selector.MultiImageSelector;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mypc.aaiv_voicecontrol.Constants.ADD_NEW_PERSON_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.UPDATE_PERSON_MODE;

public class AddPersonActivity extends AppCompatActivity {
    private SessionManager session;

    private PersonModel updatePerson;
    private TextInputLayout txtPersonName;
    private TextInputLayout txtPersonDes;
    private Button bt_cretePerson;
    private Button bt_train;
    private FloatingActionButton fab_add_image;
    private ProgressBar progressBar;
    private TextView tv_error;

    public static Context context;

    public static Context getContext() {
        return context;
    }

    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_IMAGE_SINGLE = 2;

    private static int IMAGE_AMOUNT = 3;

    private GridView gridView;
    private GridViewAdapter gridAdapter;

    private ArrayList<String> mImagePaths = new ArrayList<>();

    private LogResponse logFile;

    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "debwqzo2g",
            "api_key", "852288139213848",
            "api_secret", "qsuCuMnpTZ11_WxuIuQ5kPZmdr4"));
    private int gridItemPosition;

    @BindView(R.id.ivAdd)
    ImageView ivAdd;
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
        setContentView(R.layout.activity_add_person);
        ButterKnife.bind(this);
        context = getApplicationContext();

        txtPersonName = (TextInputLayout) findViewById(R.id.input_layout_name);
        txtPersonDes = (TextInputLayout) findViewById(R.id.input_layout_des);
        bt_cretePerson = (Button) findViewById(R.id.bt_create);
//        fab_add_image = (FloatingActionButton) findViewById(R.id.fab_add_image);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        bt_train = (Button) findViewById(R.id.bt_train);
        tv_error = (TextView) findViewById(R.id.tv_error);

        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mImagePaths);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MultiImageSelector.create(AddPersonActivity.this)
                        .showCamera(true)
                        .single()
                        .start(AddPersonActivity.this, REQUEST_IMAGE_SINGLE);
                gridItemPosition = position;
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                CropImage.activity(Uri.fromFile(new File(mImagePaths.get(position))))
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddPersonActivity.this);
                gridItemPosition = position;
                return true;
            }
        });
        gridView.setAdapter(gridAdapter);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String mode = bundle.getString("mode");
            LogResponse log = (LogResponse) bundle.getParcelable("logFile");
            final PersonModel person = bundle.getParcelable("person");
            switch (mode) {
                case ADD_NEW_PERSON_MODE: {
                    setTitle("Người mới");
                    if (log != null) {
                        logFile = log;
                        txtPersonName.getEditText().setText(logFile.name);
                        mImagePaths.add(logFile.imgUrl);
                        gridAdapter.notifyDataSetChanged();
                        IMAGE_AMOUNT = 2;
                    }
                    bt_cretePerson.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PersonServices services = new PersonServices();
                            final String personName = txtPersonName.getEditText().getText().toString();
                            final String personDes = txtPersonDes.getEditText().getText().toString();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.VISIBLE);
                                }
                            });

                            //create person in MS
                            services.CreatePerson(
                                    personName,
                                    personDes,
                                    Constants.getFreshPersonGroupId()
                            ).enqueue(new Callback<AddPersonResponse>() {
                                @Override
                                public void onResponse(Call<AddPersonResponse> call, Response<AddPersonResponse> response) {
                                    if (response.isSuccessful()) {
                                        final String personId = response.body().getPersonId();
                                        DataService service = new DataService();

                                        //create person in DB
                                        service.CreatePerson(Constants.getFreshPersonGroupId(), personId, personName, personDes)
                                                .enqueue(new Callback<ResponseModel>() {
                                                    @Override
                                                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                                        if (response.isSuccessful()) {
                                                            if (response.body().success) {
                                                                if (mImagePaths.size() > 0) {
                                                                    for (String imgUrl :
                                                                            mImagePaths) {
                                                                        File imageFile = new File(imgUrl);
                                                                        if (imageFile.exists()) {
                                                                            File compressedImage = Compressor.getDefault(AddPersonActivity.this).compressToFile(imageFile);
                                                                            new Uploader(compressedImage, Constants.getFreshPersonGroupId(), personId).execute();
                                                                        } else {
                                                                            new AddPersonFace(Constants.getFreshPersonGroupId(), imgUrl, personId).execute(UUID.fromString(personId));
                                                                        }
                                                                        if (imgUrl.equals(mImagePaths.get(mImagePaths.size() - 1))) {
                                                                            if (logFile != null) {
                                                                                DataService dataService = new DataService();
                                                                                dataService.DeactiveLog(logFile.id).enqueue(new Callback<MessageResponse>() {
                                                                                    @Override
                                                                                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                                                                                        if (response.isSuccessful()) {
                                                                                            Log.d("deactivelog", response.body().message);
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                                                                                        runOnUiThread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                                                tv_error.setText("Đã có lỗi xảy ra, vui lòng thử lại sau");
                                                                                                tv_error.setVisibility(View.VISIBLE);
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                                new TrainPersonGroup().execute(Constants.getFreshPersonGroupId());
                                                                            }
                                                                        }
                                                                    }
                                                                } else {

                                                                    progressBar.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            progressBar.setVisibility(View.INVISIBLE);
                                                                        }
                                                                    });
                                                                }
                                                            } else {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                    }
                                                                });
                                                            }
                                                        } else {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    tv_error.setText("Đã có lỗi xảy ra, vui lòng thử lại sau");
                                                                    tv_error.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                tv_error.setText("Đã có lỗi xảy ra, vui lòng thử lại sau");
                                                                tv_error.setVisibility(View.VISIBLE);
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onFailure(Call<AddPersonResponse> call, Throwable t) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            tv_error.setText("Đã có lỗi xảy ra, vui lòng thử lại sau");
                                            tv_error.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            });
                        }
                    });
                    break;
                }
                case UPDATE_PERSON_MODE: {
                    setTitle("Cập nhật");
                    final String personId = person.personid;
                    txtPersonName.getEditText().setText(person.name);
                    txtPersonDes.getEditText().setText(person.userData);
                    updatePerson = person;
                    bt_cretePerson.setText("UPDATE");
                    gridAdapter.notifyDataSetChanged();
                    IMAGE_AMOUNT = 2;

                    if (log != null) {
                        logFile = log;
                        mImagePaths.add(logFile.imgUrl);
                        for (FaceModel face :
                                updatePerson.faces) {
                            mImagePaths.add(face.imageUrl);
                        }
                        gridAdapter.notifyDataSetChanged();
                        bt_cretePerson.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DataService services = new DataService();
                                progressBar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.VISIBLE);
                                    }
                                });
                                services.UpdatePerson(personId, person.personGroupId, txtPersonName.getEditText().getText().toString(), txtPersonDes.getEditText().getText().toString())
                                        .enqueue(new Callback<ResponseModel>() {
                                            @Override
                                            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                                if (response.isSuccessful()) {
                                                    if (response.body().success) {
                                                        if (mImagePaths.size() > 0) {
                                                            for (String imgUrl :
                                                                    mImagePaths) {
                                                                File imageFile = new File(imgUrl);
                                                                if (imageFile.exists()) {
                                                                    File compressedImage = Compressor.getDefault(AddPersonActivity.this).compressToFile(imageFile);
                                                                    new Uploader(compressedImage, person.personGroupId, personId).execute();
                                                                } else {
                                                                    new AddPersonFace(person.personGroupId, imgUrl, personId).execute(UUID.fromString(personId));
                                                                }
                                                                if (imgUrl.equals(mImagePaths.get(mImagePaths.size() - 1))) {
                                                                    if (logFile != null) {
                                                                        DataService dataService = new DataService();
                                                                        dataService.DeactiveLog(logFile.id).enqueue(new Callback<MessageResponse>() {
                                                                            @Override
                                                                            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                                                                                if (response.isSuccessful()) {
                                                                                    Log.d("deactivelog", response.body().message);
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<MessageResponse> call, Throwable t) {

                                                                            }

                                                                        });
                                                                        new TrainPersonGroup().execute(person.personGroupId);
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            progressBar.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        progressBar.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    progressBar.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseModel> call, Throwable t) {
                                                progressBar.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                            }
                                        });

                            }
                        });
                    } else {
                        for (FaceModel face :
                                updatePerson.faces) {
                            mImagePaths.add(face.imageUrl);
                        }
                        gridAdapter.notifyDataSetChanged();
                        bt_cretePerson.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DataService services = new DataService();
                                progressBar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.VISIBLE);
                                    }
                                });
                                services.UpdatePerson(personId, person.personGroupId, txtPersonName.getEditText().getText().toString(), txtPersonDes.getEditText().getText().toString())
                                        .enqueue(new Callback<ResponseModel>() {
                                            @Override
                                            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                                if (response.isSuccessful()) {
                                                    if (response.body().success) {
                                                        if (mImagePaths.size() > 0) {
                                                            for (String imgUrl :
                                                                    mImagePaths) {
                                                                File imageFile = new File(imgUrl);
                                                                if (imageFile.exists()) {
                                                                    File compressedImage = Compressor.getDefault(AddPersonActivity.this).compressToFile(imageFile);
                                                                    new Uploader(compressedImage, person.personGroupId, personId).execute();
                                                                } else {
                                                                    new AddPersonFace(person.personGroupId, imgUrl, personId).execute(UUID.fromString(personId));
                                                                }
                                                                if (imgUrl.equals(mImagePaths.get(mImagePaths.size() - 1))) {
                                                                    new TrainPersonGroup().execute(person.personGroupId);
                                                                }
                                                            }
                                                        } else {
                                                            progressBar.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        progressBar.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    progressBar.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseModel> call, Throwable t) {
                                                progressBar.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                            }
                                        });

                            }
                        });
                    }
                    break;
                }
            }
            bt_train.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(person != null){
                        new TrainPersonGroup().execute(person.personGroupId);
                    }else {
                        new TrainPersonGroup().execute(Constants.getFreshPersonGroupId());
                    }
                }
            });
        }

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiImageSelector.create(AddPersonActivity.this)
                        .showCamera(true)
                        .count(IMAGE_AMOUNT)
                        .multi()
                        .start(AddPersonActivity.this, REQUEST_IMAGE);
            }
        });

        initDrawer();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_IMAGE: {
                mImagePaths.addAll(data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT));
                gridAdapter.notifyDataSetChanged();
                break;
            }
            case REQUEST_IMAGE_SINGLE: {
                mImagePaths.set(gridItemPosition,
                        data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT).get(0));
                gridAdapter.notifyDataSetChanged();
                break;
            }
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    mImagePaths.set(gridItemPosition, resultUri.getPath());
                    gridAdapter.notifyDataSetChanged();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
            }
            default:
                break;
        }

    }

    public class AddPersonFace extends AsyncTask<UUID, Void, AddPersistedFaceResult> {
        private String personGroupId;
        private String imgUrl;
        private String personId;

        public AddPersonFace(String personGroupId, String imgUrl, String personId) {
            this.personGroupId = personGroupId;
            this.imgUrl = imgUrl;
            this.personId = personId;
        }

        @Override
        protected AddPersistedFaceResult doInBackground(UUID... params) {
            FaceServiceClient client = Constants.getmFaceServiceClient();
            try {
                return client.addPersonFace(personGroupId, params[0], imgUrl, null, null);
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(AddPersistedFaceResult addPersistedFaceResult) {
            if (addPersistedFaceResult != null) {
                DataService services = new DataService();
                services.AddPersonFace(addPersistedFaceResult.persistedFaceId.toString(), personId, imgUrl).enqueue(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {

                    }
                });
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddPersonActivity.this);

                // Setting Dialog Title
                //alertDialog.setTitle("Alert Dialog");

                // Setting Dialog Message
                alertDialog.setMessage("Một hoặc nhiều hình ảnh của bạn có thể không chứa khuôn mặt. Bạn nên kiểm tra lại.");

                // Setting OK Button
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();
            }
        }
    }

    public class TrainPersonGroup extends AsyncTask<String, Void, Boolean> {

        private boolean isSuccess = true;

        @Override
        protected Boolean doInBackground(String... params) {
//            progressBar.post(new Runnable() {
//                @Override
//                public void run() {
//                    progressBar.setVisibility(View.VISIBLE);
//                }
//            });

            Log.d("Identify", "Traine group: " + params[0]);
            FaceServiceClient client = Constants.getmFaceServiceClient();

            try {
                client.trainPersonGroup(params[0]);
            } catch (ClientException e) {
                e.printStackTrace();
                isSuccess = false;
            } catch (IOException e) {
                e.printStackTrace();
                isSuccess = false;
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (isSuccess) {
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }

    public class Uploader extends AsyncTask<Void, Void, Map> {

        private File compressedFile;
        private String personId;
        private String personGroupId;

        public Uploader(File compressedFile, String personGroupId, String personId) {
            this.compressedFile = compressedFile;
            this.personGroupId = personGroupId;
            this.personId = personId;
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
                new AddPersonFace(personGroupId, url, personId).execute(UUID.fromString(personId));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (logFile != null) {
            Intent i = new Intent(AddPersonActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else {
            super.onBackPressed();
        }
    }
}
