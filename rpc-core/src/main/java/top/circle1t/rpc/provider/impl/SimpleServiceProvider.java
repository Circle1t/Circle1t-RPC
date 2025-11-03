package top.circle1t.rpc.provider.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.provider.ServiceProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单的服务提供者，在内存中存储服务名和对应的服务对象
 *
 * @author Circle1t
 * @since 2025/10/29
 */
@Slf4j
public class SimpleServiceProvider implements ServiceProvider {

    // Map用来记录 服务名 - 对应对象
    private final Map<String, Object> SERVICE_CACHE = new HashMap<>();

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        List<String> rpcServiceNames = rpcServiceConfig.getRpcServiceNames();

        if(CollectionUtil.isEmpty(rpcServiceNames)){
            throw new RuntimeException("该服务没有实现接口");
        }

        log.debug("发布服务：{}", rpcServiceNames);

        // 获取实现的接口对应的服务名，每个对象可能实现多个接口，因此可能会出现多个服务名对应同一个对象
        rpcServiceConfig.getRpcServiceNames().forEach(serviceName -> {
            SERVICE_CACHE.put(serviceName, rpcServiceConfig.getService());
        });
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
