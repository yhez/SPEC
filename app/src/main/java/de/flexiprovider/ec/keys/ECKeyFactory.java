package de.flexiprovider.ec.keys;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.pkcs8.PrivateKeyInfo;
import codec.x509.SubjectPublicKeyInfo;
import de.flexiprovider.api.keys.Key;
import de.flexiprovider.api.keys.KeyFactory;
import de.flexiprovider.api.keys.KeySpec;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurve;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurveGF2n;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurveGFP;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.PointGF2n;
import de.flexiprovider.common.math.ellipticcurves.PointGFP;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.pki.AlgorithmIdentifier;
import de.flexiprovider.pki.PKCS8EncodedKeySpec;
import de.flexiprovider.pki.PKITools;
import de.flexiprovider.pki.X509EncodedKeySpec;


public class ECKeyFactory extends KeyFactory {


    public static final String OID = "1.2.840.10045.2.1";


    public PrivateKey generatePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {

        if (keySpec instanceof ECPrivateKeySpec) {
            return new ECPrivateKey((ECPrivateKeySpec) keySpec);
        } else if (keySpec instanceof PKCS8EncodedKeySpec) {
            // get the DER-encoded Key according to PKCS#8 from the spec
            byte[] encKey = ((PKCS8EncodedKeySpec) keySpec).getEncoded();

            // decode the PKCS#8 data structure to the pki object
            PrivateKeyInfo pki = new PrivateKeyInfo();

            try {
                ASN1Tools.derDecode(encKey, pki);
                ASN1Sequence privKeySequence = (ASN1Sequence) pki
                        .getDecodedRawKey();
                ASN1OctetString oct = (ASN1OctetString) privKeySequence.get(1);
                FlexiBigInt priv = new FlexiBigInt(1, oct.getByteArray());

                AlgorithmIdentifier algId = PKITools
                        .getAlgorithmIdentifier(pki);
                AlgorithmParameters params = algId.getParams();
                CurveParams paramSpec = (CurveParams) params
                        .getParameterSpec(CurveParams.class);

                return new ECPrivateKey(priv, paramSpec);

            } catch (Exception e) {
                throw new InvalidKeySpecException(e.getClass().getName() + ": "
                        + e.getMessage());
            }
        }

        throw new InvalidKeySpecException("Unsupported key specification: "
                + keySpec + ".");
    }

    public PublicKey generatePublic(KeySpec keySpec)
            throws InvalidKeySpecException {

        if (keySpec instanceof ECPublicKeySpec) {
            ECPublicKeySpec ecPubSpec = (ECPublicKeySpec) keySpec;

            // either create a public key with parameters
            if (ecPubSpec.getParams() != null) {
                return new ECPublicKey(ecPubSpec.getW(), ecPubSpec.getParams());
            }

            // ... or one without (having the public point as uncompressed
            // encoding)
            return new ECPublicKey(ecPubSpec.getEncodedW());

        } else if (keySpec instanceof X509EncodedKeySpec) {
            // get the DER-encoded Key according to X.509 from the spec
            byte[] encKey = ((X509EncodedKeySpec) keySpec).getEncoded();

            // decode the X.509 data structure to the spki object
            SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo();
            try {
                ASN1Tools.derDecode(encKey, spki);

                // get the public key as a byte array.
                byte[] pubKeyBytes = spki.getRawKey();
                AlgorithmIdentifier algId = PKITools
                        .getAlgorithmIdentifier(spki);
                AlgorithmParameters aparam = algId.getParams();

                // see if EC domain parameters are specified for this public key
                if (aparam == null) {
                    return new ECPublicKey(pubKeyBytes);
                }

                // initialize the AlgorithmParameters.
                CurveParams ecParamSpec = (CurveParams) aparam
                        .getParameterSpec(CurveParams.class);

                EllipticCurve mE = ecParamSpec.getE();

                // make a point out of the byte array
                Point mW;
                if (mE instanceof EllipticCurveGFP) {
                    mW = new PointGFP(pubKeyBytes, (EllipticCurveGFP) mE);
                } else if (mE instanceof EllipticCurveGF2n) {
                    mW = new PointGF2n(pubKeyBytes, (EllipticCurveGF2n) mE);
                } else {
                    throw new InvalidKeySpecException(
                            "EllipticCurve must be an instance either of "
                                    + "EllipticCurveGFP or EllipticCurveGF2n.");
                }

                return new ECPublicKey(mW, ecParamSpec);

            } catch (Exception e) {
                throw new InvalidKeySpecException(e.getClass().getName() + ": "
                        + e.getMessage());
            }
        }

        throw new InvalidKeySpecException("Unsupported key specification: "
                + keySpec + ".");
    }


    public KeySpec getKeySpec(Key key, Class keySpec)
            throws InvalidKeySpecException {

        if (key instanceof ECPublicKey) {
            if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new X509EncodedKeySpec(key.getEncoded());
            } else if (ECPublicKeySpec.class.isAssignableFrom(keySpec)) {
                ECPublicKey pubKey = (ECPublicKey) key;
                Point w;
                try {
                    w = pubKey.getW();
                } catch (InvalidKeyException e) {
                    throw new InvalidKeySpecException(
                            "No EC domain parameters defined for this key. KeySpec cannot be generated.");
                }
                return new ECPublicKeySpec(w, pubKey.getParams());
            }
        } else if (key instanceof ECPrivateKey) {
            if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new PKCS8EncodedKeySpec(key.getEncoded());
            } else if (ECPrivateKeySpec.class.isAssignableFrom(keySpec)) {
                ECPrivateKey privKey = (ECPrivateKey) key;
                return new ECPrivateKeySpec(privKey.getS(), privKey.getParams());
            }
        } else {
            throw new InvalidKeySpecException("Unsupported key type: "
                    + key.getClass() + ".");
        }

        throw new InvalidKeySpecException("Unsupported key specification: "
                + keySpec + ".");
    }

    public Key translateKey(Key key) throws InvalidKeyException {

        if (key instanceof ECPublicKey) {
            // Check if key originates from this factory
            return key;
        } else if (key instanceof ECPrivateKey) {
            // Check if key originates from this factory
            return key;
        } else {
            throw new InvalidKeyException("Wrong algorithm type");
        }
    }

}
