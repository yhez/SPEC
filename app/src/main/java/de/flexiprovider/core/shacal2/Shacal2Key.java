/*
 * Copyright (c) 1998-2007 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 */

package de.flexiprovider.core.shacal2;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.ByteUtils;

public class Shacal2Key implements SecretKey {

    // the key bytes
    private byte[] keyBytes;

    protected Shacal2Key(byte[] keyBytes) {
        this.keyBytes = ByteUtils.clone(keyBytes);
    }

    public String getAlgorithm() {
        return "Shacal2";
    }

    public byte[] getEncoded() {
        return ByteUtils.clone(keyBytes);
    }

    public String getFormat() {
        return "RAW";
    }

    public boolean equals(Object other) {
        return !(other == null || !(other instanceof Shacal2Key)) && ByteUtils.equals(keyBytes, ((Shacal2Key) other).keyBytes);
    }

    public int hashCode() {
        int result = 1;
        for (int i = 0; i < keyBytes.length; i++) {
            result = 31 * result + keyBytes[i];
        }

        return result;
    }

}
