package de.flexiprovider.ec;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import de.flexiprovider.common.exceptions.InvalidPointException;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.ScalarMult;
import de.flexiprovider.ec.keys.ECPrivateKey;
import de.flexiprovider.ec.keys.ECPublicKey;
import de.flexiprovider.ec.keys.ECSecretKey;


public class ECSVDPDHC {

    /**
     * flag indicating whether cofactor multiplication shall be used
     */
    protected boolean withCoFactor = true;
    // the private key value
    private BigInteger mS;
    // the public key
    private ECPublicKey mOtherKey;
    // the (optional) cofactor
    private BigInteger mK;

    public void init(PrivateKey key) throws InvalidKeyException {
        if (!(key instanceof ECPrivateKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        ECPrivateKey ecPrivKey = (ECPrivateKey) key;

        mS = ecPrivKey.getS();
        mK = BigInteger.valueOf(ecPrivKey.getParams().getK());
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
