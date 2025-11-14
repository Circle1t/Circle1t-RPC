package top.circle1t.rpc.serialize.impl;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.serialize.Serializer;

/**
 * @author Circle1t
 * @since 2025/11/09
 */
@Slf4j
public class ProtostuffSerializer implements Serializer {
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    @Override
    public byte[] serialize(Object obj) {
        Class<?> aclass = obj.getClass();
        Schema schema = RuntimeSchema.getSchema(aclass);

        try{
            log.info("=========使用ProtoStuff做序列化=========");
            return ProtobufIOUtil.toByteArray(obj, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);

        T t = schema.newMessage();
        log.info("=========使用ProtoStuff做反序列化=========");
        ProtobufIOUtil.mergeFrom(bytes, t, schema);
        return t;
    }
}
