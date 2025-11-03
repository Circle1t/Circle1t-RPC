package top.circle1t.rpc.dto;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC请求数据传输对象，封装客户端调用服务端的相关信息
 *
 * @author Circle1t
 * @since 2025/10/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求的唯一标识，用于关联请求与响应，确保请求-响应的正确匹配
     */
    private String requestId;

    /**
     * 目标服务接口的全限定名（如：top.circle1t.service.UserService），用于定位服务实现类
     */
    private String interfaceName;

    /**
     * 目标接口中要调用的方法名称（如：getUserById），用于确定具体调用的方法
     */
    private String methodName;

    /**
     * 方法调用时传递的实际参数数组，对应方法的入参值
     */
    private Object[] parameters;

    /**
     * 方法参数的类型数组（如：{String.class, Integer.class}），用于区分重载方法（同方法名不同参数类型）
     */
    private Class<?>[] paramTypes;

    /**
     * 服务版本号，用于区分同一接口的不同实现版本（如多版本迭代时的兼容处理）
     * UserService -> UserServiceImpl1.getUserById(Long id)
     *             -> UserServiceImpl2.getUserById(Long id)
     */
    private String version;

    /**
     * 服务分组，用于对同一接口、同一版本的不同业务实现进行逻辑隔离。
     * 场景：当一个接口（如UserService）在不同业务场景下有差异化实现，且这些实现不属于版本迭代（version解决版本问题），
     * 可通过group区分。例如：
     * - 电商业务的用户服务实现：group = "ecommerce"（处理购物用户信息）
     * - 后台管理的用户服务实现：group = "admin"（处理管理员信息）
     * 调用时指定group，即可精准定位到对应业务的实现类，避免不同业务逻辑的服务互相干扰。
     */
    private String group;

    public String getRpcServiceName(){
        return this.interfaceName
                + StrUtil.blankToDefault(this.version, StrUtil.EMPTY)
                + StrUtil.blankToDefault(this.group,StrUtil.EMPTY);
    }
}