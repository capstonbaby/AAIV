package com.example.mypc.aaiv_voicecontrol.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by MyPC on 01/21/2017.
 */

public class NetworkUtil {
    public static final String TAG = NetworkUtil.class.getSimpleName();

    public static boolean isConnected(Context mContext) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        } catch (Exception ex) {
            aLog.w(TAG, ex.getMessage());
        }
        return false;
    }

    public static boolean connectionReachable() {
        Socket socket = null;
        boolean reachable = false;
        try {
            socket = new Socket("google.com", 80);
            reachable = socket.isConnected();
        } catch (UnknownHostException e) {
            aLog.w(TAG, "Error connecting to server");
            reachable = false;
        } catch (IOException e) {
            aLog.w(TAG, "Error connecting to server");
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    aLog.w(TAG, "Error closing connecting socket test");
                }
            }
        }
        aLog.w(TAG, "Data connectivity change detected, ping test=" + String.valueOf(reachable));
        return reachable;
    }


}
