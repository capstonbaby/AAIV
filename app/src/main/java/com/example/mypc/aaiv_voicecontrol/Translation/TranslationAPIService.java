package com.example.mypc.aaiv_voicecontrol.Translation;

/**
 * Created by 2TbP on 2/17/2017.
 */
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Win on 2/13/2017.
 */

public interface TranslationAPIService {

    @GET("language/translate/v2")
    Call<Get> saveGet(@Query("key") String key, @Query("source") String source, @Query("target") String target, @Query("q") String q);
}
