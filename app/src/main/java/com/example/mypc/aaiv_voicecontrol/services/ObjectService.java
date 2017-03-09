package com.example.mypc.aaiv_voicecontrol.services;


import com.example.mypc.aaiv_voicecontrol.Constants;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.object_model.ObjectApi;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by 2TbP on 2/23/2017.
 */
public class ObjectService {
    public Call<ResponseBody> DetectObject(String url){
        Retrofit retrofit = getRetrofitObjectDetect();
        ObjectApi objectApi = retrofit.create(ObjectApi.class);

        String test = String.valueOf(objectApi.DetectObject(url));

        return objectApi.DetectObject(url);
    }

    public Retrofit getRetrofitObjectDetect() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.getObjectAPIString())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

    public Retrofit getRetrofitObjectDetectCreateLog() {
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

    public Call<MessageResponse> CreateLog(String ImageUrl){
        Retrofit retrofit = getRetrofitObjectDetectCreateLog();
        ObjectApi objectApi = retrofit.create(ObjectApi.class);

        return objectApi.CreateLog(ImageUrl, Constants.getUserId());
    }
}
