package de.flexiprovider.common.mode;


public class ModeParameterSpec extends javax.crypto.spec.IvParameterSpec
        implements java.security.spec.AlgorithmParameterSpec {
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
