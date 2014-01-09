package de.flexiprovider.core.mac;

import java.util.Arrays;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.ByteUtils;


public class HMacKey implements SecretKey {

    private byte[] keyBytes;

    protected HMacKey(byte[] keyBytes) {
        this.keyBytes = ByteUtils.clone(keyBytes);
    }

    public String getAlgorithm() {
        return "Hmac";
    }

    public byte[] getEncoded() {
        return ByteUtils.clone(keyBytes);
    }

    public String getFormat() {
        return "RAW";
    }

    public boolean equals(Object other) {
        return !(other == null || !(other instanceof HMacKey)) && ByteUtils.equals(keyBytes, ((HMacKey) other).keyBytes);
    }

    public int hashCode() {
        return Arrays.hashCode(keyBytes);
    }

}
