package top.circle1t.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
@ToString
@AllArgsConstructor
@Getter
public enum VersionType {
    VERSION_1((byte) 1, "version 1");

    private final byte code;
    private final String description;

    public static VersionType getVersionType(byte code) {
        return Arrays.stream(values())
                .filter(versionType -> versionType.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("code异常：" + code));
    }
}
