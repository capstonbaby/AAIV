package com.example.mypc.aaiv_voicecontrol.Speech;

/**
 * Created by 2TbP on 2/17/2017.
 */
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SpeechAPIService {

    @Headers({
            "api_key: b2be869d882042b4907abe0c74fae5bb",
            "voice: male",
            "speed: -3"
    })
    @POST("text2speech")
    Call<Post> savePost(@Body String content);
}
