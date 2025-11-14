package top.circle1t.rpc.breaker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Circle1t
 * @since 2025/11/08
 */
public class CircuitBreaker {
    private State state = State.CLOSED;
    // 统计失败调用次数
    private final AtomicInteger failCount = new AtomicInteger(0);
    // 统计成功调用次数
    private final AtomicInteger successCount = new AtomicInteger(0);
    // 统计总调用次数
    private final AtomicInteger totalCount = new AtomicInteger(0);
    // 熔断器失败阈值
    private final int failureThreshold;
    // 半开状态下的成功阈值
    private final double successRateInHalfOpen;
    // 熔断时间窗口
    private final long windowTime;
    private long lastFailureTime;

    public CircuitBreaker(int failureThreshold, double successRateInHalfOpen, long windowTime) {
        this.failureThreshold = failureThreshold;
        this.successRateInHalfOpen = successRateInHalfOpen;
        this.windowTime = windowTime;
    }

    public synchronized boolean canRequest() {
        switch (state) {
            case CLOSED:
                return true;
            case OPEN:
                if (System.currentTimeMillis() - lastFailureTime > windowTime) {
                    state = State.HALF_OPEN;
                    resetCount();
                    return true;
                }
                return false;
            case HALF_OPEN:
                totalCount.incrementAndGet();
                return true;
            default:
                throw new IllegalArgumentException("熔断器状态异常");
        }
    }

    public synchronized void success() {
        if(state != State.HALF_OPEN){
            resetCount();
            return;
        }

        successCount.incrementAndGet();
        if(successCount.get() >= successRateInHalfOpen * totalCount.get()){
            state = State.CLOSED;
            resetCount();
        }
    }

    public synchronized void fail() {
        failCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();

        if(state == State.HALF_OPEN){
            state = State.OPEN;
            return;
        }

        if(failCount.get() >= failureThreshold){
            state = State.OPEN;
        }
    }

    private void resetCount(){
        failCount.set(0);
        successCount.set(0);
        totalCount.set(0);
    }

    enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
}
