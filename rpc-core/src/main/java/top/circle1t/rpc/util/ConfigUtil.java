package top.circle1t.rpc.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.setting.dialect.Props;
import top.circle1t.rpc.config.RpcConfig;

/**
 * @author Circle1t
 * @since 2025/11/09
 */
public class ConfigUtil {
    public static final String CONFIG_FILE_NAME = "rpc-config.properties";
    private static RpcConfig rpcConfig;

    private static void loadConfig(){
        if(ResourceUtil.getResource(CONFIG_FILE_NAME) == null){
            rpcConfig = new RpcConfig();
            return;
        }
        Props props = new Props(CONFIG_FILE_NAME);
        if(props.isEmpty()){
            rpcConfig = new RpcConfig();
            return;
        }

        rpcConfig = props.toBean(RpcConfig.class);
    }

    public static RpcConfig getRpcConfig(){
        if(rpcConfig == null){
            loadConfig();
        }
        return rpcConfig;
    }
}
