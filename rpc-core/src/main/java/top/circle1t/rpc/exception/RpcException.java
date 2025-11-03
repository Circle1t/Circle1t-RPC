package top.circle1t.rpc.exception;

/**
 * @author Circle1t
 * @since 2025/10/30
 */
public class RpcException extends RuntimeException {
    public RpcException() {
        super();
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
