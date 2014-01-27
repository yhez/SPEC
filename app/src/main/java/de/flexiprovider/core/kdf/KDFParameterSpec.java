package de.flexiprovider.core.kdf;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.util.ByteUtils;

public class KDFParameterSpec implements AlgorithmParameterSpec {

    // the shared information
    private byte[] sharedInfo;

    /**
     * Constructor. Set the shared information.
     *
     * @param sharedInfo the shared information
     */
    public KDFParameterSpec(byte[] sharedInfo) {
        this.sharedInfo = ByteUtils.clone(sharedInfo);
    }

    /**
     * @return the shared information
     */
    public byte[] getSharedInfo() {
        return ByteUtils.clone(sharedInfo);
    }

}
