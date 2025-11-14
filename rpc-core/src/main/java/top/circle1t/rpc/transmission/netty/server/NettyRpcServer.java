package top.circle1t.rpc.transmission.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.provider.ServiceProvider;
import top.circle1t.rpc.provider.impl.ZkServiceProvider;
import top.circle1t.rpc.transmission.RpcServer;
import top.circle1t.rpc.transmission.netty.client.NettyRpcClient;
import top.circle1t.rpc.transmission.netty.client.NettyRpcClientHandler;
import top.circle1t.rpc.transmission.netty.codec.NettyRpcDecoder;
import top.circle1t.rpc.transmission.netty.codec.NettyRpcEncoder;
import top.circle1t.rpc.util.ShutdownHookUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author Circle1t
 * @since 2025/11/05
 */
@Slf4j
public class NettyRpcServer implements RpcServer {
    private final ServiceProvider serviceProvider;
    private final int port;

    public NettyRpcServer(){
        this(RpcConstant.SERVER_PORT);
    }

    public NettyRpcServer(int port){
        this(port, SingletonFactory.getInstance(ZkServiceProvider.class));
    }

    public NettyRpcServer(int port, ServiceProvider serviceProvider){
        this.port = port;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            channel.pipeline().addLast(new NettyRpcDecoder());
                            channel.pipeline().addLast(new NettyRpcEncoder());
                            channel.pipeline().addLast(new NettyRpcServerHandler());
                        }
                    });
            ShutdownHookUtil.addShutdownHook();
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("服务端异常",e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }
}
