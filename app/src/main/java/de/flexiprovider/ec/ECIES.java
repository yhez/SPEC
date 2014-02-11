package de.flexiprovider.ec;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import de.flexiprovider.api.KeyAgreement;
import de.flexiprovider.api.keys.Key;
import de.flexiprovider.api.keys.KeyFactory;
import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.api.keys.KeyPairGenerator;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.ies.IES;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.ec.keys.ECKeyFactory;
import de.flexiprovider.ec.keys.ECKeyPairGenerator;
import de.flexiprovider.ec.keys.ECPrivateKey;
import de.flexiprovider.ec.keys.ECPublicKey;
import de.flexiprovider.ec.keys.ECPublicKeySpec;
import de.flexiprovider.ec.parameters.CurveParams;


public class ECIES extends IES {


    public String getName() {
        return "ECIES";
    }


    public int getKeySize(Key key) throws InvalidKeyException {
        if (key instanceof ECPrivateKey) {
            return ((ECPrivateKey) key).getParams().getQ().bitLength();
        }
        if (key instanceof ECPublicKey) {
            return ((ECPublicKey) key).getParams().getQ().bitLength();
        }
        throw new InvalidKeyException("unsupported type");
    }
    protected PublicKey checkPubKey(Key key) throws InvalidKeyException {
        // check key
        if (!(key instanceof ECPublicKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        ECPublicKey ecPubKey = (ECPublicKey) key;
        keyParams = ecPubKey.getParams();

        return ecPubKey;
    }

    protected PrivateKey checkPrivKey(Key key) throws InvalidKeyException {
        // check key
        if (!(key instanceof ECPrivateKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        ECPrivateKey ecPrivKey = (ECPrivateKey) key;
        keyParams = ecPrivKey.getParams();

        return ecPrivKey;
    }


    protected KeyAgreement getKeyAgreement() {
        return new ECSVDPDHC();
    }

    protected KeyPair generateEphKeyPair() {
        KeyPairGenerator kpg = new ECKeyPairGenerator();
        try {
            kpg.initialize(keyParams, random);
        } catch (InvalidAlgorithmParameterException e) {
            // the parameters have already been checked
            throw new RuntimeException("internal error");
        }
        return kpg.genKeyPair();
    }


    protected byte[] encodeEphPubKey(PublicKey ephPubKey) {
        Point q;
        try {
            q = ((ECPublicKey) ephPubKey).getW();
        } catch (InvalidKeyException e) {
            // the point is correctly initialized with parameters
            throw new RuntimeException("internal error");
        }
        return q.EC2OSP(Point.ENCODING_TYPE_COMPRESSED);
    }


    protected int getEncEphPubKeySize() {
        Point g = ((CurveParams) keyParams).getG();
        return g.EC2OSP(Point.ENCODING_TYPE_COMPRESSED).length;
    }


    protected PublicKey decodeEphPubKey(byte[] encEphPubKey) {

        try {
            ECPublicKeySpec ecPubKeySpec = new ECPublicKeySpec(encEphPubKey,
                    (CurveParams) keyParams);

            KeyFactory kf = new ECKeyFactory();
            return kf.generatePublic(ecPubKeySpec);
        } catch (InvalidParameterSpecException e) {
            throw new RuntimeException("InvalidParameterSpecException: "
                    + e.getMessage());
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("InvalidKeySpecException: "
                    + e.getMessage());
        }
    }

}
