package top.circle1t.rpc.transmission.netty.client;

import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.exception.RpcException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Circle1t
 * @since 2025/11/08
 */
public class UnprocessedRpcRequest {
    private static final Map<String, CompletableFuture<RpcResponse<?>>> RESPONSE_CF_MAP = new ConcurrentHashMap<>();

    public static void put(String requestId, CompletableFuture<RpcResponse<?>> future){
        RESPONSE_CF_MAP.put(requestId, future);
    }

    public static void completed(RpcResponse<?> response){
        CompletableFuture<RpcResponse<?>> future = RESPONSE_CF_MAP.remove(response.getRequestId());
        if (future != null){
            future.complete(response);
        } else {
            throw new RpcException("UnprocessedRpcRequest请求异常");
        }
    }
}
