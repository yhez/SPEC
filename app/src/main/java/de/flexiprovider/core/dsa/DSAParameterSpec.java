package de.flexiprovider.core.dsa;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.core.dsa.interfaces.DSAParams;
import my.BigInteger;

/**
 * This class specifies the set of parameters used with the DSA algorithm.
 *
 * @see de.flexiprovider.api.parameters.AlgorithmParameterSpec
 */

public final class DSAParameterSpec extends java.security.spec.DSAParameterSpec
        implements DSAParams, AlgorithmParameterSpec {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

    /**
     * Create a new DSAParameterSpec with the specified parameter values.
     *
     * @param p the prime
     * @param q the subprime
     * @param g the base
     */
    public DSAParameterSpec(BigInteger p, BigInteger q, BigInteger g) {
        super(BigInteger.get(p), BigInteger.get(q), BigInteger.get(g));
    }

    /**
     * Create a new DSAParameterSpec out of the given DSAParams.
     *
     * @param dsaParams the DSAParams object
     */
    public DSAParameterSpec(java.security.interfaces.DSAParams dsaParams) {
        super(dsaParams.getP(), dsaParams.getQ(), dsaParams.getG());
    }

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    /**
     * Create a new DSAParameterSpec with the specified parameter values.
     *
     * @param p the prime
     * @param q the subprime
     * @param g the base
     */
    public DSAParameterSpec(FlexiBigInt p, FlexiBigInt q, FlexiBigInt g) {
        super(BigInteger.get(p.bigInt), BigInteger.get(q.bigInt), BigInteger.get(g.bigInt));
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
