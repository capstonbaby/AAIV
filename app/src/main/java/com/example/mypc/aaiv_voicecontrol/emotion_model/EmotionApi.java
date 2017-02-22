package com.example.mypc.aaiv_voicecontrol.emotion_model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by MyPC on 02/21/2017.
 */

public interface EmotionApi {

    @FormUrlEncoded
    @POST
    Call<List<EmotionResponse>> DetectEmotion(@Field("urlImage") String urlImage);
}
