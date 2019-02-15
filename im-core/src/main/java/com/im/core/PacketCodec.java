package com.im.core;

import com.im.core.serializer.Serializer;
import com.im.core.serializer.Serializers;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketCodec {

    private final static int MAGIC_NUMBER = 0x4D696E67;
    private static Map<Class<? extends Packet>, Byte> idForPacketType = new ConcurrentHashMap<>(16);
    private static Map<Byte, Class<? extends Packet>> packetTypeForId = new ConcurrentHashMap<>(16);

    private static byte mapperForPacketType(Class<? extends Packet> clazz) {
        Id id = clazz.getDeclaredAnnotation(Id.class);
        if (id == null)
            throw new IllegalArgumentException("the class '" + clazz + "' must be annotated with '@Id'");
        if (packetTypeForId.containsKey(id.value()))
            throw new IllegalArgumentException("duplicate id");
        packetTypeForId.put(id.value(), clazz);
        return id.value();
    }

    public static byte getId(Class<? extends Packet> clazz) {
        return idForPacketType.computeIfAbsent(clazz, PacketCodec::mapperForPacketType);
    }

    public static Class<? extends Packet> getPacketType(byte id) {
        return packetTypeForId.get(id);
    }

    public static void encode(ByteBuf byteBuf, Packet packet, Serializer serializer) {
        byte[] bytes = serializer.serialize(packet);
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(serializer.serializationType());
        byteBuf.writeByte(getId(packet.getClass()));
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    public static void encode(ByteBuf byteBuf, Packet packet) {
        encode(byteBuf, packet, Serializers.DEFAULT_SERIALIZER);
    }

    public static Packet decode(ByteBuf byteBuf) {
        byteBuf.skipBytes(4);
        byte serializationType = byteBuf.readByte();
        byte id = byteBuf.readByte();
        int len = byteBuf.readInt();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes);
        return Serializers.of(serializationType).deserialize(bytes, getPacketType(id));
    }

}
