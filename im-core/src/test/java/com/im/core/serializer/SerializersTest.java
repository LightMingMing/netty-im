package com.im.core.serializer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SerializersTest {

    @Test
    public void of() {
        assertEquals(FstSerializer.INSTANCE, Serializers.of(FstSerializer.INSTANCE.serializationType()));
        assertEquals(JsonSerializer.INSTANCE, Serializers.of(JsonSerializer.INSTANCE.serializationType()));
    }
}