package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
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

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.mypc.aaiv_voicecontrol.Adapters.GridViewAdapter;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.services.DataService;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.rest.ClientException;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import static com.example.mypc.aaiv_voicecontrol.Constants.PersonGroupId;
import static com.example.mypc.aaiv_voicecontrol.Constants.UPDATE_PERSON_MODE;

public class AddPersonActivity extends AppCompatActivity {

    private String mImageLink;
    private TextInputLayout txtPersonName;
    private TextInputLayout txtPersonDes;
    private Button bt_cretePerson;
    private Button bt_train;
    private FloatingActionButton fab_add_image;
    private ProgressBar progressBar;

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
            switch (mode) {
                case ADD_NEW_PERSON_MODE: {
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
                            new CreatePerson(
                                    PersonGroupId,
                                    txtPersonName.getEditText().getText().toString(),
                                    txtPersonDes.getEditText().getText().toString()
                            ).execute();
                            //CreatePerson(txtPersonName.getEditText().getText().toString(), txtPersonDes.getEditText().getText().toString(), "friend");
                        }
                    });
                    break;
                }
                case UPDATE_PERSON_MODE: {
                    if (log != null) {
                        logFile = log;
                        final String personId = bundle.getString("personId");
                        txtPersonName.getEditText().setText(bundle.getString("personName"));
                        txtPersonDes.getEditText().setText(bundle.getString("personDes"));
                        bt_cretePerson.setText("UPDATE");

                        mImagePaths.add(logFile.imgUrl);
                        gridAdapter.notifyDataSetChanged();
                        IMAGE_AMOUNT = 2;

                        bt_cretePerson.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new UpdatePerson(
                                        PersonGroupId,
                                        personId,
                                        txtPersonName.getEditText().getText().toString(),
                                        txtPersonDes.getEditText().getText().toString()
                                ).execute();
                                //CreatePerson(txtPersonName.getEditText().getText().toString(), txtPersonDes.getEditText().getText().toString(), "friend");
                            }
                        });
                    }
                    break;
                }
            }
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
//        fab_add_image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MultiImageSelector.create(AddPersonActivity.this)
//                        .showCamera(true)
//                        .count(IMAGE_AMOUNT)
//                        .multi()
//                        .start(AddPersonActivity.this, REQUEST_IMAGE);
//            }
//        });

        bt_train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TrainPersonGroup().execute(PersonGroupId);
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
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.open, R.string.close);
        drawer.addDrawerListener(actionBarDrawerToggle);
    }

    private void selectDrawerItem(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.setting:

                break;
            case R.id.logs:
                break;
            case R.id.quota:
                break;
            case R.id.sign_out:
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
                if (mImagePaths.size() >= 3) {
                    new AlertDialog.Builder(AddPersonActivity.this)
                            .setMessage("Không thể thêm nhiều quá 3 hình")
                            .setTitle("Lưu ý !")
                            .setPositiveButton("Ok", null)
                            .show();
                } else {
                    mImagePaths.addAll(data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT));
                    gridAdapter.notifyDataSetChanged();
                }
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

    public class CreatePerson extends AsyncTask<Void, Void, CreatePersonResult> {

        private String personGroupId;
        private String personName;
        private String personDes;

        public CreatePerson(String personGroupId, String personName, String personDes) {
            this.personGroupId = personGroupId;
            this.personName = personName;
            this.personDes = personDes;
        }

        @Override
        protected CreatePersonResult doInBackground(Void... params) {
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

            FaceServiceClient client = Constants.getmFaceServiceClient();

            try {
                return client.createPerson(personGroupId, personName, personDes);
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(CreatePersonResult createPersonResult) {
            if (createPersonResult != null) {
                if (mImagePaths.size() > 0) {
                    for (String imgUrl :
                            mImagePaths) {
                        File imageFile = new File(imgUrl);
                        if (imageFile.exists()) {
                            File compressedImage = Compressor.getDefault(AddPersonActivity.this).compressToFile(imageFile);
                            new Uploader(compressedImage, createPersonResult.personId.toString()).execute();
                        } else {
                            new AddPersonFace(personGroupId, imgUrl).execute(createPersonResult.personId);
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
                                progressBar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    public class UpdatePerson extends AsyncTask<Void, Void, Boolean> {
        private String personGroupId;
        private String personId;
        private String personName;
        private String personDes;

        private boolean isSuccess = true;

        public UpdatePerson(String personGroupId, String personId, String personName, String personDes) {
            this.personGroupId = personGroupId;
            this.personId = personId;
            this.personName = personName;
            this.personDes = personDes;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
            FaceServiceClient client = Constants.getmFaceServiceClient();

            try {
                client.updatePerson(personGroupId, UUID.fromString(personId), personName, personDes);
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
                if (mImagePaths.size() > 0) {
                    for (String imgUrl :
                            mImagePaths) {
                        File imageFile = new File(imgUrl);
                        if (imageFile.exists()) {
                            File compressedImage = Compressor.getDefault(AddPersonActivity.this).compressToFile(imageFile);
                            new Uploader(compressedImage, personId).execute();
                        } else {
                            new AddPersonFace(personGroupId, imgUrl).execute(UUID.fromString(personId));
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
                                progressBar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        }
                    }
                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }
    }

    public class AddPersonFace extends AsyncTask<UUID, Void, AddPersistedFaceResult> {
        private String personGroupId;
        private String imgUrl;

        public AddPersonFace(String personGroupId, String imgUrl) {
            this.personGroupId = personGroupId;
            this.imgUrl = imgUrl;
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

            }
        }
    }

    public class TrainPersonGroup extends AsyncTask<String, Void, Boolean> {

        private boolean isSuccess = true;

        @Override
        protected Boolean doInBackground(String... params) {
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
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

        public Uploader(File compressedFile, String personId) {
            this.compressedFile = compressedFile;
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
                new AddPersonFace(PersonGroupId, url).execute(UUID.fromString(personId));
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(AddPersonActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
