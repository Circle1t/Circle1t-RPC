package top.circle1t.rpc.serialize;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
public interface Serializer {
    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
