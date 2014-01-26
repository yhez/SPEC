package de.flexiprovider.core.pbe;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;

/**
 * This class provides a specification for the parameters used by the PBKDF1 key
 * derivation function specified in <a
 * href="http://www.rsa.com/rsalabs/node.asp?id=2127">PKCS #5 v2.0</a>.
 */
public class PBEParameterSpec extends javax.crypto.spec.PBEParameterSpec
        implements AlgorithmParameterSpec {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    /**
     * Construct new PBE parameters using the given salt and iteration count.
     *
     * @param salt           the salt
     * @param iterationCount the iteration count
     */
    public PBEParameterSpec(byte[] salt, int iterationCount) {
        super(salt, iterationCount);
    }

}
