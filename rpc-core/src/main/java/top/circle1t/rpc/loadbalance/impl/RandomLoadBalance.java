package top.circle1t.rpc.loadbalance.impl;

import cn.hutool.core.util.RandomUtil;
import top.circle1t.rpc.loadbalance.LoadBalance;

import java.util.List;

/**
 * @author Circle1t
 * @since 2025/11/01
 */
public class RandomLoadBalance implements LoadBalance {
    @Override
    public String select(List<String> list) {
        return RandomUtil.randomEle(list);
    }
}
