package com.example.mypc.aaiv_voicecontrol.person_model;

/**
 * Created by MyPC on 02/22/2017.
 */

public class IdentifyResult {
    String identifyResponse;
    int identifyStatus;

    public IdentifyResult(String identifyResponse, int identifyStatus) {
        this.identifyResponse = identifyResponse;
        this.identifyStatus = identifyStatus;
    }

    public String getIdentifyResponse() {
        return identifyResponse;
    }

    public int getIdentifyStatus() {
        return identifyStatus;
    }
}
