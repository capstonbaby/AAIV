package com.example.mypc.aaiv_voicecontrol.person_model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by MyPC on 01/23/2017.
 */

public interface PersonApi {

    @FormUrlEncoded
    @POST("getpersonbyid")
    Call<Person> getPersonById(@Field("personGroupId") String personGroupId, @Field("personId") String personId);

    @FormUrlEncoded
    @POST("identify")
    Call<List<FaceIdentifyResponse>> identifyPerson(@Field("personGroupId") String personGroupId, @Field("faceid") String faceid);

    @FormUrlEncoded
    @POST("createfaceid")
    Call<List<FaceDetectResponse>> dectectPerson(@Field("urlImage") String urlImage);

    @GET("getgroups")
    Call<List<PersonGroup>> getPersonGroup();

    @FormUrlEncoded
    @POST("getpeopleingroup")
    Call<List<Person>> getPeopleInGroup(@Field("personGroupId") String personGroupId);

    @FormUrlEncoded
    @POST("trainpersongroup")
    Call<Void> trainPersonGroup(@Field("personGroupId") String personGroupId);

    @FormUrlEncoded
    @POST("createperson")
    Call<AddPersonResponse> createPerson(@Field("personGroupId") String personGroupId, @Field("personName") String personName, @Field("userData") String personDescription);

    @FormUrlEncoded
    @POST("addpersonface")
    Call<AddPersonFaceResponse> addPersonFace(@Field("personGroupId") String personGroupId, @Field("personId") String personId, @Field("userData") @Nullable String userData, @Field("urlImage") String urlImage);
}
