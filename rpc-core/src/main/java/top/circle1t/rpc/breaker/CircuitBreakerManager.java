package top.circle1t.rpc.breaker;

import top.circle1t.rpc.annotation.Breaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Circle1t
 * @since 2025/11/08
 */
public class CircuitBreakerManager {
    private static final Map<String, CircuitBreaker> BREAKER_MAP = new ConcurrentHashMap<>();

    public static CircuitBreaker getCircuitBreaker(String key, Breaker breaker) {
        return BREAKER_MAP.computeIfAbsent(key, k -> new CircuitBreaker(breaker.failThreshold(), breaker.successRateInHalfOpen(), breaker.windowTime()));
    }
}
