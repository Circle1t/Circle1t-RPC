package top.circle1t.server.service;

import cn.hutool.core.util.IdUtil;
import top.circle1t.api.User;
import top.circle1t.api.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Long id) {
        return User.builder()
                .id(id)
                .name("张三")
                .build();
    }
}
