package de.flexiprovider.core.rsa;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;


public class RSAKeyGenParameterSpec extends
        java.security.spec.RSAKeyGenParameterSpec implements
        java.security.spec.AlgorithmParameterSpec {

    public static final int DEFAULT_KEY_SIZE = 1024;

    public static final FlexiBigInt DEFAULT_EXPONENT = new FlexiBigInt(new BigInteger(F4));

    public RSAKeyGenParameterSpec(int keySize, FlexiBigInt e) {
        super(keySize, BigInteger.get(e.bigInt));
    }

    public int getKeySize() {
        int keySize = getKeysize();
        // check is key size is too small
        if (keySize < 512) {
            // in this case, return the default key size
            return DEFAULT_KEY_SIZE;
        }

        // else return the stored key size
        return keySize;
    }
    public FlexiBigInt getE() {
        FlexiBigInt e = new FlexiBigInt(new BigInteger(getPublicExponent()));
        // check if e is an even number, which is not allowed
        if (!e.testBit(0)) {
            // in this case, return the default exponent
            return DEFAULT_EXPONENT;
        }

        // else return the stored exponent
        return e;
    }

}
