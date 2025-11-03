package top.circle1t.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Circle1t
 * @since 2025/10/29
 */
@Getter
@ToString
@AllArgsConstructor
public enum RpcResponseStatus {
    SUCCESS(0, "success"),
    FAIL(9999, "fail")
    ;

    private final int code;
    private final String message;

    public static boolean isSuccess(int code) {
        return code == SUCCESS.getCode();
    }
    public static boolean isFail(int code) {
        return code == FAIL.getCode();
    }
}
