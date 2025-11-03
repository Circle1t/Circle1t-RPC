package top.circle1t.rpc.registry;

import java.net.InetSocketAddress;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public interface ServiceRegistry {
    void register(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
