package top.circle1t.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Circle1t
 * @since 2025/11/09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcConfig {
    private String serializer = "kryo";

}

