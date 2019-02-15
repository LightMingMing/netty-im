package com.im.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PacketCodecTest {
    @Test
    public void getIdTest() {
        assertEquals(1, PacketCodec.getId(FooPacket.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withoutIdTest() {
        PacketCodec.getId(WithoutIdPacket.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateIdTest() {
        PacketCodec.getId(FooPacket.class);
        PacketCodec.getId(BarPacket.class);
    }

    @Test
    public void codecTest() {
        FooPacket fooPacket = new FooPacket();
        fooPacket.setFoo("foo");
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        PacketCodec.encode(byteBuf, fooPacket);
        FooPacket fooPacket2 = (FooPacket) PacketCodec.decode(byteBuf);
        assertEquals("foo", fooPacket2.foo);

    }

    private static class WithoutIdPacket extends BasePacket {
        private static final long serialVersionUID = -6792141332129112942L;
    }

    @Id(1)
    @Setter
    @Getter
    private static class FooPacket extends BasePacket {
        private static final long serialVersionUID = 5379959821846389309L;
        private String foo;
    }

    @Id(1)
    private static class BarPacket extends BasePacket {
        private static final long serialVersionUID = 2488473028830370903L;
    }
}