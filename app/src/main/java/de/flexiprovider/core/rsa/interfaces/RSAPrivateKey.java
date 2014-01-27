package de.flexiprovider.core.rsa.interfaces;

import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;

public abstract class RSAPrivateKey extends PrivateKey implements RSAKey,
        java.security.interfaces.RSAPrivateKey {




    public final java.math.BigInteger getModulus() {
        return BigInteger.get(getN().bigInt);
    }


    public final java.math.BigInteger getPrivateExponent() {
        return BigInteger.get(getD().bigInt);
    }




    public final String getAlgorithm() {
        return "RSA";
    }


    public abstract FlexiBigInt getD();

}
