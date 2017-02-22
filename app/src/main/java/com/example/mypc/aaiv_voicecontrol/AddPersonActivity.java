package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.mypc.aaiv_voicecontrol.person_model.AddPersonResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.PersonGroup;
import com.example.mypc.aaiv_voicecontrol.services.PersonServices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import id.zelory.compressor.Compressor;
import me.nereo.multi_image_selector.MultiImageSelector;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPersonActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String mImageLink;
    private TextInputLayout txtPersonName;
    private TextInputLayout txtPersonDes;
    private Button bt_cretePerson;
    private FloatingActionButton fab_add_image;
    private ProgressBar progressBar;

    private String personGroupId;

    public static Context context;

    public static Context getContext() {
        return context;
    }

    private static final int REQUEST_IMAGE = 1;

    private GridView gridView;
    private GridViewAdapter gridAdapter;

    private ArrayList<String> mImagePaths;
    private ArrayList<String> mCompressedImagePaths = new ArrayList<>();

    private Handler mHandler;
    private HandlerThread mBackgroundThread;

    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "debwqzo2g",
            "api_key", "852288139213848",
            "api_secret", "qsuCuMnpTZ11_WxuIuQ5kPZmdr4"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        context = getApplicationContext();

        //Constants.setApiHost("192.168.1.18");
        Toast.makeText(this, Constants.getApiHost(), Toast.LENGTH_LONG).show();

        txtPersonName = (TextInputLayout) findViewById(R.id.input_layout_name);
        txtPersonDes = (TextInputLayout) findViewById(R.id.input_layout_des);
        bt_cretePerson = (Button) findViewById(R.id.bt_create);
        fab_add_image = (FloatingActionButton) findViewById(R.id.fab_add_image);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        fab_add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiImageSelector.create(AddPersonActivity.this)
                        .showCamera(true) // show camera or not. true by default
                        .count(3) // max select image size, 9 by default. used width #.multi()
                        .multi() // multi mode, default mode;
                        .start(AddPersonActivity.this, REQUEST_IMAGE);
            }
        });

        bt_cretePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreatePerson(txtPersonName.getEditText().getText().toString(), txtPersonDes.getEditText().getText().toString(), "friend");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        StartThread();
    }

    @Override
    protected void onPause() {
        StopThread();
        super.onPause();
    }

    private void StartThread() {
        mBackgroundThread = new HandlerThread("AddPersonBackground");
        mBackgroundThread.start();
        mHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void StopThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        StartThread();

        mImagePaths = null;

        if (requestCode != REQUEST_IMAGE) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        mImagePaths = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
        //Safety check to prevent null pointer exception
        if (mImagePaths == null || mImagePaths.isEmpty() || mImagePaths.size() > 3) return;

        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mImagePaths);
        gridView.setAdapter(gridAdapter);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (String path :
                        mImagePaths) {
                    try {
                        File imageFile = new File(path);
                        File compressedImage = Compressor.getDefault(AddPersonActivity.this).compressToFile(imageFile);
                        Map uploadResult = null;
                        uploadResult = cloudinary.uploader().upload(compressedImage, ObjectUtils.emptyMap());
                        String url = (String) uploadResult.get("url");

                        mCompressedImagePaths.add(url);
                        Log.d("ImagePath", url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        PersonGroup item = (PersonGroup) parent.getItemAtPosition(position);
        personGroupId = item.getPersonGroupId();
        Log.d("GroupId", item.getPersonGroupId() + " | " + item.name);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void CreatePerson(String name, final String des, final String personGroupId) {

        final PersonServices personServices = new PersonServices();

        progressBar.setVisibility(View.VISIBLE);

        personServices.CreatePerson(name, des, personGroupId).enqueue(new Callback<AddPersonResponse>() {
            @Override
            public void onResponse(Call<AddPersonResponse> call, Response<AddPersonResponse> response) {
                if (response.isSuccessful()) {
                    final String personId = response.body().getPersonId();

                    Toast.makeText(AddPersonActivity.this, personId, Toast.LENGTH_LONG).show();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (String imgurl :
                                    mCompressedImagePaths) {
                                try {
                                    personServices.AddPersonFace(personGroupId, personId, imgurl, des).execute();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            TrainGroup(personGroupId);
                        }
                    });

                } else {
                    Toast.makeText(AddPersonActivity.this, "Create Person Failed", Toast.LENGTH_LONG).show();
                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<AddPersonResponse> call, Throwable t) {
                Toast.makeText(AddPersonActivity.this, "Create Person Process Failed", Toast.LENGTH_LONG).show();
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
                t.printStackTrace();
            }
        });
    }

    public void TrainGroup(final String personGroupId) {
        final PersonServices personServices = new PersonServices();
        personServices.TrainPersonGroup(personGroupId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                new AlertDialog.Builder(AddPersonActivity.this)
                        .setTitle("Train Group Failed")
                        .setMessage("Try Again ?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                TrainGroup(personGroupId);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }
}
