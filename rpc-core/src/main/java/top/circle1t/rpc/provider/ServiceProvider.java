package top.circle1t.rpc.provider;

import top.circle1t.rpc.config.RpcServiceConfig;

/**
 * @author Circle1t
 * @since 2025/10/29
 */
public interface ServiceProvider {
    void publishService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);
}
