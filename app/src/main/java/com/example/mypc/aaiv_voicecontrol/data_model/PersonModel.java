package com.example.mypc.aaiv_voicecontrol.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by MyPC on 03/13/2017.
 */

public class PersonModel implements Parcelable{
    public String name;
    public String userData;
    public String personid;
    public List<FaceModel> faces = null;

    protected PersonModel(Parcel in) {
        name = in.readString();
        userData = in.readString();
        personid = in.readString();
        faces = in.createTypedArrayList(FaceModel.CREATOR);
    }

    public static final Creator<PersonModel> CREATOR = new Creator<PersonModel>() {
        @Override
        public PersonModel createFromParcel(Parcel in) {
            return new PersonModel(in);
        }

        @Override
        public PersonModel[] newArray(int size) {
            return new PersonModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(userData);
        dest.writeString(personid);
        dest.writeTypedList(faces);
    }
}
