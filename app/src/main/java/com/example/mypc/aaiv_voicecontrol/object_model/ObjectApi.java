package com.example.mypc.aaiv_voicecontrol.object_model;

import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.vision_model.VisionResponse;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by 2TbP on 2/28/2017.
 */

public interface ObjectApi {

    @FormUrlEncoded
    @POST("detect")
    Call<ResponseBody> DetectObject(@Field("url") String url);

    @FormUrlEncoded
    @POST("CreateLogObject")
    Call<MessageResponse> CreateLog(@Field("ImageUrl") String ImageUrl, @Field("userID") String userId);
}