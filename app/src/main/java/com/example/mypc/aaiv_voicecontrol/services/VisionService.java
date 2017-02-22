package com.example.mypc.aaiv_voicecontrol.services;

import com.example.mypc.aaiv_voicecontrol.Constants;
import com.example.mypc.aaiv_voicecontrol.Utils.MyDeserializer;
import com.example.mypc.aaiv_voicecontrol.vision_model.Caption;
import com.example.mypc.aaiv_voicecontrol.vision_model.Description;
import com.example.mypc.aaiv_voicecontrol.vision_model.VisionApi;
import com.example.mypc.aaiv_voicecontrol.vision_model.VisionResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by MyPC on 02/11/2017.
 */

public class VisionService {

    public Call<VisionResponse> DetectVision(String url){
        Retrofit retrofit = getRetrofitVisionDetect();
        VisionApi visionApi = retrofit.create(VisionApi.class);

        return visionApi.DetectView(url);
    }

    public Retrofit getRetrofitVisionDetect() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Caption.class, new MyDeserializer<Description>())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.getVisionAPIString())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

}
