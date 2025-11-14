package top.circle1t.rpc.loadbalance.impl;

import com.google.common.hash.Hashing;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.loadbalance.LoadBalance;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Circle1t
 * @since 2025/11/09
 */
public class ConsistentHashLoadBalance implements LoadBalance {
    @Override
    public String select(List<String> list,RpcRequest rpcRequest) {
        String key = rpcRequest.getRpcServiceName();
        long hashCode = Hashing.murmur3_128().hashString(key, StandardCharsets.UTF_8).asLong();
        int index = Hashing.consistentHash(hashCode, list.size());
        return list.get(index);
    }
}
