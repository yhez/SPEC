package de.flexiprovider.core.mac;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.SecretKey;

import de.flexiprovider.common.util.ByteUtils;


public class HMacKey implements SecretKey, Key {

    private byte[] keyBytes;

    public HMacKey(byte[] keyBytes) {
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
