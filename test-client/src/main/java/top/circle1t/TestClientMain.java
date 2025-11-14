package top.circle1t;

import cn.hutool.core.math.Money;
import lombok.extern.slf4j.Slf4j;
import top.circle1t.api.User;
import top.circle1t.api.UserService;
import top.circle1t.rpc.config.RpcServiceConfig;
import top.circle1t.rpc.dto.RpcRequest;
import top.circle1t.rpc.dto.RpcResponse;
import top.circle1t.rpc.proxy.RpcClientProxy;
import top.circle1t.rpc.transmission.RpcClient;
import top.circle1t.rpc.transmission.netty.client.NettyRpcClient;
import top.circle1t.rpc.transmission.socket.client.SocketRpcClient;
import top.circle1t.rpc.util.ProxyUtil;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
@Slf4j
public class TestClientMain {
    public static void main( String[] args ) {
        UserService userService = ProxyUtil.getProxy(UserService.class);
        User user = userService.getUserById(1L);
        log.info("服务端返回数据：{}", user);
//        Scanner scanner = new Scanner(System.in);
//        ExecutorService executorService = Executors.newFixedThreadPool(20);
//        while(true){
//            System.out.println("请输入请求数：");
//            int num = scanner.nextInt();
//            System.out.println("请输入id：");
//            long id = scanner.nextLong();
//            for(int i = 0; i < num; i++){
//                executorService.execute(() -> {
//                    try {
//                        User user = userService.getUserById(id);
//                        System.out.println(user);
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                    }
//                });
//            }
//        }

    }
}
