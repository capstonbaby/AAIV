package com.example.mypc.aaiv_voicecontrol.Translation;

/**
 * Created by 2TbP on 2/17/2017.
 */
public class TranslationApiUtils {
    private TranslationApiUtils() {};
    public static final String BASE_URL = "https://translation.googleapis.com/";

    public static TranslationAPIService getAPIService() {

        return RetrofitClient1.getClient("https://translation.googleapis.com/").create(TranslationAPIService.class);
    }

}
