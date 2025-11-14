package top.circle1t.rpc.proxy;

import cn.hutool.core.util.IdUtil;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import lombok.SneakyThrows;
import top.circle1t.rpc.annotation.Breaker;
import top.circle1t.rpc.annotation.Retry;
import top.circle1t.rpc.breaker.CircuitBreaker;
import top.circle1t.rpc.breaker.CircuitBreakerManager;
import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.enums.RpcResponseStatus;
import top.circle1t.rpc.exception.RpcException;
import top.circle1t.rpc.transmission.RpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Circle1t
 * @since 2025/10/30
 */
public class RpcClientProxy implements InvocationHandler {
    private final RpcClient rpcClient;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcClient rpcClient) {
        this(rpcClient, new RpcServiceConfig());
    }

    public RpcClientProxy(RpcClient rpcClient, RpcServiceConfig rpcServiceConfig) {
        this.rpcClient = rpcClient;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = RpcRequest.builder()
                .requestId(IdUtil.fastSimpleUUID())
                .interfaceName(method.getDeclaringClass().getCanonicalName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .version(rpcServiceConfig.getVersion())
                .group(rpcServiceConfig.getGroup())
                .build();

        Breaker breaker = method.getAnnotation(Breaker.class);
        if (breaker == null) {
            return sendReqWithRetry(request, method);
        }

        CircuitBreaker circuitBreaker = CircuitBreakerManager.getCircuitBreaker(request.getRpcServiceName(), breaker);
        if(!circuitBreaker.canRequest()){
            throw new RpcException("已被熔断处理");
        }

        try {
            Object o = sendReqWithRetry(request, method);
            circuitBreaker.success();
            return o;
        } catch (Exception e) {
            circuitBreaker.fail();
            throw e;
        }
    }

    @SneakyThrows
    private Object sendReqWithRetry(RpcRequest request, Method method){
        Retry retry = method.getAnnotation(Retry.class);
        if (retry == null) {
            return sendReq(request);
        }

        Retryer<Object> retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(retry.value())
                .withStopStrategy(StopStrategies.stopAfterAttempt(retry.maxAttempts()))
                .withWaitStrategy(WaitStrategies.fixedWait(retry.delay(), TimeUnit.MILLISECONDS))
                .build();
        return retryer.call(() -> sendReq(request));
    }

    private Object sendReq(RpcRequest request) throws InterruptedException, ExecutionException {
        Future<RpcResponse<?>> future = rpcClient.sendRequest(request);
        RpcResponse<?> rpcResponse = future.get();
        check(rpcResponse, request);
        return rpcResponse.getData();
    }

    private void check(RpcResponse<?> rpcResponse, RpcRequest rpcRequest){
        if (Objects.isNull(rpcResponse)) {
            throw new RpcException("服务调用失败：RpcResponse为空 service: " + rpcRequest.getInterfaceName());
        }

        if (!Objects.equals(rpcResponse.getRequestId(), rpcRequest.getRequestId())){
            throw new RpcException("服务调用失败：RpcResponse和RpcRequest的requestId不一致 service: " + rpcRequest.getInterfaceName());
        }

        if (RpcResponseStatus.isFail(rpcResponse.getCode())){
            throw new RpcException("响应值为失败：" + rpcResponse.getMessage());
        }
    }
}
