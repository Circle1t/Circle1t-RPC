package top.circle1t.rpc.handler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.util.concurrent.RateLimiter;
import top.circle1t.rpc.annotation.Limit;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.exception.RpcException;
import top.circle1t.rpc.provider.ServiceProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 具体的请求方法处理由 RpcReqHandler类完成
 *
 * @author Circle1t
 * @since 2025/10/29
 */
@Slf4j
public class RpcReqHandler {
    private final ServiceProvider serviceProvider;
    private final Map<String, RateLimiter> RATE_LIMITER_MAP = new ConcurrentHashMap<>();

    public RpcReqHandler(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }

    public Object invokeMethod(RpcRequest rpcRequest) {
        try {
            String rpcServiceName = rpcRequest.getRpcServiceName();
            Object service = serviceProvider.getService(rpcServiceName);

            log.debug("获取到对应的服务：{}", service.getClass().getCanonicalName());
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());

            Limit limit = method.getAnnotation(Limit.class);
            if(limit == null){
                return method.invoke(service, rpcRequest.getParameters());
            }

            // 获取限流器
            RateLimiter rateLimiter = RATE_LIMITER_MAP.computeIfAbsent(rpcServiceName, k -> RateLimiter.create(limit.permitsPerSecond()));
            // 尝试获取令牌
            if(!rateLimiter.tryAcquire(limit.timeout(), TimeUnit.MILLISECONDS)){
                throw new RpcException("系统繁忙，请稍后重试");
            }

            return method.invoke(service, rpcRequest.getParameters());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("方法不存在: " + rpcRequest.getMethodName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("方法访问权限不足: " + rpcRequest.getMethodName(), e);
        } catch (InvocationTargetException e) {
            // 关键：获取被调用方法实际抛出的异常
            Throwable targetException = e.getTargetException();
            log.error("服务方法执行异常: {}", targetException.getMessage(), targetException);
            throw new RuntimeException("服务方法执行异常: " + targetException.getMessage(), targetException);
        } catch (Exception e) {
            log.error("调用RPC方法时发生未知异常", e);
            throw new RuntimeException("调用RPC方法时发生未知异常: " + e.getMessage(), e);
        }
    }
}