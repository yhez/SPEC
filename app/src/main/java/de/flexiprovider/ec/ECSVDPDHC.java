package de.flexiprovider.ec;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import de.flexiprovider.api.KeyAgreement;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.common.exceptions.InvalidPointException;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.ScalarMult;
import de.flexiprovider.ec.keys.ECPrivateKey;
import de.flexiprovider.ec.keys.ECPublicKey;
import de.flexiprovider.ec.keys.ECSecretKey;


public class ECSVDPDHC extends KeyAgreement {

    /**
     * flag indicating whether cofactor multiplication shall be used
     */
    protected boolean withCoFactor = true;
    // the private key value
    private FlexiBigInt mS;
    // the public key
    private ECPublicKey mOtherKey;
    // the (optional) cofactor
    private FlexiBigInt mK;


    public void init(PrivateKey key, AlgorithmParameterSpec params,
                     SecureRandom random) throws InvalidKeyException {
        if (!(key instanceof ECPrivateKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        ECPrivateKey ecPrivKey = (ECPrivateKey) key;

        mS = ecPrivKey.getS();
        mK = FlexiBigInt.valueOf(ecPrivKey.getParams().getK());
    }


    public SecretKey generateSecret(String algorithm)
            throws NoSuchAlgorithmException {
        SecretKey secr;
        if (!(algorithm.equals("ECDH"))) {
            throw new NoSuchAlgorithmException(algorithm + " is not supported");
        }

        try {
            secr = secretGenerator();
        } catch (InvalidKeyException ex) {
            throw new RuntimeException("Can't generate shared secret: "
                    + ex.getMessage());
        }

        return secr;
    }


    public int generateSecret(byte[] sharedSecret, int offset)
            throws ShortBufferException {
        ECSecretKey secr;
        try {
            secr = secretGenerator();
        } catch (InvalidKeyException ex) {
            throw new RuntimeException("Can't generate shared secret: "
                    + ex.getMessage());
        }
        byte[] sByte = secr.getS().toByteArray();
        int n = sByte.length;
        try {
            System.arraycopy(sByte, 0, sharedSecret, offset, n);
        } catch (IndexOutOfBoundsException ex) {
            throw new ShortBufferException(
                    "Byte array sharedSecret too small for shared secret.");
        }

        return n;
    }

    public byte[] generateSecret() {
        ECSecretKey secr;

        try {
            secr = secretGenerator();
        } catch (InvalidKeyException ex) {
            throw new RuntimeException("Can't generate shared secret: "
                    + ex.getMessage());
        }

        return secr.getS().toByteArray();
    }

    public Key doPhase(PublicKey key, boolean lastPhase)
            throws InvalidKeyException {

        if (!(key instanceof ECPublicKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        mOtherKey = (ECPublicKey) key;

        Point q = mOtherKey.getW();
        if (q.isZero() || !q.onCurve()) {
            throw new InvalidKeyException("invalid public key");
        }
        try {
            if (lastPhase) {
                return generateSecret("ECDH");
            }
            return null;
        } catch (NoSuchAlgorithmException ex) {
            // the requested type is correct
            throw new RuntimeException("internal error");
        }
    }

    private ECSecretKey secretGenerator() throws InvalidKeyException {
        // obtain the public key value
        Point q = mOtherKey.getW();

        // scalar multiplication with private key value
        q = ScalarMult.multiply(mS, q);

        // optional cofactor multiplication
        if (withCoFactor) {
            q = ScalarMult.multiply(mK, q);
        }

        if (q.isZero()) {
            throw new InvalidPointException("shared secret is invalid");
        }

        // return the x-coordinate of the computed point as EC secret key
        return new ECSecretKey(q.getXAffin().toFlexiBigInt());
    }

}
