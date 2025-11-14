package top.circle1t.rpc.transmission.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.dto.RpcMessage;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.enums.CompressType;
import top.circle1t.rpc.enums.MessageType;
import top.circle1t.rpc.enums.SerializeType;
import top.circle1t.rpc.enums.VersionType;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.handler.RpcReqHandler;
import top.circle1t.rpc.provider.impl.ZkServiceProvider;
import top.circle1t.rpc.util.ConfigUtil;

/**
 * @author Circle1t
 * @since 2025/11/05
 */
@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private final RpcReqHandler rpcReqHandler;

    // 类加载时初始化RpcReqHandler并注册到单例工厂
    static {
        // 获取ZkServiceProvider单例（依赖提前初始化）
        ZkServiceProvider zkServiceProvider = SingletonFactory.getInstance(ZkServiceProvider.class);
        // 创建RpcReqHandler实例
        RpcReqHandler rpcReqHandler = new RpcReqHandler(zkServiceProvider);
        // 手动放入单例工厂缓存
        SingletonFactory.putInstance(RpcReqHandler.class, rpcReqHandler);
    }


    public NettyRpcServerHandler() {
        this.rpcReqHandler = SingletonFactory.getInstance(RpcReqHandler.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage rpcMessage) throws Exception {
        log.debug("接收到客户端请求：{}", rpcMessage);

        MessageType messageType;
        Object data;
        // 判断是否是心跳请求 心跳请求不需要返回数据
        if (rpcMessage.getMessageType().isHeartBeat()) {
            messageType = MessageType.HEARTBEAT_RESPONSE;
            data = null;
        } else {
            messageType = MessageType.RPC_RESPONSE;
            RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
            data = handleRpcRequest(rpcRequest);
        }

        String serializer = ConfigUtil.getRpcConfig().getSerializer();

        RpcMessage message = RpcMessage.builder()
                .requestId(rpcMessage.getRequestId())
                .messageType(messageType)
                .compressType(CompressType.GZIP)
                .serializeType(SerializeType.getSerializeType(serializer))
                .version(VersionType.VERSION_1)
                .data(data)
                .build();
        ctx.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

    }

    private RpcResponse<?> handleRpcRequest(RpcRequest rpcRequest) {
        try{
            Object result = rpcReqHandler.invokeMethod(rpcRequest);
            return RpcResponse.success(rpcRequest.getRequestId(), result);
        } catch (Exception e) {
            log.error("处理RPC请求时发生异常：{}", e.getMessage());
            return RpcResponse.fail(rpcRequest.getRequestId(), "处理RPC请求时发生异常：" + e.getMessage());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean isNeedClose = evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.READER_IDLE;
        if (!isNeedClose){
            super.userEventTriggered(ctx, evt);
            return;
        }

        log.info("服务端长时间没有收到客户端的心跳包，关闭channel，地址：{}", ctx.channel().remoteAddress());
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端发生异常", cause);
    }
}
