package top.circle1t.rpc.util;

import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.proxy.RpcClientProxy;
import top.circle1t.rpc.transmission.RpcClient;
import top.circle1t.rpc.transmission.socket.client.SocketRpcClient;

import java.lang.reflect.Proxy;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public class ProxyUtil {
    private static final RpcClient rpcClient = SingletonFactory.getInstance(SocketRpcClient.class);
    private static final RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);

    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> clazz) {
        return rpcClientProxy.getProxy(clazz);
    }
}
