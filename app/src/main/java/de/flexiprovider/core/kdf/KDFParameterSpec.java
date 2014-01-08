package de.flexiprovider.core.kdf;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.util.ByteUtils;

/**
 * This class specifies parameters used by the {@link de.flexiprovider.core.kdf.KDF1}, {@link de.flexiprovider.core.kdf.KDF2}, and
 * {@link de.flexiprovider.core.kdf.X963} key derivation functions. The parameters consist of a byte array
 * containing shared information.
 *
 * @author Martin D�ring
 */
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
