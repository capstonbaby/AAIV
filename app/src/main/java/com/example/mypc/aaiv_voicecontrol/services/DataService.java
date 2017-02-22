package com.example.mypc.aaiv_voicecontrol.services;

import com.example.mypc.aaiv_voicecontrol.Constants;
import com.example.mypc.aaiv_voicecontrol.data_model.DataApi;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.data_model.LogResponse;

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

        return dataApi.CreateLog(ImageUrl, name);
    }

    public Call<List<LogResponse>> GetAllLogFromUser(String userId){
        Retrofit retrofit = getRetrofitDataApi();
        DataApi dataApi = retrofit.create(DataApi.class);

        return dataApi.GetAllLogFromUser(userId);
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
}
