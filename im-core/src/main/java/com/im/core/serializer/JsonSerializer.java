package com.im.core.serializer;

import com.alibaba.fastjson.JSON;

public class JsonSerializer implements Serializer {

    public final static JsonSerializer INSTANCE = new JsonSerializer();

    private JsonSerializer() {
    }

    @Override
    public byte serializationType() {
        return SerializationType.JSON_SERIALIZER;
    }

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }

}
