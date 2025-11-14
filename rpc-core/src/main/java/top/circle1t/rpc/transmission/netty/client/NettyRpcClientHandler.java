package top.circle1t.rpc.transmission.netty.client;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.dto.RpcMessage;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.enums.CompressType;
import top.circle1t.rpc.enums.MessageType;
import top.circle1t.rpc.enums.SerializeType;
import top.circle1t.rpc.enums.VersionType;
import top.circle1t.rpc.util.ConfigUtil;

/**
 * @author Circle1t
 * @since 2025/11/05
 */
@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage rpcMessage) throws Exception {

        if (rpcMessage.getMessageType().isHeartBeat()){
            // TODO 还需要处理服务端发送的心跳响应，要不然无法确定服务器是否挂了
            log.info("收到服务端心跳包，{}", rpcMessage);
            return;
        }

        log.info("收到服务端数据：{}", rpcMessage);
        RpcResponse<?> rpcResponse = (RpcResponse<?>) rpcMessage.getData();
        UnprocessedRpcRequest.completed(rpcResponse);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean isNeedHeartbeat = evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE;
        if (!isNeedHeartbeat){
            super.userEventTriggered(ctx, evt);
            return;
        }

        String serializer = ConfigUtil.getRpcConfig().getSerializer();

        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(MessageType.HEARTBEAT_REQUEST)
                .compressType(CompressType.GZIP)
                .serializeType(SerializeType.getSerializeType(serializer))
                .version(VersionType.VERSION_1)
                .build();
        log.info("客户端发送心跳包，{}", rpcMessage);
        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端发生异常", cause);
        ctx.close();
    }
}
