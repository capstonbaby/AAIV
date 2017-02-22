package com.example.mypc.aaiv_voicecontrol.Speech;

/**
 * Created by 2TbP on 2/17/2017.
 */
public class SpeechApiUtils {
    private SpeechApiUtils() {}

    public static final String BASE_URL = "http://api.openfpt.vn/";

    public static SpeechAPIService getAPIService() {

        return RetrofitClient.getClient("http://api.openfpt.vn/").create(SpeechAPIService.class);
    }
}
