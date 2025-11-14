package top.circle1t.rpc.transmission;

import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;

import java.util.concurrent.Future;

/**
 * @author Circle1t
 * @since 2025/10/29
 */
public interface RpcClient {
    Future<RpcResponse<?>> sendRequest(RpcRequest request);
}
