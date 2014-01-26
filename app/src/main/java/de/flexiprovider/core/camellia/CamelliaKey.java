package de.flexiprovider.core.camellia;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.ByteUtils;

public class CamelliaKey implements SecretKey {

    /**
     * Key data
     */
    private byte[] keyBytes;

    /**
     * Construct new instance of <tt>CamelliaKey</tt> from an array of key
     * bytes.
     *
     * @param keyBytes the key bytes
     */
    protected CamelliaKey(byte[] keyBytes) {
        this.keyBytes = ByteUtils.clone(keyBytes);
    }

    /**
     * Obtain the name of the algorithm this key can be used for.
     *
     * @return name of the algorithm the key can be used for as a
     * <tt>String</tt>
     */
    public String getAlgorithm() {
        return "Camellia";
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        return ByteUtils.clone(keyBytes);
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof CamelliaKey)) {
            return false;
        }
        CamelliaKey otherKey = (CamelliaKey) other;
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
