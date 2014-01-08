package de.flexiprovider.core.rsa;

import de.flexiprovider.common.math.FlexiBigInt;
import my.BigInteger;

public class RSAPrivateKeySpec extends java.security.spec.RSAPrivateKeySpec
        implements RSAPrivKeySpecInterface {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

    public RSAPrivateKeySpec(BigInteger n, BigInteger d) {
        super(BigInteger.get(n), BigInteger.get(d));
    }

    /**
     * Create a new RSAPrivateKeySpec out of the given
     * {@link java.security.spec.RSAPrivateKeySpec}.
     *
     * @param keySpec the {@link java.security.spec.RSAPrivateKeySpec}
     */
    public RSAPrivateKeySpec(java.security.spec.RSAPrivateKeySpec keySpec) {
        super(keySpec.getModulus(), keySpec.getPrivateExponent());
    }

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    public RSAPrivateKeySpec(FlexiBigInt n, FlexiBigInt d) {
        super(BigInteger.get(n.bigInt), BigInteger.get(d.bigInt));
    }

    /**
     * @return the modulus n
     */
    public FlexiBigInt getN() {
        return new FlexiBigInt(new BigInteger(getModulus()));
    }

    /**
     * @return the private exponent d
     */
    public FlexiBigInt getD() {
        return new FlexiBigInt(new BigInteger(getPrivateExponent()));
    }

}
