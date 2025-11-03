package top.circle1t.rpc.transmission.socket.server;

import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.handler.RpcReqHandler;
import top.circle1t.rpc.provider.ServiceProvider;
import top.circle1t.rpc.provider.impl.ZkServiceProvider;
import top.circle1t.rpc.transmission.RpcServer;
import top.circle1t.rpc.util.ThreadPoolUtil;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Server只处理请求与响应，具体的方法调用由RpcReqHandler处理
 *
 * @author Circle1t
 * @since 2025/10/29
 */
@Slf4j
public class SocketRpcServer implements RpcServer {

    private final int PORT;
    private final ServiceProvider serviceProvider;
    private final RpcReqHandler rpcReqHandler;
    private final ExecutorService executor;

    public SocketRpcServer() {
        this(RpcConstant.SERVER_PORT);
    }

    public SocketRpcServer(int port) {
        this(port, SingletonFactory.getInstance(ZkServiceProvider.class));
    }

    public SocketRpcServer(int port, ServiceProvider serviceProvider){
        this.PORT = port;
        this.serviceProvider = serviceProvider;
        this.rpcReqHandler = new RpcReqHandler(serviceProvider);
        this.executor = ThreadPoolUtil.createIoIncentiveThreadPool("socket-rpc-server-");
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            log.info("服务端启动成功！端口：{}", PORT);
            Socket socket;
            while((socket = serverSocket.accept()) != null){
                executor.submit(new SocketRequestHandler(socket,rpcReqHandler));
            }
        } catch (Exception e){
            log.error("服务端异常",e);
        }
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
        log.info("服务发布成功！服务名：{}",rpcServiceConfig.getRpcServiceNames());
    }

}
