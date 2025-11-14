package top.circle1t.rpc.constant;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public class RpcConstant {
    public static final int SERVER_PORT = 8888;

    public static final String ZK_IP = "192.168.150.128";
    public static final int ZK_PORT = 2181;
    public static final String ZK_RPC_ROOT_PATH = "/circle1t-rpc";

    public static final String NETTY_RPC_KEY = "rpcResponse";

    public static final byte[] RPC_MAGIC_CODE = new byte[]{(byte)'c',(byte)'r',(byte)'p',(byte)'c'};
    public static final int REQUEST_HEADER_LENGTH = 16;
    public static final int REQUEST_MAX_LENGTH = 1024 * 1024 * 8;
}
