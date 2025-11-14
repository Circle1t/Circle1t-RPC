package top.circle1t.rpc.loadbalance.impl;

import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.loadbalance.LoadBalance;

import java.util.List;

/**
 * @author Circle1t
 * @since 2025/11/09
 */
public class RoundLoadBalance implements LoadBalance {
    private int index = -1;
    @Override
    public String select(List<String> list, RpcRequest rpcRequest) {
        index++;
        return list.get(index % list.size());
    }
}
