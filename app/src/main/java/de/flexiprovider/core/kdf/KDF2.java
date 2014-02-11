package de.flexiprovider.core.kdf;

import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.KeyDerivation;
import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.common.util.BigEndianConversions;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.core.md.SHA1;


public class KDF2 extends KeyDerivation {

    // the hash function
    private MessageDigest md;

    // the secret key
    private byte[] z;

    // a shared info string
    private byte[] sharedInfo;


    public KDF2() {
        md = new SHA1();
    }


    public void init(byte[] secret, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (secret == null) {
            throw new InvalidKeyException("null");
        }
        z = ByteUtils.clone(secret);

        if (params != null) {
            if (!(params instanceof KDFParameterSpec)) {
                throw new InvalidAlgorithmParameterException("unsupported type");
            }
            sharedInfo = ((KDFParameterSpec) params).getSharedInfo();
        }
    }


    public byte[] deriveKey(int keySize) {

        if (keySize < 0) {
            return null;
        }

        int mdLength = md.getDigestLength();
        int d = keySize / mdLength;
        int t = keySize % mdLength;

        byte[] result = new byte[keySize];
        int ctr = 1;
        try {
            for (int i = 0; i < d; i++, ctr++) {
                byte[] ctrBytes = BigEndianConversions.I2OSP(ctr);
                md.update(z);
                md.update(ctrBytes);
                md.update(sharedInfo);
                md.digest(result, i * mdLength, mdLength);
            }
        } catch (DigestException e) {
            // must not happen
            throw new RuntimeException("internal error");
        }

        if (t != 0) {
            // derive remaining key bytes
            byte[] ctrBytes = BigEndianConversions.I2OSP(ctr);
            md.update(z);
            md.update(ctrBytes);
            md.update(sharedInfo);
            byte[] last = md.digest();
            System.arraycopy(last, 0, result, d * mdLength, t);
        }

        // return the derived key
        return result;
    }

}
