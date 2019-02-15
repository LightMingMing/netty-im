package com.im.core;

public abstract class BasePacket implements Packet {

    private static final long serialVersionUID = -3445759208233749025L;

    @Override
    public final byte getId() {
        return PacketCodec.getId(this.getClass());
    }
}
