package com.example.mypc.aaiv_voicecontrol.services;

import com.example.mypc.aaiv_voicecontrol.Constants;
import com.example.mypc.aaiv_voicecontrol.emotion_model.EmotionApi;
import com.example.mypc.aaiv_voicecontrol.emotion_model.EmotionResponse;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by MyPC on 02/21/2017.
 */

public class EmotionService {

    public Call<List<EmotionResponse>> DetectEmotion(String imgUrl){
        Retrofit retrofit = getRetrofitEmotionDetect();
        EmotionApi emotionApi = retrofit.create(EmotionApi.class);

        return emotionApi.DetectEmotion(imgUrl);
    }


    public Retrofit getRetrofitEmotionDetect() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.getEmotionAPIString())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}
