package top.circle1t.rpc.transmission.netty.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * @author Circle1t
 * @since 2025/11/07
 */
public class ChannelPool {
    private final Map<String, Channel> pool = new ConcurrentHashMap<>();

    public Channel get(InetSocketAddress inetSocketAddress, Supplier<Channel> channelSupplier){
        String addrString = inetSocketAddress.toString();

        Channel channel = pool.get(addrString);
        if(channel != null && channel.isActive()){
            return channel;
        }

        Channel newChannel = channelSupplier.get();
        pool.put(addrString, newChannel);
        return newChannel;
    }
}