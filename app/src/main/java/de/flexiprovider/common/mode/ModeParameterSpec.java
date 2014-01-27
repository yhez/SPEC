package de.flexiprovider.common.mode;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;


public class ModeParameterSpec extends javax.crypto.spec.IvParameterSpec
        implements AlgorithmParameterSpec {
    public ModeParameterSpec(javax.crypto.spec.IvParameterSpec params) {
        super(params.getIV());
    }




    public ModeParameterSpec(byte[] iv) {
        super(iv);
    }


    public ModeParameterSpec(byte[] iv, int offset, int length) {
        super(iv, offset, length);
    }

}
