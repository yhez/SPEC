package de.flexiprovider.common.mode;

import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.parameters.AlgorithmParameterGenerator;
import de.flexiprovider.api.parameters.AlgorithmParameters;


public class ModeParameterGenerator extends AlgorithmParameterGenerator {

    // the length of the IV
    private int ivLength;

    // the source of randomness
    private SecureRandom random;

    // flag indicating whether the parameter generator has been initialized
    private boolean initialized;


    protected AlgorithmParameters getAlgorithmParameters() {
        return new ModeParameters();
    }


    public void init(AlgorithmParameterSpec genParams, SecureRandom random)
            throws InvalidAlgorithmParameterException {

        ModeParamGenParameterSpec modeGenParams;
        if (genParams == null) {
            modeGenParams = new ModeParamGenParameterSpec();
        } else if (genParams instanceof ModeParamGenParameterSpec) {
            modeGenParams = (ModeParamGenParameterSpec) genParams;
        } else {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }

        ivLength = modeGenParams.getIVLength();
        this.random = random != null ? random : Registry.getSecureRandom();

        initialized = true;
    }


    public void init(int ivLength, SecureRandom random) {
        ModeParamGenParameterSpec genParams = new ModeParamGenParameterSpec(
                ivLength);
        try {
            init(genParams, random);
        } catch (InvalidAlgorithmParameterException e) {
            // the parameters are correct and must be accepted
            throw new RuntimeException("internal error");
        }
    }

    private void initDefault() {
        ModeParamGenParameterSpec defaultGenParams = new ModeParamGenParameterSpec();
        try {
            init(defaultGenParams, random);
        } catch (InvalidAlgorithmParameterException e) {
            // the parameters are correct and must be accepted
            throw new RuntimeException("internal error");
        }
    }


    public AlgorithmParameterSpec generateParameters() {
        if (!initialized) {
            initDefault();
        }

        byte[] iv = new byte[ivLength];
        random.nextBytes(iv);
        return new ModeParameterSpec(iv);
    }

}
