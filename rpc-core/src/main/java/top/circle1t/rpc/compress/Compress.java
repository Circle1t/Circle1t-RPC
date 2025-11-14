package top.circle1t.rpc.compress;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
public interface Compress {
    byte[] compress(byte[] bytes);
    byte[] decompress(byte[] bytes);
}
