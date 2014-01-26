package de.flexiprovider.core.dsa;

import de.flexiprovider.api.keys.KeySpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;

/**
 * This class specifies a DSA private key with its associated parameters.
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see de.flexiprovider.api.keys.KeySpec
 */
public final class DSAPrivateKeySpec extends
        java.security.spec.DSAPrivateKeySpec implements KeySpec {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

    public DSAPrivateKeySpec(java.security.spec.DSAPrivateKeySpec keySpec) {
        super(keySpec.getX(), keySpec.getP(), keySpec.getQ(), keySpec.getG());
    }

    public DSAPrivateKeySpec(FlexiBigInt x, FlexiBigInt p, FlexiBigInt q,
                             FlexiBigInt g) {
        super(BigInteger.get(x.bigInt), BigInteger.get(p.bigInt), BigInteger.get(q.bigInt), BigInteger.get(g.bigInt));
    }

    /**
     * @return the private key <tt>x</tt>.
     */
    public FlexiBigInt getValueX() {
        return new FlexiBigInt(new BigInteger(getX()));
    }

    /**
     * @return the prime <tt>p</tt>
     */
    public FlexiBigInt getPrimeP() {
        return new FlexiBigInt(new BigInteger(getP()));
    }

    /**
     * @return the sub-prime <tt>q</tt>
     */
    public FlexiBigInt getPrimeQ() {
        return new FlexiBigInt(new BigInteger(getQ()));
    }

    /**
     * @return the base <tt>g</tt>
     */
    public FlexiBigInt getBaseG() {
        return new FlexiBigInt(new BigInteger(getG()));
    }

}
