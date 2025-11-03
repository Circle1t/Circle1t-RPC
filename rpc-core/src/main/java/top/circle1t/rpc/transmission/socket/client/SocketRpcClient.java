package top.circle1t.rpc.transmission.socket.client;

import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.registry.ServiceDiscovery;
import top.circle1t.rpc.registry.impl.ZkServiceDiscovery;
import top.circle1t.rpc.transmission.RpcClient;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 基于Socket的RPC客户端实现类
 * 负责通过Socket网络连接，向服务端发送序列化的RpcRequest对象，
 * 并接收服务端返回的序列化RpcResponse对象，完成远程调用
 *
 * @author Circle1t
 * @since 2025/10/29
 */
@Slf4j
public class SocketRpcClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public SocketRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 实现RpcClient接口的核心方法：发送RPC请求并获取响应
     * @param request 封装了远程调用信息的RpcRequest对象（包含接口名、方法名、参数等）
     * @return 服务端返回的RpcResponse对象（包含调用结果或异常信息），异常时返回null
     */
    @Override
    public RpcResponse<?> sendRequest(RpcRequest request) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(request);
        // try-with-resources语法：自动关闭实现AutoCloseable接口的资源（此处为Socket），避免资源泄露
        try (Socket socket = new Socket(inetSocketAddress.getHostName(), inetSocketAddress.getPort())) {
            // 1. 创建Socket连接：连接服务端的IP和端口

            // 2. 创建对象输出流：用于将Java对象（RpcRequest）序列化为字节流，通过Socket发送到服务端
            //    要求RpcRequest必须实现Serializable接口，否则序列化会失败
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            // 3. 发送请求：将RpcRequest对象写入输出流（序列化过程）
            objectOutputStream.writeObject(request);
            objectOutputStream.flush(); // 强制刷新缓冲区，确保数据立即通过Socket发送，避免滞留

            // 4. 创建对象输入流：用于从Socket读取服务端返回的字节流，并反序列化为Java对象（RpcResponse）
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            // 5. 接收响应：读取服务端返回的序列化数据，反序列化为Object对象，再强转为RpcResponse<?>
            Object o = inputStream.readObject();
            return (RpcResponse<?>) o;

        } catch (Exception e) {
            log.error("发送RPC请求失败", e);
        }
        return null;
    }
}