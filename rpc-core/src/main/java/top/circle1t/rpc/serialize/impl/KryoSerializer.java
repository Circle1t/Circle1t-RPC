package top.circle1t.rpc.serialize.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.dto.RpcMessage;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.enums.CompressType;
import top.circle1t.rpc.enums.MessageType;
import top.circle1t.rpc.enums.SerializeType;
import top.circle1t.rpc.enums.VersionType;
import top.circle1t.rpc.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
@Slf4j
public class KryoSerializer implements Serializer {

    /**
     * 在这段代码中，KRYO_THREAD_LOCAL 的核心作用是解决 Kryo 框架的线程安全问题，同时兼顾序列化性能。具体原因和作用可以从以下两方面分析：
     * 1. Kryo 的线程安全问题
     * Kryo 是一个高效的 Java 序列化框架，但它本身不是线程安全的。Kryo 内部维护了对象引用缓存、类元信息等状态，当多个线程同时使用同一个 Kryo 实例进行序列化 / 反序列化时，会导致状态错乱（比如对象引用冲突、类信息混淆），最终可能出现数据错误、序列化失败甚至程序崩溃。
     * 2. ThreadLocal 的作用：为每个线程分配独立的 Kryo 实例
     * ThreadLocal 是 Java 中用于线程本地存储的工具类，它可以为每个线程创建一个独立的变量副本（这里是 Kryo 实例），各个线程之间的 Kryo 实例互不干扰。
     *
     * 每个线程首次使用 KRYO_THREAD_LOCAL 时，会通过 withInitial 初始化一个专属的 Kryo 实例，并注册 RpcRequest 和 RpcResponse 类（提前注册类可以提升 Kryo 的序列化性能，避免重复解析类元信息）。
     * 后续该线程再次使用时，直接获取自己线程内的 Kryo 实例，无需重复创建。
     *
     * 为什么要这样设计？
     * 这样做的核心目的是在 “线程安全” 和 “性能” 之间做平衡：
     * 保证线程安全：通过 ThreadLocal 隔离每个线程的 Kryo 实例，避免多线程并发访问导致的状态混乱。
     * 减少性能损耗：Kryo 实例的创建和类注册有一定开销，ThreadLocal 允许每个线程复用自己的 Kryo 实例，避免频繁创建 / 销毁实例带来的性能浪费。
     *
     * withInitial 是 Java 中 ThreadLocal 类的一个静态方法（Java 8 引入），用于创建一个 ThreadLocal 实例，并指定该实例的初始值生成逻辑。
     * 具体作用：
     * 当你通过 ThreadLocal.withInitial 创建 ThreadLocal 时，supplier 参数（一个函数式接口）会定义 “当前线程首次访问该 ThreadLocal 时，如何生成初始值”。
     * 当线程第一次调用 ThreadLocal.get() 方法时，会自动执行 supplier.get() 生成初始值，并将这个值与当前线程绑定。
     * 后续该线程再次调用 get() 时，直接返回之前绑定的初始值（不会重复执行 supplier）。
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
               Output output = new Output(byteArrayOutputStream);
        ){
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.writeObject(output, obj);
            output.flush();
            log.info("=========使用Kryo做序列化=========");
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("Kryo序列化失败");
            throw new RuntimeException("Kryo序列化失败");
        } finally {
            KRYO_THREAD_LOCAL.remove();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream);
               ){
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            log.info("=========使用Kryo做反序列化=========");
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            log.error("Kryo反序列化失败");
            throw new RuntimeException("Kryo反序列化失败");
        } finally {
            KRYO_THREAD_LOCAL.remove();
        }
    }
}
