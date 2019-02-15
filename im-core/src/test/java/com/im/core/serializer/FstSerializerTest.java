package com.im.core.serializer;

import com.im.core.BasePacket;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

public class FstSerializerTest {

    @Test
    public void serializeTest() {
        FooPacket foo = new FooPacket();
        foo.setFoo("hello");
        FooPacket foo2 = FstSerializer.INSTANCE.deserialize(FstSerializer.INSTANCE.serialize(foo), FooPacket.class);
        Assert.assertEquals("hello", foo2.foo);
    }

    @Setter
    @Getter
    private static class FooPacket extends BasePacket {
        private static final long serialVersionUID = -6201351465231723043L;
        private String foo;
    }
}