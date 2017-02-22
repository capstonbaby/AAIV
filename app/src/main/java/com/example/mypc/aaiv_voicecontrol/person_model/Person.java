package com.example.mypc.aaiv_voicecontrol.person_model;

import java.util.List;

/**
 * Created by MyPC on 01/12/2017.
 */

public class Person {
    public String personId;
    public List<String> persistedFaceIds;
    public String name;
    public String userData;

    public Person(String personId, List<String> persistedFaceIds, String name, String userData) {
        this.personId = personId;
        this.persistedFaceIds = persistedFaceIds;
        this.name = name;
        this.userData = userData;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public List<String> getPersistedFaceIds() {
        return persistedFaceIds;
    }

    public void setPersistedFaceIds(List<String> persistedFaceIds) {
        this.persistedFaceIds = persistedFaceIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }
}
