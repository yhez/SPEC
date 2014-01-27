package de.flexiprovider.core.desede;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.ByteUtils;

public class DESedeKey implements SecretKey {
    private byte[] keyBytes;
    protected DESedeKey(byte[] keyBytes) {
        this.keyBytes = ByteUtils.clone(keyBytes);
    }
    public String getAlgorithm() {
        return DESede.ALG_NAME;
    }
    public byte[] getEncoded() {
        return ByteUtils.clone(keyBytes);
    }
    public String getFormat() {
        return "RAW";
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof DESedeKey)) {
            return false;
        }
        DESedeKey otherKey = (DESedeKey) other;
        return ByteUtils.equals(keyBytes, otherKey.keyBytes);
    }
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < keyBytes.length; i++) {
            result = 31 * result + keyBytes[i];
        }
        return result;
    }
}
