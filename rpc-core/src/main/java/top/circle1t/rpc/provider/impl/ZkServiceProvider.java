package top.circle1t.rpc.provider.impl;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.provider.ServiceProvider;
import top.circle1t.rpc.registry.ServiceRegistry;
import top.circle1t.rpc.registry.impl.ZkServiceRegistry;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public class ZkServiceProvider implements ServiceProvider {
    private final Map<String, Object> SERVICE_CACHE = new HashMap<>();
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProvider() {
        this(SingletonFactory.getInstance(ZkServiceRegistry.class));
    }

    public ZkServiceProvider(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        rpcServiceConfig.getRpcServiceNames().forEach(serviceName -> {
            publishService(rpcServiceConfig, serviceName);
        });
    }

    @SneakyThrows
    private void publishService(RpcServiceConfig rpcServiceConfig, String serviceName) {
        String host = InetAddress.getLocalHost().getHostAddress();
        int port = RpcConstant.SERVER_PORT;

        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        serviceRegistry.register(serviceName, inetSocketAddress);

        SERVICE_CACHE.put(serviceName, rpcServiceConfig.getService());
    }

    @Override
    public Object getService(String rpcServiceName) {
        if(StrUtil.isBlank(rpcServiceName)) throw new IllegalArgumentException("服务名不能为空");
        if(!SERVICE_CACHE.containsKey(rpcServiceName)){
            throw new RuntimeException("服务名：" + rpcServiceName + "不存在");
        }
        return SERVICE_CACHE.get(rpcServiceName);
    }
}
