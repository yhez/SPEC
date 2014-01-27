package de.flexiprovider.core.rijndael;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.ByteUtils;


public class RijndaelKey implements SecretKey {

    // the key bytes
    private byte[] keyBytes;


    protected RijndaelKey(byte[] keyBytes) {
        this.keyBytes = ByteUtils.clone(keyBytes);
    }


    public String getAlgorithm() {
        return "Rijndael";
    }


    public String getFormat() {
        return "RAW";
    }


    public byte[] getEncoded() {
        return ByteUtils.clone(keyBytes);
    }


    public boolean equals(Object other) {
        return !(other == null || !(other instanceof RijndaelKey)) && ByteUtils.equals(keyBytes, ((RijndaelKey) other).keyBytes);
    }


    public int hashCode() {
        int result = 1;
        for (int i = 0; i < keyBytes.length; i++) {
            result = 31 * result + keyBytes[i];
        }

        return result;
    }

}
