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
@AllArgsConstructor
@Getter
public enum SerializeType {
    CUSTOM((byte)0, "custom"),
    KRYO((byte)1, "kryo"),
    HESSIAN((byte)2, "hessian"),
    PROTOSTUFF((byte)3, "protostuff");



    private final byte code;
    private final String description;

    public static SerializeType getSerializeType(byte code) {
        return Arrays.stream(values())
                .filter(serializeType -> serializeType.code == code)
                .findFirst()
                .orElse(CUSTOM);
    }

    public static SerializeType getSerializeType(String description) {
        return Arrays.stream(values())
                .filter(serializeType -> serializeType.description.equals(description))
                .findFirst()
                .orElse(CUSTOM);
    }
}
