package top.circle1t.rpc.registry.zk;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import top.circle1t.rpc.constant.RpcConstant;

import java.util.List;

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

    private CuratorFramework client;

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

            if (client.checkExists().forPath(path) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("创建持久节点：{}", path);
            } else {
                log.info("节点已存在：{}", path);
            }
        } catch (Exception e) {
            log.error("创建持久节点发生异常：{}", e.getMessage());
        }
    }

    public List<String> getChildrenNodes(String path){
        try {
            if(StrUtil.isBlank(path)) throw new IllegalArgumentException("path不能为空");
            return client.getChildren().forPath(path);
        } catch (Exception e) {
            log.error("获取子节点发生异常：{}", e.getMessage());
            return null;
        }
    }

}
