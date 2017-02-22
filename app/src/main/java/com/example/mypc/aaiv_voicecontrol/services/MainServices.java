package com.example.mypc.aaiv_voicecontrol.services;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.mypc.aaiv_voicecontrol.CameraActivity_2;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Response;

/**
 * Created by MyPC on 02/06/2017.
 */

public class MainServices {
    private final int PERSON_DETECTED_SUCCESSFULLY = 1;
    private final int PERSON_DETECTED_FAILED = 2;
    private final int NO_PERSON_DETECTED = 3;


    private String identifyResponse = "Identify Fail";
    IdentifyResult identifyResult;
    private String faceIds = "";
    private String urlCloudsight = "";
    private String urlClarifai = "";

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
                    identifyResponse = "There is a " + (gender.get(0).equals("male") ? "man" : "women");
                    identifyResult  = new IdentifyResult(identifyResponse, PERSON_DETECTED_FAILED);
                    Log.d("gender", gender.get(0));
                } else if (gender.size() < 1) {
                    identifyResponse = "No person detected";
                    identifyResult  = new IdentifyResult(identifyResponse, NO_PERSON_DETECTED);
                } else {
                    identifyResponse = "There are " + gender.size() + " people.";
                    identifyResult  = new IdentifyResult(identifyResponse, PERSON_DETECTED_FAILED);
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
                                    identifyResponse = person.name + " " + person.userData;
                                    identifyResult  = new IdentifyResult(identifyResponse, PERSON_DETECTED_SUCCESSFULLY);
                                } else {
                                    Log.e("Person", "Can't get person");

                                    if (gender.size() == 1) {
                                        identifyResponse = "There is a " + (gender.get(0).equals("male") ? "man" : "women");
                                        identifyResult  = new IdentifyResult(identifyResponse, PERSON_DETECTED_FAILED);
                                        Log.d("gender", gender.get(0));
                                    } else if (gender.size() < 1) {
                                        identifyResponse = "No person detected";
                                        identifyResult  = new IdentifyResult(identifyResponse, NO_PERSON_DETECTED);
                                    } else {
                                        identifyResponse = "There are " + gender.size() + " people.";
                                        identifyResult  = new IdentifyResult(identifyResponse, PERSON_DETECTED_FAILED);
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

    public List<PersonGroup> GetPersonGroupSync() {
        PersonServices personServices = new PersonServices();
        List<PersonGroup> personGroups = null;
        try {
            Response<List<PersonGroup>> response = personServices.GetPersonGroupSync().execute();
            if (response.isSuccessful()) {
                personGroups = response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return personGroups;
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
        String nameObject = "";
        try {
            nameObject = new ObjectService().execute(url).get();
        } catch (Exception e){
            Log.d("detectobject", "fail DetectObject in MainService");
        }
        return nameObject;
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        jsonString = sb.toString();

        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }

}
