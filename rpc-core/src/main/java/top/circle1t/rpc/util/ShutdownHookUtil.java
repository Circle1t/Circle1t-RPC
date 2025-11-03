package top.circle1t.rpc.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Circle1t
 * @since 2025/10/30
 */
@Slf4j
public class ShutdownHookUtil {
    public static void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("系统结束运行，清理资源");
            ThreadPoolUtil.shutdownAll();
        }));
    }
}
