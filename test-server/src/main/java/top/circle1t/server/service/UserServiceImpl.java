package top.circle1t.server.service;

import cn.hutool.core.util.IdUtil;
import top.circle1t.api.User;
import top.circle1t.api.UserService;
import top.circle1t.rpc.annotation.Limit;

public class UserServiceImpl implements UserService {
    //@Limit(permitsPerSecond = 5, timeout = 0)
    @Override
    public User getUserById(Long id) {
        if(id < 0){
            throw new RuntimeException("id不能小于0");
        }
        return User.builder()
                .id(id)
                .name("张三")
                .build();
    }
}
