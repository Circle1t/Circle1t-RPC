package top.circle1t.rpc.registry.zk;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import top.circle1t.rpc.constant.RpcConstant;
import top.circle1t.rpc.util.IPUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
@Slf4j
public class ZkClient {
    // 重试之间的等待时间
    private static final int BASE_SLEEP_TIME_MS = 1000;
    // 最大重试次数
    private static final int MAX_RETRIES = 3;

    private final CuratorFramework client;

    // 主要用于客户端 客户端可以通过key从缓存中获取对应的服务所有地址 例如 /circle1t-rpc/rpcServiceName : [192.168.0.1:8080, 192.168.0.2:8080]
    private final Map<String, List<String>> SERVICE_ADDRESS_CACHE = new ConcurrentHashMap<>();
    // 主要用于服务端 SET里面缓存整个服务的完整地址 例如 /circle1t-rpc/rpcServiceName/192.168.0.1:8080
    private final Set<String> SERVICE_ADDRESS_SET = ConcurrentHashMap.newKeySet();

    public ZkClient(){
        this(RpcConstant.ZK_IP, RpcConstant.ZK_PORT);
    }

    public ZkClient(String host, int port){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES);

        this.client = CuratorFrameworkFactory.builder()
                .connectString(host + ":" + port)
                .retryPolicy(retryPolicy)
                .build();

        log.info("ZkClient正在启动...");
        client.start();
        log.info("ZkClient启动成功");
    }

    public void createPersistentNode(String path){
        try {
            if(StrUtil.isBlank(path)) throw new IllegalArgumentException("path不能为空");

            // 如果本地缓存里面有节点数据，就直接返回
            if(SERVICE_ADDRESS_SET.contains(path)){
                log.info("节点已存在：{}", path);
                return;
            }

            // 如果本地缓存里面没有节点数据，就从zk中获取
            if(client.checkExists().forPath(path) != null){
                SERVICE_ADDRESS_SET.add(path);
                log.info("节点已存在：{}", path);
                return;
            }

            // 如果zk也不存在就创建，并添加到本地缓存中
            log.info("创建持久节点：{}", path);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            SERVICE_ADDRESS_SET.add(path);
        } catch (Exception e) {
            log.error("创建持久节点发生异常：{}", e.getMessage());
        }
    }

    public List<String> getChildrenNodes(String path){
        try {
            if(StrUtil.isBlank(path)) throw new IllegalArgumentException("path不能为空");
            if(SERVICE_ADDRESS_CACHE.containsKey(path)){
                log.info("从缓存中获取子节点：{}", path);
                return SERVICE_ADDRESS_CACHE.get(path);
            }
            log.info("从zk中获取子节点：{}", path);
            List<String> children = client.getChildren().forPath(path);
            SERVICE_ADDRESS_CACHE.put(path, children);
            watchNode(path);
            return children;
        } catch (Exception e) {
            log.error("获取子节点发生异常：{}", e.getMessage());
            return null;
        }
    }


    /**
     * 监听节点的子节点变化
     * @param path 要监听的节点路径
     */
    private void watchNode(String path) {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);

        PathChildrenCacheListener pathChildrenCacheListener = (client, event) -> {
            List<String> children = client.getChildren().forPath(path);
            SERVICE_ADDRESS_CACHE.put(path, children);
            pathChildrenCache.start();
        };
    }

    public void clearIPWithPort(InetSocketAddress address){
        if(Objects.isNull(address)) throw new IllegalArgumentException("address不能为空");
        String ipWithPort = IPUtil.toIPWithPort(address);
        SERVICE_ADDRESS_SET.forEach(path -> {
            if(path.endsWith(ipWithPort)){
                log.debug("清除IPCWithPort: {}", ipWithPort);
                try{
                    client.delete().deletingChildrenIfNeeded().forPath(path);
                } catch (Exception e) {
                    log.error("清除IPWithPort发生异常：{}", e.getMessage());
                }
            }
        });
    }

}
