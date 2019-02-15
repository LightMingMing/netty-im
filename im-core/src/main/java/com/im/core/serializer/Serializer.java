package com.im.core.serializer;

public interface Serializer {

    byte serializationType();

    byte[] serialize(Object object);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}