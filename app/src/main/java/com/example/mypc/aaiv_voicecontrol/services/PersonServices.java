package com.example.mypc.aaiv_voicecontrol.services;

import android.content.Context;

import com.example.mypc.aaiv_voicecontrol.AddPersonActivity;
import com.example.mypc.aaiv_voicecontrol.Constants;
import com.example.mypc.aaiv_voicecontrol.Helper.NotificationHelper;
import com.example.mypc.aaiv_voicecontrol.person_model.AddPersonFaceResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.AddPersonResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.FaceDetectResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.FaceIdentifyResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.Person;
import com.example.mypc.aaiv_voicecontrol.person_model.PersonApi;
import com.example.mypc.aaiv_voicecontrol.person_model.PersonGroup;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by MyPC on 01/23/2017.
 */

public class PersonServices {

    public NotificationHelper notificationHelper;
    Context mAddPersonContext = new AddPersonActivity().getContext();

    List<PersonGroup> personGroups = null;

    public Call<List<FaceIdentifyResponse>> IdentifyPerson(String personGroupId, String faceIds){
        Retrofit retrofit = getRetrofitFaceDetect();

        PersonApi personApi = retrofit.create(PersonApi.class);

        return personApi.identifyPerson(personGroupId, faceIds);
    }

    public Call<List<FaceDetectResponse>> DetectFaces(String urlImage){
        Retrofit retrofit = getRetrofitFaceDetect();

        PersonApi personApi = retrofit.create(PersonApi.class);

        return personApi.dectectPerson(urlImage);
    }

    public Call<AddPersonResponse> CreatePerson(String personName, String personDes, String personGroupId){
        Retrofit retrofit = getRetrofitFaceDetect();

        PersonApi personApi = retrofit.create(PersonApi.class);

        return personApi.createPerson(personGroupId, personName, personDes);
    }

    public Call<Person> GetPersonById(String personGroupid, String personId){
        Retrofit retrofit = getRetrofitFaceDetect();

        PersonApi personApi = retrofit.create(PersonApi.class);

        return personApi.getPersonById(personGroupid, personId);
    }

    public Call<AddPersonFaceResponse> AddPersonFace(String personGroupId,String personId, String urlImage, String userData) {
        Retrofit retrofit = getRetrofitFaceDetect();

        PersonApi personApi = retrofit.create(PersonApi.class);

        return personApi.addPersonFace(personGroupId, personId, userData, urlImage);
    }

    public Call<List<PersonGroup>> GetPersonGroups() {

        Retrofit retrofit = getRetrofitFaceDetect();

        PersonApi faceDetectApi = retrofit.create(PersonApi.class);

        return faceDetectApi.getPersonGroup();
    }

    public Call<List<PersonGroup>> GetPersonGroupSync(){
        Retrofit retrofit = getRetrofitFaceDetect();

        PersonApi faceDetectApi = retrofit.create(PersonApi.class);
        Call<List<PersonGroup>> call =  faceDetectApi.getPersonGroup();

        return call;
    }

    public Call<Void> TrainPersonGroup(String personGroupId) {
        Retrofit retrofit = getRetrofitFaceDetect();
        PersonApi faceDetectApi = retrofit.create(PersonApi.class);

        notificationHelper = new NotificationHelper(mAddPersonContext);
        notificationHelper.createTrainingStatusNotification();

        return faceDetectApi.trainPersonGroup(personGroupId);
    }

    public Retrofit getRetrofitFaceDetect() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.getFaceAPIString())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}
