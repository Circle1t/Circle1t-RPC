package top.circle1t.server;

import lombok.extern.slf4j.Slf4j;
import top.circle1t.api.UserService;
import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.proxy.RpcClientProxy;
import top.circle1t.rpc.transmission.RpcServer;
import top.circle1t.rpc.transmission.socket.server.SocketRpcServer;
import top.circle1t.server.service.UserServiceImpl;

import java.lang.reflect.Proxy;

/**
 * @author Circle1t
 * @since 2025/10/29
 */
@Slf4j
public class TestServerMain {
    public static void main(String[] args) {
        RpcServer rpcServer = new SocketRpcServer(8888);
        rpcServer.publishService(new RpcServiceConfig(new UserServiceImpl()));
        rpcServer.start();

    }
}
