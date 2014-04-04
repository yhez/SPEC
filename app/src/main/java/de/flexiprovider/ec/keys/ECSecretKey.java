package de.flexiprovider.ec.keys;

import de.flexiprovider.common.math.FlexiBigInt;


public class ECSecretKey implements javax.crypto.SecretKey, java.security.Key {

    // the private key s, 1 < s < r.
    private FlexiBigInt mS;


    public ECSecretKey(FlexiBigInt s) {
        mS = s;
    }


    public String getAlgorithm() {
        return "EC";
    }


    public byte[] getEncoded() {
        return mS.toByteArray();
    }


    public String getFormat() {
        return "RAW";
    }


    public FlexiBigInt getS() {
        return mS;
    }

    public boolean equals(Object other) {
        return other instanceof ECSecretKey && mS.equals(((ECSecretKey) other).mS);

    }

    public int hashCode() {
        return mS.hashCode();
    }

}
