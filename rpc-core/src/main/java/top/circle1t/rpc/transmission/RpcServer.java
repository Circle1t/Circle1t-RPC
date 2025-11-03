package top.circle1t.rpc.transmission;

import top.circle1t.rpc.config.RpcServiceConfig;

/**
 * @author Circle1t
 * @since 2025/10/29
 */
public interface RpcServer {
    void start();

    void publishService(RpcServiceConfig rpcServiceConfig);
}
