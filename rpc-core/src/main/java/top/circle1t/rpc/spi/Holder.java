package top.circle1t.rpc.spi;

/**
 * @author Circle1t
 * @since 2025/11/09
 */
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
