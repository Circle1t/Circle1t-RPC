package top.circle1t.rpc.compress.impl;

import cn.hutool.core.util.ZipUtil;
import top.circle1t.rpc.compress.Compress;

import java.util.Objects;

/**
 * @author Circle1t
 * @since 2025/11/06
 */
public class GzipCompress implements Compress {
    @Override
    public byte[] compress(byte[] bytes) {
        if(Objects.isNull(bytes) || bytes.length == 0){
            return bytes;
        }

        return ZipUtil.gzip(bytes);
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if(Objects.isNull(bytes) || bytes.length == 0){
            return bytes;
        }

        return ZipUtil.unGzip(bytes);
    }
}
