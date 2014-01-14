package de.flexiprovider.core.desede;

import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.keys.KeySpec;

/**
 * This class specifies a DES-EDE ("triple-DES") key.
 */
public class DESedeKeySpec implements KeySpec {

    /**
     * The length of a DESede key in bytes.
     */
    public static final int DES_EDE_KEY_LEN = 24;

    // ****************************************************
    // JCA adapter
    // ****************************************************

    /**
     * A reference to a {@link javax.crypto.spec.DESedeKeySpec}.
     */
    public javax.crypto.spec.DESedeKeySpec javaKeySpec;


    public DESedeKeySpec(javax.crypto.spec.DESedeKeySpec keySpec) {
        javaKeySpec = keySpec;
    }

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    /**
     * Uses the first 24 bytes in key as the key material for the DESede key.
     * The key bytes are those between key[0] and key[23] inclusive.
     *
     * @param key the buffer with the DESede key material
     * @throws de.flexiprovider.api.exceptions.InvalidKeyException if the given key material is shorter than 24 bytes.
     */
    public DESedeKeySpec(byte[] key) throws InvalidKeyException {
        try {
            javaKeySpec = new javax.crypto.spec.DESedeKeySpec(key);
        } catch (java.security.InvalidKeyException e) {
            throw new InvalidKeyException(e.getMessage());
        }
    }

    /**
     * @return the DESede key material
     */
    public byte[] getKey() {
        return javaKeySpec.getKey();
    }

}
