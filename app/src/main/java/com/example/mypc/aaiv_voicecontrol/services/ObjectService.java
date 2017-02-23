package com.example.mypc.aaiv_voicecontrol.services;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 2TbP on 2/23/2017.
 */
public class ObjectService extends AsyncTask<String, Void, String> {
    private String urlClarifai = "";
    private String urlCloudsight = "";
    @Override
    protected String doInBackground(String[] params) {
        //String url = "http://res.cloudinary.com/debwqzo2g/image/upload/v1487612998/sample.jpg";
        String returnValue = "";
        String urlImage = params[0];
        urlClarifai = "http://detectobject.herokuapp.com/clarifai/v1.0/image?url=" + urlImage;
        JSONObject jsonClarifai = null;
        JSONObject outputPart = null;
        JSONObject firstConcept = null;
        String valueStr = "";
        try {
            jsonClarifai = getJSONObjectFromURL(urlClarifai);
            outputPart = (JSONObject) jsonClarifai.getJSONArray("outputs").get(0);
            firstConcept = (JSONObject) outputPart.getJSONObject("data").getJSONArray("concepts").get(0);
            valueStr = firstConcept.getString("value");
            if(Double.parseDouble(valueStr) > 0.6){
                returnValue =  firstConcept.getString("id");
            }else {
                returnValue = UseCloudsight(urlImage);
            }
        } catch (Exception e){
            Log.d("clarifai", "detect object clarifai fail");
            returnValue = UseCloudsight(urlImage);
        }
        return returnValue;
    }

    public String UseCloudsight(String url){
        urlCloudsight = "http://detectobject.herokuapp.com/cloudsight/v1.0/image?url=" + url;
        String nameObject = "";
        try {
            JSONObject jsonObject = getJSONObjectFromURL(urlCloudsight);
            nameObject = jsonObject.getString("name");
        } catch (Exception e) {
            e.printStackTrace();
            return "Detect object fail";
        }
        return nameObject;
    }


    @Override
    protected void onPostExecute(String message) {
        //process message
    }

    public JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

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
