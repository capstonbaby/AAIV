package com.example.mypc.aaiv_voicecontrol.person_model;

/**
 * Created by MyPC on 01/18/2017.
 */

public class TrainingStatus {
    public String status;
    public String createdDateTime;
    public Object lastActionDateTime;
    public Object message;

    public String getStatus() {
        return status;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public Object getLastActionDateTime() {
        return lastActionDateTime;
    }

    public Object getMessage() {
        return message;
    }
}
