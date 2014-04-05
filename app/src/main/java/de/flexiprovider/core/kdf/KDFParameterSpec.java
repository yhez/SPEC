package de.flexiprovider.core.kdf;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.common.util.ByteUtils;

public class KDFParameterSpec implements AlgorithmParameterSpec {

    // the shared information
    private byte[] sharedInfo;

    public KDFParameterSpec(byte[] sharedInfo) {
        this.sharedInfo = ByteUtils.clone(sharedInfo);
    }

    public byte[] getSharedInfo() {
        return ByteUtils.clone(sharedInfo);
    }

}
