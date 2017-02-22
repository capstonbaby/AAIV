package com.example.mypc.aaiv_voicecontrol.person_model;

import android.support.annotation.Nullable;

/**
 * Created by MyPC on 01/15/2017.
 */

public class PersonGroup {
    public String personGroupId;
    public String name;
    public @Nullable String userData;

    public PersonGroup(String personGroupId, String name, String userData) {
        this.personGroupId = personGroupId;
        this.name = name;
        this.userData = userData;
    }

    public PersonGroup(String s, String s1) {
        this.name = s;
        this.userData = s1;
    }

    public String getPersonGroupId() {
        return personGroupId;
    }

    public void setPersonGroupId(String personGroupId) {
        this.personGroupId = personGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getUserData() {
        return userData == null ? "" : userData;
    }

    public void setUserData(@Nullable String userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return name;
    }
}
