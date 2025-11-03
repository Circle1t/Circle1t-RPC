package top.circle1t.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.circle1t.rpc.enums.RpcResponseStatus;

import java.io.Serializable;

/**
 * @author Circle1t
 * @since 2025/10/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;
    private Integer code;
    private String message;
    private T data;

    public static <T> RpcResponse<T> success(String requestId, T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(0);
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> fail(String requestId, RpcResponseStatus status) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(status.getCode());
        response.setMessage(status.getMessage());
        return response;
    }

    public static <T> RpcResponse<T> fail(String requestId, String message) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(RpcResponseStatus.FAIL.getCode());
        response.setMessage(message);
        return response;
    }
}
