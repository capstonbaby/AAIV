package com.example.mypc.aaiv_voicecontrol;

import com.cloudinary.Api;

/**
 * Created by MyPC on 01/21/2017.
 */

public class Constants {
    //API Host link
    public static String API_HOST;
    public static final String VISION_API = "api/vision/";
    public static final String FACE_API = "api/face/";
    public static final String EMOTION_API = "api/emotion/";
    public static final String DATA_API = "data/";

    public static void setApiHost(String hostApi) {
        API_HOST = "http://" + hostApi + "/CapstoneProject.WebAPI/";
    }

    public static String getApiHost() {
        return API_HOST;
    }

    public static String getVisionAPIString() {
        return API_HOST + VISION_API;
    }

    public static String getFaceAPIString() {
        return API_HOST + FACE_API;
    }

    public static String getEmotionAPIString() {
        return API_HOST + EMOTION_API;
    }

    public static String getDataAPIString() {
        return API_HOST + DATA_API;
    }


    /*
                  Logging flag
                 */
    public static final boolean LOGGING = false;
}
