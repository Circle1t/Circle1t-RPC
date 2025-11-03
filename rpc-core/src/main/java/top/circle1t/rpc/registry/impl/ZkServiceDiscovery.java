package top.circle1t.rpc.registry.impl;

import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.loadbalance.LoadBalance;
import top.circle1t.rpc.loadbalance.impl.RandomLoadBalance;
import top.circle1t.rpc.registry.ServiceDiscovery;
import top.circle1t.rpc.registry.zk.ZkClient;
import top.circle1t.rpc.util.IPUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final ZkClient zkClient;
    private final LoadBalance loadBalance;

    public ZkServiceDiscovery(){
        this(SingletonFactory.getInstance(ZkClient.class),SingletonFactory.getInstance(RandomLoadBalance.class));
    }

    public ZkServiceDiscovery(ZkClient zkClient, LoadBalance loadBalance){
        this.zkClient = zkClient;
        this.loadBalance = loadBalance;
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String path = RpcConstant.ZK_RPC_ROOT_PATH + "/" + rpcRequest.getRpcServiceName();
        List<String> childrenNodes = zkClient.getChildrenNodes(path);

        String addr = loadBalance.select(childrenNodes);
        return IPUtil.parseIPWithPortToInetSocketAddress(addr);
    }
}
