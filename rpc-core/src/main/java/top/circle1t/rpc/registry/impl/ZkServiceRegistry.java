package top.circle1t.rpc.registry.impl;

import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.registry.ServiceRegistry;
import top.circle1t.rpc.registry.zk.ZkClient;
import top.circle1t.rpc.util.IPUtil;

import java.net.InetSocketAddress;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    private final ZkClient zkClient;

    public ZkServiceRegistry(){
        this(SingletonFactory.getInstance(ZkClient.class));
    }

    public ZkServiceRegistry(ZkClient zkClient){
        this.zkClient = zkClient;
    }
    @Override
    public void register(String rpcServiceName, InetSocketAddress address) {
        log.info("注册服务：{}, address：{}", rpcServiceName, address);

        String path = RpcConstant.ZK_RPC_ROOT_PATH + "/" + rpcServiceName + "/" + IPUtil.toIPWithPort(address);
        zkClient.createPersistentNode(path);

    }
}
