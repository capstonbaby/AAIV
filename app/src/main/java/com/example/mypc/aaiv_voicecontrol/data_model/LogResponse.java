package com.example.mypc.aaiv_voicecontrol.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by MyPC on 02/22/2017.
 */

public class LogResponse implements Parcelable{
    public int id;
    public String imgUrl;
    public String createdate;
    public String name;

    protected LogResponse(Parcel in) {
        id = in.readInt();
        imgUrl = in.readString();
        createdate = in.readString();
        name = in.readString();
    }

    public static final Creator<LogResponse> CREATOR = new Creator<LogResponse>() {
        @Override
        public LogResponse createFromParcel(Parcel in) {
            return new LogResponse(in);
        }

        @Override
        public LogResponse[] newArray(int size) {
            return new LogResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(imgUrl);
        dest.writeString(createdate);
        dest.writeString(name);
    }
}
