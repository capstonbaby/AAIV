package com.example.mypc.aaiv_voicecontrol.data_model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MyPC on 03/13/2017.
 */

public class FaceModel implements Parcelable {
    public String persistedFaceId;
    public String imageUrl;

    protected FaceModel(Parcel in) {
        persistedFaceId = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<FaceModel> CREATOR = new Creator<FaceModel>() {
        @Override
        public FaceModel createFromParcel(Parcel in) {
            return new FaceModel(in);
        }

        @Override
        public FaceModel[] newArray(int size) {
            return new FaceModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(persistedFaceId);
        dest.writeString(imageUrl);
    }
}
