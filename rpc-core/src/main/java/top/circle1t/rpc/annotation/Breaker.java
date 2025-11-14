package top.circle1t.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Breaker {
    // 熔断失败阈值 默认达到20次失败就熔断
    int failThreshold() default 20;

    // 半开状态下的成功阈值 默认50%
    double successRateInHalfOpen() default 0.5;

    // 熔断时间窗口 默认10s
    long windowTime() default 10000;
}
