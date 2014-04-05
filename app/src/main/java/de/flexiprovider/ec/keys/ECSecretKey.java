package de.flexiprovider.ec.keys;

import java.security.Key;

import javax.crypto.SecretKey;

import de.flexiprovider.common.math.FlexiBigInt;


public class ECSecretKey implements SecretKey, Key {

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
