package top.circle1t.rpc.transmission.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.dto.RpcMessage;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.enums.CompressType;
import top.circle1t.rpc.enums.MessageType;
import top.circle1t.rpc.enums.SerializeType;
import top.circle1t.rpc.enums.VersionType;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.registry.ServiceDiscovery;
import top.circle1t.rpc.registry.impl.ZkServiceDiscovery;
import top.circle1t.rpc.spi.CustomLoader;
import top.circle1t.rpc.transmission.RpcClient;
import top.circle1t.rpc.transmission.netty.codec.NettyRpcDecoder;
import top.circle1t.rpc.transmission.netty.codec.NettyRpcEncoder;
import top.circle1t.rpc.util.ConfigUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Circle1t
 * @since 2025/11/05
 */
@Slf4j
public class NettyRpcClient implements RpcClient {

    private static final Bootstrap bootstrap;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private final ServiceDiscovery serviceDiscovery;
    private final ChannelPool channelPool;


    public NettyRpcClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public NettyRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.channelPool = SingletonFactory.getInstance(ChannelPool.class);
    }

    static {
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,DEFAULT_CONNECTION_TIMEOUT)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        // 先添加日志处理器
                        channel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        // 添加心跳处理器
                        channel.pipeline().addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
                        // 再添加编解码器和业务处理器
                        channel.pipeline().addLast(new NettyRpcDecoder());
                        channel.pipeline().addLast(new NettyRpcEncoder());
                        channel.pipeline().addLast(new NettyRpcClientHandler());
                    }
                });
    }

    @SneakyThrows
    @Override
    public Future<RpcResponse<?>> sendRequest(RpcRequest request) {
        CompletableFuture<RpcResponse<?>> cf = new CompletableFuture<>();
        UnprocessedRpcRequest.put(request.getRequestId(),cf);

        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(request);
        Channel channel = channelPool.get(inetSocketAddress, () -> connect(inetSocketAddress));
        log.info("NettyRpcClient连接到：{}", inetSocketAddress);

        String serializer = ConfigUtil.getRpcConfig().getSerializer();

        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(MessageType.RPC_REQUEST)
                .compressType(CompressType.GZIP)
                .serializeType(SerializeType.getSerializeType(serializer))
                .version(VersionType.VERSION_1)
                .data(request)
                .build();


        channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) listener -> {
            if(!listener.isSuccess()){
                listener.channel().close();
                cf.completeExceptionally(listener.cause());
            }
        });

        return cf;
    }

    private Channel connect(InetSocketAddress inetSocketAddress) {
        try {
            return bootstrap.connect(inetSocketAddress).sync().channel();
        } catch (InterruptedException e) {
            log.error("连接服务端失败：{}", inetSocketAddress);
            throw new RuntimeException(e);
        }
    }
}
