package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.mypc.aaiv_voicecontrol.Adapters.PersonsAdapter;
import com.example.mypc.aaiv_voicecontrol.Utils.DividerItemDecoration;
import com.example.mypc.aaiv_voicecontrol.Utils.RecyclerTouchListener;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.mypc.aaiv_voicecontrol.Constants.ADD_NEW_PERSON_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.PersonGroupId;
import static com.example.mypc.aaiv_voicecontrol.Constants.UPDATE_PERSON_MODE;

public class UpdatePersonActivity extends AppCompatActivity {

    private Context context;

    public Context getContext() {
        return context;
    }

    private List<Person> mPersonList = new ArrayList<>();
    private PersonsAdapter mPersonsAdapter;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private Button mBtAddNewPerson;
    private LogResponse log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_person);

        context = getApplicationContext();

        mProgressBar = (ProgressBar) findViewById(R.id.pb_show_persons);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_persons);
        mBtAddNewPerson = (Button) findViewById(R.id.bt_new_person);

        mProgressBar.setVisibility(View.VISIBLE);
        new ListPerson().execute(PersonGroupId);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            log = (LogResponse) bundle.get("logfile");
        }

         mBtAddNewPerson.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(UpdatePersonActivity.this, AddPersonActivity.class);
                 intent.putExtra("logFile", log);
                 intent.putExtra("mode", ADD_NEW_PERSON_MODE);
                 startActivity(intent);
             }
         });
    }

    public class ListPerson extends AsyncTask<String, Void, Person[]> {

        @Override
        protected Person[] doInBackground(String... params) {
            FaceServiceClient client = Constants.getmFaceServiceClient();
            try {
                return client.getPersons(params[0]);
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Person[] persons) {
            if (persons != null) {

                for (Person person :
                        persons) {
                    mPersonList.add(person);
                }
                mPersonsAdapter = new PersonsAdapter(mPersonList);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.addItemDecoration(new DividerItemDecoration(UpdatePersonActivity.this, LinearLayoutManager.VERTICAL));
                mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if(log != null){
                            Person person = mPersonList.get(position);
                            Intent intent = new Intent(UpdatePersonActivity.this, AddPersonActivity.class);
                            intent.putExtra("logFile", log);
                            intent.putExtra("personId", person.personId.toString());
                            intent.putExtra("personName", person.name);
                            intent.putExtra("personDes", person.userData);
                            intent.putExtra("mode", UPDATE_PERSON_MODE);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));

                mRecyclerView.setAdapter(mPersonsAdapter);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}
