/*
 * Copyright (c) 1998-2003 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 */

package de.flexiprovider.core.pbe;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.pki.AlgorithmIdentifier;

/**
 * This is the parameter specification for the
 * {@link de.flexiprovider.core.pbe.PBES2 passphrase based encryption scheme 2}.
 *
 * @author Thomas Wahrenbruch
 */
public class PBES2ParameterSpec implements AlgorithmParameterSpec {

    /**
     * the AlgorithmIdentifier of the key derivation function
     */
    private AlgorithmIdentifier keyDerivationFunction;

    /**
     * the AlgorithmIdentifier of the encryption scheme
     */
    private AlgorithmIdentifier encryptionScheme;

    /**
     * Construct a new PBE2ParameterSpec object.
     *
     * @param keyDerivationFunction the key derivation function
     * @param encryptionScheme      the encryption scheme
     */
    protected PBES2ParameterSpec(AlgorithmIdentifier keyDerivationFunction,
                                 AlgorithmIdentifier encryptionScheme) {

        this.keyDerivationFunction = keyDerivationFunction;
        this.encryptionScheme = encryptionScheme;
    }

    /**
     * @return the AlgorithmIdentifier of the encryption scheme
     */
    public AlgorithmIdentifier getEncryptionScheme() {
        return encryptionScheme;
    }

    /**
     * @return the AlgorithmIdentifier of key derivation function.
     */
    public AlgorithmIdentifier getKeyDerivationFunction() {
        return keyDerivationFunction;
    }

}
