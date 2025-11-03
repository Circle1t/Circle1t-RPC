package top.circle1t.rpc.loadbalance;

import java.util.List;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public interface LoadBalance {
    public String select(List<String> list);
}
