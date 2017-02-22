package com.example.mypc.aaiv_voicecontrol.data_model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by MyPC on 02/22/2017.
 */

public interface DataApi {

    /*TODO: ADD USER ID AFTER IMPLIMENT LOGIN FUNCTION*/
    @FormUrlEncoded
    @POST("CreateLog")
    Call<MessageResponse> CreateLog(@Field("ImageUrl") String ImageUrl, @Field("Name") String name);

    @FormUrlEncoded
    @POST("GetAllLogFromUser")
    Call<List<LogResponse>> GetAllLogFromUser(@Field("userid") String userid);

    @FormUrlEncoded
    @POST("DeactiveLog")
    Call<MessageResponse> DeactiveLog(@Field("logId") int logId);
}
