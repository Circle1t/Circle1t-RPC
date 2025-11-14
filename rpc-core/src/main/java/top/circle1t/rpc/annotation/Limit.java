package top.circle1t.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Limit {
    /**
     * 每秒支持多少请求访问
     */
    double permitsPerSecond();

    /**
     * 拿不到令牌的等待时间
     */
    long timeout();
}
