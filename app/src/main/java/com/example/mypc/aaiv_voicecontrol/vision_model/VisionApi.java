package com.example.mypc.aaiv_voicecontrol.vision_model;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by MyPC on 02/11/2017.
 */

public interface VisionApi {

    @FormUrlEncoded
    @POST("detect")
    Call<VisionResponse> DetectView(@Field("url") String url);

}
