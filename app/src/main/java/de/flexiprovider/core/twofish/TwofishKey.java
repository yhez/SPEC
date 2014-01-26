/*
 * Copyright (c) 1998-2003 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 */

package de.flexiprovider.core.twofish;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.ByteUtils;

public class TwofishKey implements SecretKey {

    // the key bytes
    private byte[] keyBytes;

    protected TwofishKey(byte[] keyBytes) {
        this.keyBytes = ByteUtils.clone(keyBytes);
    }


    public String getAlgorithm() {
        return "Twofish";
    }

    /**
     * @return a copy of the key bytes
     */
    public byte[] getEncoded() {
        return ByteUtils.clone(keyBytes);
    }

    /**
     * Return the encoding format of this key.
     *
     * @return "RAW"
     */
    public String getFormat() {
        return "RAW";
    }

    /**
     * Compare this key with another object.
     *
     * @param other the other object
     * @return the result of the comparison
     */
    public boolean equals(Object other) {
        return !(other == null || !(other instanceof TwofishKey)) && ByteUtils.equals(keyBytes, ((TwofishKey) other).keyBytes);
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
