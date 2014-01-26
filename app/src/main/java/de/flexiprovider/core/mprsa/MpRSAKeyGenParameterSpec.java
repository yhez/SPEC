package de.flexiprovider.core.mprsa;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.core.rsa.RSAKeyGenParameterSpec;

/**
 * This class specifies parameters used by the {@link MpRSAKeyPairGenerator}.
 *
 * @author Paul Nguentcheu
 * @author Martin D�ring
 */
public class MpRSAKeyGenParameterSpec extends RSAKeyGenParameterSpec implements
        AlgorithmParameterSpec {

    /**
     * The default number of primes
     */
    public static final int DEFAULT_NUM_PRIMES = 3;

    // the number of primes
    private int k;

    public MpRSAKeyGenParameterSpec() {
        k = DEFAULT_NUM_PRIMES;
    }

    /**
     * @return the number of primes
     */
    public int getNumPrimes() {
        return k;
    }

}
