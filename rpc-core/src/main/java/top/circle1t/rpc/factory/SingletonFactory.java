package top.circle1t.rpc.factory;

import lombok.SneakyThrows;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public class SingletonFactory {
    private static final Map<Class<?>,Object> INSTANCE_CACHE = new ConcurrentHashMap<>();

    private SingletonFactory(){}

    @SneakyThrows
    public static <T> T getInstance(Class<T> clazz){
        if (Objects.isNull(clazz)){
            throw new RuntimeException("class不能为空");
        }

        if(INSTANCE_CACHE.containsKey(clazz)){
            return clazz.cast(INSTANCE_CACHE.get(clazz));
        }

        synchronized (SingletonFactory.class){
            if(INSTANCE_CACHE.containsKey(clazz)){
                return clazz.cast(INSTANCE_CACHE.get(clazz));
            }

            T t = clazz.getConstructor().newInstance();
            INSTANCE_CACHE.put(clazz,t);
            return t;
        }
    }

    // 手动放入实例（若已存在则不覆盖，避免误操作）
    public static void putInstance(Class<?> clazz, Object instance) {
        if (Objects.isNull(clazz) || Objects.isNull(instance)) {
            throw new RuntimeException("class或instance不能为空");
        }
        // 仅在缓存中不存在时放入，防止覆盖已有单例
        INSTANCE_CACHE.putIfAbsent(clazz, instance);
    }
}
