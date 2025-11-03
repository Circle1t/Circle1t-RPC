package top.circle1t.rpc.handler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.provider.ServiceProvider;

import java.lang.reflect.Method;

/**
 * 具体的请求方法处理由 RpcReqHandler类完成
 *
 * @author Circle1t
 * @since 2025/10/29
 */
@Slf4j
public class RpcReqHandler {
    private final ServiceProvider serviceProvider;

    public RpcReqHandler(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }


    /**
     * @SneakyThrows 注解的作用如下：
     * 隐藏异常声明：该注解来自 Lombok 库，用于隐藏方法可能抛出的受检异常（checked exceptions）
     * 避免 throws 声明：使用此注解后，方法签名中不需要显式声明 throws 子句，但仍会在运行时正常抛出异常
     * 简化代码：避免在方法签名中添加冗长的异常声明，使代码更简洁
     * @param rpcRequest
     * @return 服务调用结果
     */
    @SneakyThrows
    public Object invokeMethod(RpcRequest rpcRequest){
        String rpcServiceName = rpcRequest.getRpcServiceName();
        Object service = serviceProvider.getService(rpcServiceName);

        log.debug("获取到对应的服务：{}",service.getClass().getCanonicalName());
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        return method.invoke(service, rpcRequest.getParameters());
    }
}
