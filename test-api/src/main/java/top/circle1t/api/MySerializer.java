package top.circle1t.api;

import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Circle1t
 * @since 2025/11/09
 */
@Slf4j
public class MySerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            log.info("=========使用MySerializer做序列化=========");

            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            log.info("=========使用MySerializer做反序列化=========");
            return clazz.cast(ois.readObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
