package com.example.mypc.aaiv_voicecontrol;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;

/**
 * Created by MyPC on 01/21/2017.
 */

public class Constants {

    //FACE DETECTION API KEY
    private static FaceServiceClient mFaceServiceClient = new FaceServiceRestClient("3fafcdb48bdc4ef6b20d61524bfac93c");

    public static FaceServiceClient getmFaceServiceClient() {
        return mFaceServiceClient;
    }

    //API Host link
    public static String API_HOST;
    public static final String VISION_API = "api/vision/";
    public static final String FACE_API = "api/face/";
    public static final String EMOTION_API = "api/emotion/";
    public static final String DATA_API = "data/";
    public static final String Object_API = "api/object/";

    public static void setApiHost(String hostApi) {
        API_HOST = "http://" + hostApi + "/CapstoneProject.WebAPI/";
    }

    public static String getApiHost() {
        return API_HOST;
    }

    public static String getVisionAPIString() {
        return API_HOST + VISION_API;
    }

    public  static  String getObjectAPIString(){
        return  API_HOST + Object_API;
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

    public static final boolean LOGGING = false;

    public static final int SPEECH_RECOGNITION_CODE = 1;
    public static final String FACE_RECOGNITION_MODE = "face";
    public static final String OBJECT_RECOGNITION_MODE = "object";
    public static final String VIEW_RECOGNITION_MODE = "view";
    public static final String REPEAT = "repeat";
    public static final String ADD_PERSON_VIEW = "new person";
    public static final String AFFIRMATIVE = "yes";
    public static final String NEGATIVE = "no";
    public static final String CREATE_LOG_FILE = "save person";
    public static final String SHOW_LOGS = "history";
    public static final String STREAM_DETECT = "stream";

    public static final int SPEECH_PERSON_NAME_CODE = 2;
    public static final String SPEECH_LANGUAGE_ENG = "en-US";
    public static final String SPEECH_LANGUAGE_VIE = "vi-VN";
    public static final String SPEECH_ONDONE_CONFIRMATION = "1";
    public static final String SPEECH_ONDONE_NOREQUEST = "0";


    public static final int PERSON_DETECTED_SUCCESSFULLY = 1;
    public static final int PERSON_DETECTED_FAILED = 2;
    public static final int NO_PERSON_DETECTED = 3;

    public static final String ADD_NEW_PERSON_MODE = "new";
    public static final String UPDATE_PERSON_MODE = "update";

    public static final String PersonGroupId = "friend";
}
