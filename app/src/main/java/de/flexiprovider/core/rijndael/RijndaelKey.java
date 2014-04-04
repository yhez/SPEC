package de.flexiprovider.core.rijndael;

import de.flexiprovider.common.util.ByteUtils;


public class RijndaelKey implements javax.crypto.SecretKey, java.security.Key {

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
        for (byte keyByte : keyBytes) {
            result = 31 * result + keyByte;
        }

        return result;
    }

}
