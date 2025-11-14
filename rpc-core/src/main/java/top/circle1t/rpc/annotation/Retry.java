package top.circle1t.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retry {
    // 注解成员类型  成员名() default 默认值;
    Class<? extends Throwable> value() default Exception.class;

    int maxAttempts() default 3;

    long delay() default 0;
}
