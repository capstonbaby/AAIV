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

    @FormUrlEncoded
    @POST("CreateLog")
    Call<MessageResponse> CreateLog(@Field("ImageUrl") String ImageUrl, @Field("Name") String name, @Field("userId") String userId);

    @FormUrlEncoded
    @POST("GetAllLogFromUser")
    Call<List<LogResponse>> GetAllLogFromUser(@Field("userid") String userid);

    @FormUrlEncoded
    @POST("DeactiveLog")
    Call<MessageResponse> DeactiveLog(@Field("logId") int logId);

    @FormUrlEncoded
    @POST("Login")
    Call<LoginResponse> Login(@Field("Email") String email, @Field("Password") String password);

    @FormUrlEncoded
    @POST("Register")
    Call<ResponseModel> Register(@Field("Email") String email, @Field("Password") String password, @Field("ConfirmPassword") String confirmPassword);

    @FormUrlEncoded
    @POST("CreatePerson")
    Call<ResponseModel> CreatPerson(@Field("PersonGroupID") String personGroupId, @Field("PersonId") String personId, @Field("Name") String personName, @Field("Description") String personDes);

    @FormUrlEncoded
    @POST("UpdatePerson")
    Call<ResponseModel> UpdatePerson(@Field("persongroupId") String personGroupId, @Field("PersonId") String PersonId, @Field("Name") String personName, @Field("Description") String personDes);

    @FormUrlEncoded
    @POST("AddPersonFace")
    Call<ResponseModel> AddPersonFace(@Field("PersistedFaceId") String persistedFaceId, @Field("PersonID") String personId, @Field("ImageURL") String imgUrl);

    @FormUrlEncoded
    @POST("getpeopleingroup")
    Call<GetPersonInGroupModel> GetPeopleInGroup(@Field("PersonGroupId") String personGroupId);

}
