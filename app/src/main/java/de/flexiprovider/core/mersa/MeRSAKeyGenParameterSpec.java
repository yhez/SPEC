package de.flexiprovider.core.mersa;

import de.flexiprovider.core.rsa.RSAKeyGenParameterSpec;

/**
 * This class specifies parameters used by the {@link MeRSAKeyPairGenerator}.
 *
 * @author Paul Nguentcheu
 * @author Martin D�ring
 */
public class MeRSAKeyGenParameterSpec extends RSAKeyGenParameterSpec {

    /**
     * The default exponent of the prime <tt>p</tt>
     */
    public static final int DEFAULT_EXPONENT_K = 3;

    // the exponent of the prime p
    private int k;


    public MeRSAKeyGenParameterSpec() {
        k = DEFAULT_EXPONENT_K;
    }


    public MeRSAKeyGenParameterSpec(int keySize) {
        super(keySize);
        k = DEFAULT_EXPONENT_K;
    }

    /**
     * @return the exponent of the prime <tt>p</tt>
     */
    public int getExponentK() {
        return k;
    }

}
