package com.example.mypc.aaiv_voicecontrol.services;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.TextView;

import com.example.mypc.aaiv_voicecontrol.Speech.Post;
import com.example.mypc.aaiv_voicecontrol.Speech.SpeechAPIService;
import com.example.mypc.aaiv_voicecontrol.Speech.SpeechApiUtils;
import com.example.mypc.aaiv_voicecontrol.Translation.Get;
import com.example.mypc.aaiv_voicecontrol.Translation.TranslationAPIService;
import com.example.mypc.aaiv_voicecontrol.Translation.TranslationApiUtils;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by 2TbP on 2/17/2017.
 */
public class SpeechServices {
    private TextView mResponseTv;
    private SpeechAPIService mAPIService;
    private TranslationAPIService transApiService;

    public SpeechServices(){
        transApiService = TranslationApiUtils.getAPIService();

        //Text To Speech
        mAPIService = SpeechApiUtils.getAPIService();
    }

    public void sendPost(String body) {
        mAPIService.savePost(body).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {

                if(response.isSuccessful()) {
                    // Play sound

                    String url = response.body().async; // your URL here
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.prepare(); // might take long! (for buffering, etc)
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.d("load", "Speech failed");
            }
        });
    }

    public void sendGet(String src){

        String key = "AIzaSyDOi-0A_dUQ0CDIQU_ku2SiYpdZxwP6BtY";
        String source = "en";
        String target = "vi";
        transApiService.saveGet(key,source,target,src).enqueue(new Callback<Get>() {

            @Override
            public void onResponse(Call<Get> call, Response<Get> response) {
                Log.d("load", response.body().toString());
                if (response.isSuccessful()){
                    String tranlatedText = response.body().getData().getTranslations().get(0).getTranslatedText();
                    sendPost(tranlatedText);
                }
            }

            @Override
            public void onFailure(Call<Get> call, Throwable t) {
                Log.d("load", "failed");
                t.printStackTrace();
            }
        });

    }
}
