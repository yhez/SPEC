package de.flexiprovider.core.rsa.interfaces;

import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;

public abstract class RSAPublicKey extends PublicKey implements RSAKey,
        java.security.interfaces.RSAPublicKey {

    public final java.math.BigInteger getModulus() {
        return BigInteger.get(getN().bigInt);
    }


    public final java.math.BigInteger getPublicExponent() {
        return BigInteger.get(getE().bigInt);
    }

    public final String getAlgorithm() {
        return "RSA";
    }


    public abstract FlexiBigInt getE();

}
