package top.circle1t.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 这个类主要是用来配置服务提供者的信息，比如服务名、服务版本、服务分组等信息
 * [top.circle1t.api.UserService1.0.0common]
 * 1.0.0 - version
 * common - group
 *
 * @author Circle1t
 * @since 2025/10/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcServiceConfig {
    private String version = "";
    private String group = "";
    private Object service;

    public RpcServiceConfig(Object service){
        this.service = service;
    }

    public List<String> getRpcServiceNames(){
        return getInterfaceNames().stream()
                .map(interfaceName -> interfaceName + version + group)
                .collect(Collectors.toList());
    }

    private List<String> getInterfaceNames(){
        return Arrays.stream(service.getClass().getInterfaces())
                .map(Class::getCanonicalName)
                .collect(Collectors.toList());
    }
}
