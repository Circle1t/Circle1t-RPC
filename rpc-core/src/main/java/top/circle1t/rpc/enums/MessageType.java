package top.circle1t.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
@ToString
@Getter
@AllArgsConstructor
public enum MessageType {

    HEARTBEAT_REQUEST((byte)1, "心跳请求"),
    HEARTBEAT_RESPONSE((byte)2, "心跳响应"),
    RPC_REQUEST((byte)3, "RPC请求"),
    RPC_RESPONSE((byte)4, "RPC响应");

    private final byte code;
    private final String description;

    public boolean isHeartBeat() {
        return this == HEARTBEAT_REQUEST || this == HEARTBEAT_RESPONSE;
    }

    public boolean isRequest() {
        return this == RPC_REQUEST || this == HEARTBEAT_REQUEST;
    }

    public static MessageType getMessageType(byte code) {
        return Arrays.stream(values())
                .filter(messageType -> messageType.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("code异常：" + code));
    }
}
