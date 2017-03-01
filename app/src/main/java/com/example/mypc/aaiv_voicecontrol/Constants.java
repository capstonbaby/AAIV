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

    //Command
    private static String SHOOTING_COMMAND;
    private static String DETECT_PERSON_COMMAND;
    private static String DETECT_OBJECT_COMMAND;
    private static String DETECT_VIEW_COMMAND;
    private static String REPEAT_RESULT_COMMAND;
    private static String NEW_PERSON_COMMAND;
    private static String ACCEPT_COMMAND;
    private static String DENY_COMMAND;
    private static String SHOW_LOG_COMMAND;

//    private static String SHOOTING_COMMAND = "Chụp";
//    private static String DETECT_PERSON_COMMAND = "Ai đó";
//    private static String DETECT_OBJECT_COMMAND = "Cái gì đó";
//    private static String DETECT_VIEW_COMMAND = "Cảnh gì đó";
//    private static String REPEAT_RESULT_COMMAND = "lặp lại";
//    private static String NEW_PERSON_COMMAND = "thêm mới";
//    private static String ACCEPT_COMMAND = "Đồng ý";
//    private static String DENY_COMMAND = "Hủy";
//    private static String SHOW_LOG_COMMAND = "lịch sử";


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


    public static void setShootingCommand(String shootingCommand) {
        SHOOTING_COMMAND = shootingCommand;
    }

    public static void setDetectPersonCommand(String detectPersonCommand) {
        DETECT_PERSON_COMMAND = detectPersonCommand;
    }

    public static void setDetectObjectCommand(String detectObjectCommand) {
        DETECT_OBJECT_COMMAND = detectObjectCommand;
    }

    public static void setDetectViewCommand(String detectViewCommand) {
        DETECT_VIEW_COMMAND = detectViewCommand;
    }

    public static void setRepeatResultCommand(String repeatResultCommand) {
        REPEAT_RESULT_COMMAND = repeatResultCommand;
    }

    public static void setNewPersonCommand(String newPersonCommand) {
        NEW_PERSON_COMMAND = newPersonCommand;
    }

    public static void setAcceptCommand(String acceptCommand) {
        ACCEPT_COMMAND = acceptCommand;
    }

    public static void setDenyCommand(String denyCommand) {
        DENY_COMMAND = denyCommand;
    }

    public static void setShowLogCommand(String showLogCommand) {
        SHOW_LOG_COMMAND = showLogCommand;
    }


    public static String getShootingCommand() {
        return SHOOTING_COMMAND;
    }

    public static String getDetectPersonCommand() {
        return DETECT_PERSON_COMMAND;
    }

    public static String getDetectObjectCommand() {
        return DETECT_OBJECT_COMMAND;
    }

    public static String getDetectViewCommand() {
        return DETECT_VIEW_COMMAND;
    }

    public static String getRepeatResultCommand() {
        return REPEAT_RESULT_COMMAND;
    }

    public static String getNewPersonCommand() {
        return NEW_PERSON_COMMAND;
    }

    public static String getAcceptCommand() {
        return ACCEPT_COMMAND;
    }

    public static String getDenyCommand() {
        return DENY_COMMAND;
    }

    public static String getShowLogCommand() {
        return SHOW_LOG_COMMAND;
    }
}
