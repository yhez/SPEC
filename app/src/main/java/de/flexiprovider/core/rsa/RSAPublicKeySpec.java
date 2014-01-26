package de.flexiprovider.core.rsa;

import de.flexiprovider.api.keys.KeySpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;

public final class RSAPublicKeySpec extends java.security.spec.RSAPublicKeySpec
        implements KeySpec {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************


    public RSAPublicKeySpec(java.security.spec.RSAPublicKeySpec keySpec) {
        super(keySpec.getModulus(), keySpec.getPublicExponent());
    }

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    public RSAPublicKeySpec(FlexiBigInt n, FlexiBigInt e) {
        super(BigInteger.get(n.bigInt), BigInteger.get(e.bigInt));
    }

    /**
     * @return the modulus n
     */
    public FlexiBigInt getN() {
        return new FlexiBigInt(new BigInteger(getModulus()));
    }

    /**
     * @return the public exponent e
     */
    public FlexiBigInt getE() {
        return new FlexiBigInt(new BigInteger(getPublicExponent()));
    }

}
