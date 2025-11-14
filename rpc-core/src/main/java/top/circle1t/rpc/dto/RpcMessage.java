package top.circle1t.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.circle1t.rpc.enums.CompressType;
import top.circle1t.rpc.enums.MessageType;
import top.circle1t.rpc.enums.SerializeType;
import top.circle1t.rpc.enums.VersionType;

import java.io.Serializable;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // 消息相关配置

    // 请求ID
    private Integer requestId;
    // 版本
    private VersionType version;
    // 消息类型
    private MessageType messageType;
    // 序列化类型
    private SerializeType serializeType;
    // 压缩类型
    private CompressType compressType;
    // 消息体
    private Object data;

}
