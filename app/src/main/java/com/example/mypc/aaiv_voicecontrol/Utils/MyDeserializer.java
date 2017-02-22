package com.example.mypc.aaiv_voicecontrol.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;


/**
 * Created by MyPC on 02/11/2017.
 */

public class MyDeserializer<T> implements JsonDeserializer<T> {

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonElement caption = json.getAsJsonObject().get("description");

        return new Gson().fromJson(caption, typeOfT);
    }
}
