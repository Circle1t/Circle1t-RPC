package top.circle1t.api;

import top.circle1t.rpc.annotation.Breaker;
import top.circle1t.rpc.annotation.Retry;

public interface UserService {
    // @Retry
    @Breaker(windowTime = 30000)
    User getUserById(Long id);
}
