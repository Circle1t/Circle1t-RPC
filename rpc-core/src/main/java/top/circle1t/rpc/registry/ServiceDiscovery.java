package top.circle1t.rpc.registry;

import top.circle1t.rpc.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
