package top.circle1t.rpc.transmission.socket.server;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.handler.RpcReqHandler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Circle1t
 * @since 2025/10/30
 */
@Slf4j
@AllArgsConstructor
public class SocketRequestHandler implements Runnable {

    private final Socket socket;
    private final RpcReqHandler rpcReqHandler;

    @SneakyThrows
    @Override
    public void run() {
        log.info("客户端连接成功！");
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        RpcRequest rpcRequest = (RpcRequest) inputStream.readObject();
        log.info("服务端接收到请求：{}",rpcRequest);

        Object data = rpcReqHandler.invokeMethod(rpcRequest);

        RpcResponse<?> response = RpcResponse.success(rpcRequest.getRequestId(),data);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(response);
        outputStream.flush();
        log.info("服务端返回结果：{}",response);
    }
}
