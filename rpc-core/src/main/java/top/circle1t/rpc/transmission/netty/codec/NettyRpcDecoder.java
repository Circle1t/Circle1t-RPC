package top.circle1t.rpc.transmission.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import top.circle1t.rpc.compress.Compress;
import top.circle1t.rpc.compress.impl.GzipCompress;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.dto.RpcMessage;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.enums.CompressType;
import top.circle1t.rpc.enums.MessageType;
import top.circle1t.rpc.enums.SerializeType;
import top.circle1t.rpc.enums.VersionType;
import top.circle1t.rpc.factory.SingletonFactory;
import top.circle1t.rpc.serialize.Serializer;
import top.circle1t.rpc.serialize.impl.KryoSerializer;
import top.circle1t.rpc.spi.CustomLoader;

import java.util.Arrays;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
public class NettyRpcDecoder extends LengthFieldBasedFrameDecoder {

    // 最长帧长，长度域偏移量，长度域长度，长度适配器（用来确定帧尾，到达帧尾所需字节数 = 总长 + lengthAdjustment），初始数据偏移量（我们要读取包括魔数等所有参数，所以为0）
    public NettyRpcDecoder() {
        super(RpcConstant.REQUEST_MAX_LENGTH, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        return decodeFrame(frame);
    }

    private Object decodeFrame(ByteBuf frame) {
        // 读取魔数
        readMagicCode(frame);

        byte versionCode = frame.readByte();
        VersionType version = VersionType.getVersionType(versionCode);
        int msgLength = frame.readInt();

        byte messageTypeCode = frame.readByte();
        MessageType messageType = MessageType.getMessageType(messageTypeCode);

        byte serializeTypeCode = frame.readByte();
        SerializeType serializeType = SerializeType.getSerializeType(serializeTypeCode);

        byte compressTypeCode = frame.readByte();
        CompressType compressType = CompressType.getCompressType(compressTypeCode);

        int requestId = frame.readInt();

        Object data = readData(frame, msgLength - RpcConstant.REQUEST_HEADER_LENGTH, messageType, serializeType);


        return RpcMessage.builder()
                .requestId(requestId)
                .version(version)
                .compressType(compressType)
                .messageType(messageType)
                .serializeType(serializeType)
                .data(data)
                .build();
    }

    private void readMagicCode(ByteBuf frame) {
        // 读取魔数
        byte[] magicCode = new byte[RpcConstant.RPC_MAGIC_CODE.length];
        frame.readBytes(magicCode);

        if (!Arrays.equals(magicCode, RpcConstant.RPC_MAGIC_CODE)) {
            // 魔数错误
            throw new RuntimeException("魔数异常");
        }
    }

    private Object readData(ByteBuf frame, int dataLength, MessageType messageType, SerializeType serializeType) {
        if (messageType.isRequest()) {
            return readData(frame, dataLength, RpcRequest.class, serializeType);
        }
        return readData(frame, dataLength, RpcResponse.class, serializeType);
    }

    private <T> T readData(ByteBuf frame, int dataLength, Class<T> clazz, SerializeType serializeType) {
        if (dataLength <= 0) {
            return null;
        }

        byte[] data = new byte[dataLength];
        frame.readBytes(data);

        Compress compress = SingletonFactory.getInstance(GzipCompress.class);
        data = compress.decompress(data);

        String serializerDescription = serializeType.getDescription();
        Serializer serializer = CustomLoader.getLoader(Serializer.class).get(serializerDescription);

        return serializer.deserialize(data, clazz);
    }

}
