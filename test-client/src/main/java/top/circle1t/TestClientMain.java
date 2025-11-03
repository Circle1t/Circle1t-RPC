package top.circle1t;

import lombok.extern.slf4j.Slf4j;
import top.circle1t.api.User;
import top.circle1t.api.UserService;
import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.proxy.RpcClientProxy;
import top.circle1t.rpc.transmission.RpcClient;
import top.circle1t.rpc.transmission.socket.client.SocketRpcClient;
import top.circle1t.rpc.util.ProxyUtil;

/**
 * Hello world!
 *
 */
@Slf4j
public class TestClientMain {
    public static void main( String[] args ) {
        UserService userService = ProxyUtil.getProxy(UserService.class);
        User user = userService.getUserById(1L);
        log.info("{}", user);
    }
}
