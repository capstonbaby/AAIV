package com.example.mypc.aaiv_voicecontrol.services;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.mypc.aaiv_voicecontrol.CameraActivity_2;
import com.example.mypc.aaiv_voicecontrol.data_model.MessageResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.Candidate;
import com.example.mypc.aaiv_voicecontrol.person_model.FaceDetectResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.FaceIdentifyResponse;
import com.example.mypc.aaiv_voicecontrol.person_model.IdentifyResult;
import com.example.mypc.aaiv_voicecontrol.person_model.Person;
import com.example.mypc.aaiv_voicecontrol.person_model.PersonGroup;
import com.example.mypc.aaiv_voicecontrol.vision_model.VisionResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mypc.aaiv_voicecontrol.Constants.NO_PERSON_DETECTED;
import static com.example.mypc.aaiv_voicecontrol.Constants.PERSON_DETECTED_FAILED;
import static com.example.mypc.aaiv_voicecontrol.Constants.PERSON_DETECTED_SUCCESSFULLY;

/**
 * Created by MyPC on 02/06/2017.
 */

public class MainServices {

    private String identifyResponse = "Identify Fail";
    IdentifyResult identifyResult;
    private String faceIds = "";
    private SpeechServices mSpeechServices = new SpeechServices();

    public IdentifyResult IdentifyPerson(String urlImage) {

        final PersonServices service = new PersonServices();
        try {
            List<FaceDetectResponse> faceDetectResponses = service.DetectFaces(urlImage).execute().body();

            if (faceDetectResponses != null) {
                List<String> listFaceIds = new ArrayList<>();
                List<String> gender = new ArrayList<>();

                for (FaceDetectResponse faceDetected :
                        faceDetectResponses) {
                    Log.d("Detect", faceDetected.faceId);
                    listFaceIds.add(faceDetected.faceId);
                    gender.add(faceDetected.faceAttributes.gender);
                }

                if (gender.size() == 1) {
                    identifyResponse = "Có một người " + (gender.get(0).equals("male") ? "đàn ông" : "phụ nữ");
                    identifyResult = new IdentifyResult(identifyResponse, PERSON_DETECTED_FAILED);
                    Log.d("gender", gender.get(0));
                } else if (gender.size() < 1) {
                    identifyResponse = "Không có ai cả";
                    identifyResult = new IdentifyResult(identifyResponse, NO_PERSON_DETECTED);
                } else {
                    identifyResponse = "Có " + gender.size() + " người.";
                    identifyResult = new IdentifyResult(identifyResponse, PERSON_DETECTED_FAILED);
                }

                faceIds = TextUtils.join(",", listFaceIds);

                List<FaceIdentifyResponse> faceIdentifyResponses = service.IdentifyPerson("friend", faceIds).execute().body();

                if (faceIdentifyResponses != null) {

                    for (FaceIdentifyResponse faceIdentified :
                            faceIdentifyResponses) {

                        if (faceIdentified.candidates.size() > 0 || faceIdentified.candidates != null) {

                            Log.d("Identify", "Candidates for faceId: " + faceIdentified.faceId);
                            for (Candidate candidate :
                                    faceIdentified.candidates) {
                                Log.d("Identify", candidate.personId);
                                Person person = service.GetPersonById("friend", candidate.personId).execute().body();
                                if (person != null) {
                                    identifyResponse = person.name;
                                    identifyResult = new IdentifyResult(identifyResponse, PERSON_DETECTED_SUCCESSFULLY);
                                } else {
                                    Log.e("Person", "Can't get person");

                                    if (gender.size() == 1) {
                                        identifyResponse = "There is a " + (gender.get(0).equals("male") ? "man" : "women");
                                        identifyResult = new IdentifyResult(identifyResponse, PERSON_DETECTED_FAILED);
                                        Log.d("gender", gender.get(0));
                                    } else if (gender.size() < 1) {
                                        identifyResponse = "No person detected";
                                        identifyResult = new IdentifyResult(identifyResponse, NO_PERSON_DETECTED);
                                    } else {
                                        identifyResponse = "There are " + gender.size() + " people.";
                                        identifyResult = new IdentifyResult(identifyResponse, PERSON_DETECTED_FAILED);
                                    }

                                    Log.d("identify", identifyResponse);
                                }
                            }
                        }
                    }
                } else {
                    Log.d("identify", identifyResponse);
                }

            } else {
                Toast.makeText(CameraActivity_2.getContext(), "Detect Faces Failed", Toast.LENGTH_SHORT).show();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return identifyResult;
    }

    public VisionResponse DetectVision(String url) {
        VisionResponse visionResponse = null;


        VisionService visionService = new VisionService();
        try {
            Response<VisionResponse> response = visionService.DetectVision(url).execute();
            if (response.isSuccessful()) {
                visionResponse = response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return visionResponse;
    }

    public String DetectObject(String url) {
        JSONObject outputPart = null;
        JSONObject firstConcept = null;
        String valueStr = "";
        JSONObject jsonClarifai  = null;
        String returnValue = "";

        ObjectService objectService = new ObjectService();
        try {
            Response<ResponseBody> response = objectService.DetectObject(url).execute();

            String jsonStr = response.body().string();
            jsonClarifai = new JSONObject(jsonStr);

            outputPart = (JSONObject) jsonClarifai.getJSONArray("outputs").get(0);
            firstConcept = (JSONObject) outputPart.getJSONObject("data").getJSONArray("concepts").get(0);
            valueStr = firstConcept.getString("value");
            if(Double.parseDouble(valueStr) > 0.7){
                returnValue =  firstConcept.getString("id");
            }else{
                objectService.CreateLog(url).enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                        Log.d("createLog", "onReponse function");
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        Log.d("createLog", "Create Log Fail");
                        t.printStackTrace();
                    }
                });
                returnValue = "Không xác định được vật thể";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("detectobject", "error when detect object MainService");
            returnValue = "Máy chủ bị lỗi";
        }

        return returnValue;
    }



}
