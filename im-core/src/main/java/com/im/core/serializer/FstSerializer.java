package com.im.core.serializer;

import com.im.core.util.SerializeException;
import org.nustaq.serialization.FSTConfiguration;

public class FstSerializer implements Serializer {

    public static final FstSerializer INSTANCE = new FstSerializer();

    private static final ThreadLocal<FSTConfiguration> conf = ThreadLocal.withInitial(FSTConfiguration::createDefaultConfiguration);

    private FstSerializer() {
    }

    @Override
    public byte serializationType() {
        return SerializationType.FST_SERIALIZER;
    }

    @Override
    public byte[] serialize(Object object) {
        return conf.get().asByteArray(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Object obj = conf.get().asObject(bytes);
        if (clazz.isAssignableFrom(obj.getClass()))
            return (T) obj;
        throw new SerializeException();
    }
}
