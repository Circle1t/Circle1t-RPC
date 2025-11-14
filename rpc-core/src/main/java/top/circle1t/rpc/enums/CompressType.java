package top.circle1t.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
@ToString
@Getter
@AllArgsConstructor
public enum CompressType {
    GZIP((byte)1, "GZIP");

    private final byte code;
    private final String description;

    public static CompressType getCompressType(byte code) {
        return Arrays.stream(values())
                .filter(compressType -> compressType.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("code异常：" + code));
    }
}
