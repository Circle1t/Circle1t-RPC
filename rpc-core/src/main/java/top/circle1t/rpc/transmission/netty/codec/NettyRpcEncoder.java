package top.circle1t.rpc.transmission.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import top.circle1t.rpc.compress.Compress;
import top.circle1t.rpc.compress.impl.GzipCompress;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.dto.RpcMessage;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.serialize.Serializer;
import top.circle1t.rpc.serialize.impl.KryoSerializer;
import top.circle1t.rpc.spi.CustomLoader;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
public class NettyRpcEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ID_GEN = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        // 写入魔数，用来区分协议类型，相同魔数表示是同一协议
        byteBuf.writeBytes(RpcConstant.RPC_MAGIC_CODE);
        // 写入版本号，用于区分协议版本
        byteBuf.writeByte(rpcMessage.getVersion().getCode());

        // 向右挪动4位，给报文总长度留出空间，因为现在这里还不能确定
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);

        // 写入消息类型
        byteBuf.writeByte(rpcMessage.getMessageType().getCode());
        // 写入序列化类型
        byteBuf.writeByte(rpcMessage.getSerializeType().getCode());
        // 写入压缩类型
        byteBuf.writeByte(rpcMessage.getCompressType().getCode());
        // 写入请求ID
        byteBuf.writeInt(ID_GEN.getAndIncrement());

        int msgLength = RpcConstant.REQUEST_HEADER_LENGTH;
        if(!rpcMessage.getMessageType().isHeartBeat() && rpcMessage.getData() != null){
            byte[] bytes = dataToBytes(rpcMessage);
            byteBuf.writeBytes(bytes);
            msgLength += bytes.length;
        }

        int currentIndex = byteBuf.writerIndex();
        // 当前指针处于报文末尾，先把指针移到报文开头，再把指针移动到报文长度部分，写入报文总长度
        byteBuf.writerIndex(currentIndex - msgLength + RpcConstant.RPC_MAGIC_CODE.length + 1);
        byteBuf.writeInt(msgLength);
        byteBuf.writerIndex(currentIndex);

    }

    private byte[] dataToBytes(RpcMessage rpcMessage){
        String serializerDescription = rpcMessage.getSerializeType().getDescription();
        Serializer serializer = CustomLoader.getLoader(Serializer.class).get(serializerDescription);
        byte[] bytes = serializer.serialize(rpcMessage.getData());

        Compress compress = SingletonFactory.getInstance(GzipCompress.class);
        bytes = compress.compress(bytes);

        return bytes;


    }
}
