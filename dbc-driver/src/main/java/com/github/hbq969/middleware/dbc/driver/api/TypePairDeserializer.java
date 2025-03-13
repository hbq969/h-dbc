package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.middleware.dbc.driver.api.model.TypePair;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

public class TypePairDeserializer implements JsonDeserializer<TypePair> {
    @Override
    public TypePair deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        TypePair pair = new TypePair();
        pair.setKey(jsonObject.get("key").getAsString());
        pair.setType(jsonObject.get("type").getAsString());

        // 根据 type 字段动态解析 value
        switch (jsonObject.get("type").getAsString()) {
            case "java.lang.Boolean":
                pair.setValue(jsonObject.get("value").getAsBoolean());
                break;
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Short":
            case "java.lang.Byte":
                pair.setValue(jsonObject.get("value").getAsLong());
                break;
            case "java.lang.Float":
            case "java.lang.Double":
                pair.setValue(jsonObject.get("value").getAsDouble());
                break;
            case "java.lang.Character":
                pair.setValue(jsonObject.get("value").getAsCharacter());
                break;
            default:
                pair.setValue(jsonObject.get("value").getAsString());
        }
        return pair;
    }
}
