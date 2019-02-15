package com.im.core.serializer;

import java.util.Arrays;
import java.util.List;

public class Serializers {

    public static final Serializer DEFAULT_SERIALIZER = FstSerializer.INSTANCE;

    private static final List<Serializer> SERIALIZERS = Arrays.asList(DEFAULT_SERIALIZER, JsonSerializer.INSTANCE);

    public static Serializer of(byte serializationType) {
        for (Serializer serializer : SERIALIZERS) {
            if (serializationType == serializer.serializationType())
                return serializer;
        }
        return DEFAULT_SERIALIZER;
    }
}
