package de.flexiprovider.core.kdf;

import de.flexiprovider.api.KeyDerivation;
import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.core.md.SHA1;

public class KDF1 extends KeyDerivation {

    // the hash function
    private MessageDigest md;

    // the secret key
    private byte[] z;

    // a shared info string
    private byte[] sharedInfo;

    /**
     * Constructor. Set the message digest.
     */
    public KDF1() {
        md = new SHA1();
    }

    /**
     * Initialize this KDF with a secret and parameters. The parameters have to
     * be an instance of {@link KDFParameterSpec}.
     *
     * @param secret the secret from which to derive the key
     * @param params the parameters
     * @throws de.flexiprovider.api.exceptions.InvalidKeyException                if the secret is <tt>null</tt>.
     * @throws de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException if the parameters are not an instance of
     *                                                                            {@link KDFParameterSpec} or the shared information stored
     *                                                                            in the parameters is <tt>null</tt>.
     */
    public void init(byte[] secret, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        if (secret == null) {
            throw new InvalidKeyException("null");
        }
        z = ByteUtils.clone(secret);

        if (!(params instanceof KDFParameterSpec)) {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }
        sharedInfo = ((KDFParameterSpec) params).getSharedInfo();
        if (sharedInfo == null) {
            throw new InvalidAlgorithmParameterException(
                    "shared information must not be null");
        }
    }

    /**
     * Start the derivation process and return the derived key. KDF1 uses the
     * shared key value <tt>z</tt> and the <tt>shared info</tt> to derive
     * the key <tt>hash(z || shared-info)</tt>. Thus, the derived key will
     * always have the same length as the hash function output and the given key
     * data length is ignored.
     *
     * @param keySize the desired length of the derived key (not used)
     * @return the derived key
     */
    public byte[] deriveKey(int keySize) {
        byte[] both = ByteUtils.concatenate(z, sharedInfo);
        return md.digest(both);
    }

}
