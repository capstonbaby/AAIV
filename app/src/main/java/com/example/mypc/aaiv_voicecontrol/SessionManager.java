package com.example.mypc.aaiv_voicecontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by MyPC on 03/06/2017.
 */

public class SessionManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private int PRIVATE_MODE = 0;
    private static final String PREP_NAME = "AAIVPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_PERSON_GROUP_ID = "personGroupId";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USERNAME = "username";

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREP_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void CreateLoginSession(String personGroupId, String userId, String username){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_PERSON_GROUP_ID, personGroupId);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_PERSON_GROUP_ID, sharedPreferences.getString(KEY_PERSON_GROUP_ID, null));
        user.put(KEY_USER_ID, sharedPreferences.getString(KEY_USER_ID, null));
        user.put(KEY_USERNAME, sharedPreferences.getString(KEY_USERNAME, null));

        return user;
    }

    public void checkLoggedIn(){
        if(!this.isLoggedIn()){
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        }
    }

    public void logoutUser(){
        editor.clear();
        editor.commit();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }
}
