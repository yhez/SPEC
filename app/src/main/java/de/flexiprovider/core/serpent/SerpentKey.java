package de.flexiprovider.core.serpent;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.ByteUtils;

public class SerpentKey implements SecretKey {

    private byte[] keyBytes;

    protected SerpentKey(byte[] keyBytes) {
        this.keyBytes = ByteUtils.clone(keyBytes);
    }

    public String getAlgorithm() {
        return "Serpent";
    }

    public byte[] getEncoded() {
        return ByteUtils.clone(keyBytes);
    }

    public String getFormat() {
        return "RAW";
    }
    public boolean equals(Object other) {
        return !(other == null || !(other instanceof SerpentKey)) && ByteUtils.equals(keyBytes, ((SerpentKey) other).keyBytes);
    }

    public int hashCode() {
        int result = 1;
        for (int i = 0; i < keyBytes.length; i++) {
            result = 31 * result + keyBytes[i];
        }

        return result;
    }

}
