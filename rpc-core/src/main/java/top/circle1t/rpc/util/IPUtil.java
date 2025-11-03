package top.circle1t.rpc.util;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public class IPUtil {

    // ip:port
    public static String toIPWithPort(InetSocketAddress address){
        if(Objects.isNull(address)) throw new IllegalArgumentException("address不能为空");
        String host = address.getHostString();
        if(Objects.equals(host, "localhost")){
            host = "127.0.0.1";
        }
        return host + ":" + address.getPort();
    }

    public static InetSocketAddress parseIPWithPortToInetSocketAddress(String ipWithPort){
        if(Objects.isNull(ipWithPort)) throw new IllegalArgumentException("ipWithPort不能为空");
        String[] ipAndPort = ipWithPort.split(":");
        if(ipAndPort.length != 2) throw new IllegalArgumentException("ipWithPort格式错误");
        String ip = ipAndPort[0];
        int port = Integer.parseInt(ipAndPort[1]);
        return new InetSocketAddress(ip, port);
    }
}
