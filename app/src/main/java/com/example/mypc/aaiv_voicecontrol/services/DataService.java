package com.example.mypc.aaiv_voicecontrol.services;

import com.example.mypc.aaiv_voicecontrol.Constants;
import com.example.mypc.aaiv_voicecontrol.data_model.DataApi;
import com.example.mypc.aaiv_voicecontrol.data_model.GetPeopleModel;
import com.example.mypc.aaiv_voicecontrol.data_model.LoginResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.ResponseModel;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by MyPC on 02/22/2017.
 */

public class DataService {

    public Call<MessageResponse> DeactiveLog(int logId){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.DeactiveLog(logId);
    }

    public Call<MessageResponse> CreateLog(String ImageUrl, String name){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.CreateLog(ImageUrl, name, Constants.getUserId());
    }

    public Call<List<LogResponse>> GetAllLogFromUser(String userId){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.GetAllLogFromUser(userId);
    }

    public Call<LoginResponse> Login(String email, String password){
        Retrofit retrofit = getRetrofitAccountApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.Login(email, password);
    }

    public Call<ResponseModel> Register(String email, String password, String confirmPassword){
        Retrofit retrofit = getRetrofitAccountApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.Register(email, password, confirmPassword);
    }

    public Call<ResponseModel> CreatePerson(String personGroupID, String personId, String personName, String personDes){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.CreatPerson(personGroupID, personId, personName, personDes);
    }

    public Call<ResponseModel> UpdatePerson(String personId, String personGroupId, String personName, String personDes){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.UpdatePerson(personGroupId, personId, personName, personDes);
    }

    public Call<ResponseModel> AddPersonFace(String persistedFaceId, String personId, String imgUrl){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.AddPersonFace(persistedFaceId, personId, imgUrl);
    }

    public Call<GetPeopleModel> GetPeopleInGroup(String personGroupId){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.GetPeopleInGroup(personGroupId);
    }

    public Call<GetPeopleModel> GetPeopleOfuser(String userId){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.GetPeopleOfUser(userId);
    }

    public Retrofit getRetrofitDataApi() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.getDataAPIString())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

    public Retrofit getRetrofitAccountApi() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.getAccountAPIString())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}
