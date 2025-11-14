package top.circle1t.rpc.spi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.serialize.Serializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义的SPI（Service Provider Interface）加载器，主要作用如下：
 * SPI机制实现：基于Java SPI思想，通过读取META-INF/circle1t-rpc/目录下的配置文件来动态加载和实例化指定接口的不同实现类。
 * 泛型支持：支持为任意接口类型创建对应的加载器实例，例如可以用来加载Serializer序列化器的不同实现。
 * 双重缓存机制：
 * 使用classCache缓存已加载的类信息
 * 使用objectCache缓存已实例化的对象，提高获取实例的效率
 * 线程安全：在获取实例时采用双重检查锁定（Double-checked locking）模式保证线程安全
 * 懒加载与按需初始化：只有在真正需要使用某个实现时才会去加载并实例化对应的类
 * 该类的核心功能就是根据名称获取对应的服务实现类实例，在RPC框架中通常用于灵活切换不同的序列化、负载均衡等组件实现。
 * @author Circle1t
 * @since 2025/11/09
 */
@Slf4j
public class CustomLoader<T> {

    private static final String PREFIX = "META-INF/circle1t-rpc/";

    // META-INF下每一种类型对应一个CustomLoader 比如Serializer
    private final Class<T> type;
    // 缓存已加载的类信息
    private final Map<String, Class<T>> classCache = new ConcurrentHashMap<>();
    // 缓存每种实现类的实例
    private final Map<String, Holder<T>> objectCache = new ConcurrentHashMap<>();
    // 缓存每种类的加载器
    private static final Map<Class<?>, CustomLoader<?>> LOADER_CACHE = new ConcurrentHashMap<>();

    public CustomLoader(Class<T> type) {
        this.type = type;
    }

    public T get(String name) {
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("名称不能为空");
        }
        Holder<T> holder = objectCache.computeIfAbsent(name, k -> new Holder<>());

        T t = holder.get();
        if (t == null) {
            synchronized (holder) {
                t = holder.get();
                if (t == null) {
                    t = createObject(name);
                    holder.set(t);
                }
            }
        }
        return t;
    }

    public static <V> CustomLoader<V> getLoader(Class<V> clazz) {
        if(clazz == null){
            throw new IllegalArgumentException("clazz不能为空");
        }

        if(!clazz.isInterface()){
            throw new IllegalArgumentException("clazz必须是接口");
        }

        return (CustomLoader<V>) LOADER_CACHE.computeIfAbsent(clazz, __ -> new CustomLoader<>(clazz));
    }

    @SneakyThrows
    private T createObject(String name) {
        if (classCache.isEmpty()) {
            loadDir();
        }
        Class<T> clazz = classCache.get(name);

        // Java 17 推荐方式
        // 获取无参构造器（如果需要参数化构造，可传入参数类型，如 getDeclaredConstructor(String.class, int.class)）
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        // 实例化
        return constructor.newInstance();
    }

    @SneakyThrows
    private void loadDir() {
        String path = PREFIX + type.getName();
        ClassLoader classLoader = CustomLoader.class.getClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        if (CollUtil.isEmpty(resources)) {
            throw new RuntimeException("未找到" + path);
        }
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            loadResource(classLoader, resource);
        }
    }

    /**
     * 加载资源
     * 例如 kryo=top.circle1t.rpc.serialize.impl.KryoSerializer
     *
     * @param classLoader
     * @param url
     */
    @SneakyThrows
    private void loadResource(ClassLoader classLoader, URL url) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int index = line.indexOf("=");
                if (index <= 0) {
                    continue;
                }
                String name = line.substring(0, index);
                String className = line.substring(index + 1);
                if (StrUtil.isBlank(className)) {
                    continue;
                }
                Class<T> clazz = (Class<T>) Class.forName(className, true, classLoader);
                classCache.put(name, clazz);
            }
        } catch (Exception e) {
            log.error("加载资源失败", e);
        }
    }

//    public static void main(String[] args) {
//        CustomLoader<Serializer> loader = CustomLoader.getLoader(Serializer.class);
//        Serializer serializer = loader.get("kryo");
//        System.out.println(serializer);
//    }
//    top.circle1t.rpc.serialize.impl.KryoSerializer@5b464ce8

}
