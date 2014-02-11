package de.flexiprovider.api.parameters;

import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.common.mode.ModeParameterSpec;
import de.flexiprovider.common.util.JavaSecureRandomWrapper;

public abstract class AlgorithmParameterGenerator extends
        java.security.AlgorithmParameterGeneratorSpi {


    protected final java.security.AlgorithmParameters engineGenerateParameters() {

        final class JCAAlgorithmParameters extends
                java.security.AlgorithmParameters {
            private JCAAlgorithmParameters(AlgorithmParameters params) {
                super(params, null, null);
            }
        }

        JCAAlgorithmParameters algParams = new JCAAlgorithmParameters(
                getAlgorithmParameters());

        try {
            algParams.init(generateParameters());
        } catch (java.security.spec.InvalidParameterSpecException ipse) {
            throw new RuntimeException("InvalidParameterSpecException: "
                    + ipse.getMessage());
        }

        return algParams;
    }

    protected final void engineInit(int keySize,
                                    java.security.SecureRandom javaRand) {
        SecureRandom flexiRand = new JavaSecureRandomWrapper(javaRand);
        init(keySize, flexiRand);
    }

    protected final void engineInit(
            java.security.spec.AlgorithmParameterSpec genParamSpec,
            java.security.SecureRandom javaRand)
            throws java.security.InvalidAlgorithmParameterException {
        SecureRandom flexiRand = new JavaSecureRandomWrapper(javaRand);
        ModeParameterSpec paramSpec;
        if (genParamSpec instanceof javax.crypto.spec.IvParameterSpec) {
            paramSpec = new ModeParameterSpec(
                    (javax.crypto.spec.IvParameterSpec) genParamSpec);
            init(paramSpec, flexiRand);
        } else {
            if (!(genParamSpec instanceof AlgorithmParameterSpec)) {
                throw new java.security.InvalidAlgorithmParameterException();
            }
            init(genParamSpec, flexiRand);
        }
    }


    protected abstract AlgorithmParameters getAlgorithmParameters();


    public abstract void init(int keySize, SecureRandom random);


    public abstract void init(AlgorithmParameterSpec genParamSpec,
                              SecureRandom random) throws InvalidAlgorithmParameterException;

    public abstract AlgorithmParameterSpec generateParameters();

}
