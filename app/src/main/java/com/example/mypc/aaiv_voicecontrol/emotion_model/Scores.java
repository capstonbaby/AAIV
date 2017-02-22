package com.example.mypc.aaiv_voicecontrol.emotion_model;

/**
 * Created by MyPC on 02/21/2017.
 */

public class Scores {
    public float anger;
    public float contempt;
    public float disgust;
    public float fear;
    public float happiness;
    public float neutral;
    public float sadness;
    public float surprise;

    public void getBigeest() {
        getBiggestFloat(
                anger,
                contempt,
                disgust,
                fear,
                happiness,
                neutral,
                sadness,
                surprise
        );
    }

    public float getBiggestFloat(Float... values) {
        float max = Float.NEGATIVE_INFINITY;
        for (float f : values) {
            if (f > max) max = f;
        }
//        return Math.max(anger, Math.max(contempt, Math.max(disgust, Math.max(fear, Math.max(happiness, Math.max(neutral, Math.max(sadness, surprise)))))));
        return max;
    }
}
