package com.example.mypc.aaiv_voicecontrol.Utils;

import android.util.Log;

import com.example.mypc.aaiv_voicecontrol.Constants;

/**
 * Created by MyPC on 01/21/2017.
 */

public class aLog {
    public static void w (String TAG, String msg){
        if(Constants.LOGGING) {
            if (TAG != null && msg != null)
                Log.w(TAG, msg);
        }
    }
}
