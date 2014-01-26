package de.flexiprovider.core.rc5;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.ByteUtils;

public class RC5Key implements SecretKey {

    // the key bytes
    private byte[] keyBytes;


    protected RC5Key(byte[] keyBytes) {
        this.keyBytes = ByteUtils.clone(keyBytes);
    }

    public String getAlgorithm() {
        return "RC5";
    }

    /**
     * Return the encoding format of the key.
     *
     * @return "RAW"
     */
    public String getFormat() {
        return "RAW";
    }

    /**
     * @return a copy of the key bytes
     */
    public byte[] getEncoded() {
        return ByteUtils.clone(keyBytes);
    }

    /**
     * Compare this key with another object.
     *
     * @param other the other object
     * @return the result of the comparison
     */
    public boolean equals(Object other) {
        return !(other == null || !(other instanceof RC5Key)) && ByteUtils.equals(keyBytes, ((RC5Key) other).keyBytes);
    }

    /**
     * @return the hash code of this key
     */
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < keyBytes.length; i++) {
            result = 31 * result + keyBytes[i];
        }

        return result;
    }

}
