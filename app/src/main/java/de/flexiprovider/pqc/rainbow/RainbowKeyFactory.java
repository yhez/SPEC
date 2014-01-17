package de.flexiprovider.pqc.rainbow;

import codec.CorruptedCodeException;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import codec.pkcs8.PrivateKeyInfo;
import codec.x509.SubjectPublicKeyInfo;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.InvalidKeySpecException;
import de.flexiprovider.api.keys.Key;
import de.flexiprovider.api.keys.KeySpec;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.pki.PKCS8EncodedKeySpec;
import de.flexiprovider.pki.X509EncodedKeySpec;


public class RainbowKeyFactory extends de.flexiprovider.api.keys.KeyFactory {

    /**
     * The OID of Rainbow
     */
    public static final String OID = "1.3.6.1.4.1.8301.3.1.3.5.3.2";


    public PrivateKey generatePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {
        if (keySpec instanceof RainbowPrivateKeySpec) {
            return new RainbowPrivateKey((RainbowPrivateKeySpec) keySpec);
        } else if (keySpec instanceof PKCS8EncodedKeySpec) {
            // get the DER-encoded Key according to PKCS#8 from the spec
            byte[] encKey = ((PKCS8EncodedKeySpec) keySpec).getEncoded();

            // decode the PKCS#8 data structure to the pki object
            PrivateKeyInfo pki = new PrivateKeyInfo();
            try {
                ASN1Tools.derDecode(encKey, pki);
            } catch (Exception ce) {
                throw new InvalidKeySpecException(
                        "Unable to decode PKCS8EncodedKeySpec.");
            }

            try {
                // get the inner type inside the OCTET STRING
                ASN1Type innerType = pki.getDecodedRawKey();

                // now, build and return the actual key
                ASN1Sequence privKey = (ASN1Sequence) innerType;

                // decode oidString (but we don't need it right now)
                String oidString = privKey.get(0)
                        .toString();

                // decode <A1inv>
                ASN1Sequence asnA1 = (ASN1Sequence) privKey.get(1);
                int dim = asnA1.size();
                byte[][] A1inv = new byte[dim][dim];
                for (int i = 0; i < dim; i++) {
                    A1inv[i] = ((ASN1OctetString) asnA1.get(i)).getByteArray();
                }

                // decode <b1>
                byte[] b1 = ((ASN1OctetString) privKey.get(2)).getByteArray();

                // decode <A2inv>
                ASN1Sequence asnA2 = (ASN1Sequence) privKey.get(3);
                dim = asnA2.size();
                byte[][] A2inv = new byte[dim][dim];
                for (int i = 0; i < dim; i++) {
                    A2inv[i] = ((ASN1OctetString) asnA2.get(i)).getByteArray();
                }

                // decode <b2>
                byte[] b2 = ((ASN1OctetString) privKey.get(4)).getByteArray();

                // decode <vi>
                byte[] vi = ((ASN1OctetString) privKey.get(5)).getByteArray();

                // decode <layers>
                ASN1Sequence asnLayers = (ASN1Sequence) privKey.get(6);
                int numOfLayers = asnLayers.size();
                byte[][][][] alphas = new byte[numOfLayers][][][];
                byte[][][][] betas = new byte[numOfLayers][][][];
                byte[][][] gammas = new byte[numOfLayers][][];
                byte[][] etas = new byte[numOfLayers][];
                // pack coefficients of all polynomials of each layer into arrs
                for (int l = 0; l < numOfLayers; l++) {
                    ASN1Sequence asnLayer = (ASN1Sequence) asnLayers.get(l);
                    int oi; // num of Polynomials

                    // alphas
                    ASN1Sequence asnAlphas = (ASN1Sequence) asnLayer.get(0);
                    oi = asnAlphas.size();
                    alphas[l] = new byte[oi][][];
                    for (int i = 0; i < oi; i++) {
                        ASN1Sequence asnAlphas2d = (ASN1Sequence) asnAlphas
                                .get(i);
                        alphas[l][i] = new byte[asnAlphas2d.size()][];
                        for (int j = 0; j < asnAlphas2d.size(); j++) {
                            alphas[l][i][j] = ((ASN1OctetString) asnAlphas2d
                                    .get(j)).getByteArray();
                        }
                    }
                    // betas
                    ASN1Sequence asnBetas = (ASN1Sequence) asnLayer.get(1);
                    // oi = asnBetas.size();
                    betas[l] = new byte[oi][][];
                    for (int i = 0; i < oi; i++) {
                        ASN1Sequence asnBetas2d = (ASN1Sequence) asnBetas
                                .get(i);
                        betas[l][i] = new byte[asnBetas2d.size()][];
                        for (int j = 0; j < asnBetas2d.size(); j++) {
                            betas[l][i][j] = ((ASN1OctetString) asnBetas2d
                                    .get(j)).getByteArray();
                        }
                    }
                    // gammas
                    ASN1Sequence asnG = (ASN1Sequence) asnLayer.get(2);
                    // oi = asnG.size();
                    gammas[l] = new byte[oi][];
                    for (int i = 0; i < oi; i++) {
                        gammas[l][i] = ((ASN1OctetString) asnG.get(i))
                                .getByteArray();
                    }

                    // etas
                    etas[l] = ((ASN1OctetString) asnLayer.get(3))
                            .getByteArray();
                }

                // create a private key out of this asn-extracted data
                return new RainbowPrivateKey(new RainbowPrivateKeySpec(oidString, A1inv,
                        b1, A2inv, b2, vi, alphas, betas, gammas, etas));

            } catch (CorruptedCodeException cce) {
                throw new InvalidKeySpecException(
                        "Unable to decode PKCS8EncodedKeySpec.");
            }
        }

        throw new InvalidKeySpecException("Unsupported key specification: "
                + keySpec.getClass() + ".");
    }

    /**
     * Converts, if possible, a key specification into a
     * {@link RainbowPublicKey}. Currently, the following key specifications are
     * supported: {@link RainbowParameterSpec}, {@link de.flexiprovider.pki.X509EncodedKeySpec}.
     * <p/>
     * <p/>
     * <p/>
     * The ASN.1 definition of a public key's structure is
     * <p/>
     * <pre>
     *    RainbowPublicKey ::= SEQUENCE {
     *      oid            OBJECT IDENTIFIER        -- OID identifying the algorithm
     *      docLength      Integer      	        -- length of signable msg
     *      coeffquadratic SEQUENCE OF OCTET STRING -- quadratic (mixed) coefficients
     *      coeffsingular  SEQUENCE OF OCTET STRING -- singular coefficients
     *      coeffscalar	   OCTET STRING             -- scalar coefficients
     *       }
     * </pre>
     * <p/>
     * <p/>
     *
     * @param keySpec the key specification
     * @return the Rainbow public key
     * @throws de.flexiprovider.api.exceptions.InvalidKeySpecException if the KeySpec is not supported.
     */
    public PublicKey generatePublic(KeySpec keySpec)
            throws InvalidKeySpecException {

        if (keySpec instanceof RainbowPublicKeySpec) {
            return new RainbowPublicKey((RainbowPublicKeySpec) keySpec);
        } else if (keySpec instanceof X509EncodedKeySpec) {
            // get the DER-encoded Key according to X.509 from the spec
            byte[] encKey = ((X509EncodedKeySpec) keySpec).getEncoded();

            // decode the X.509 data structure to the pki object
            SubjectPublicKeyInfo pki = new SubjectPublicKeyInfo();
            try {
                ASN1Tools.derDecode(encKey, pki);
            } catch (Exception ce) {
                throw new InvalidKeySpecException(
                        "Unable to decode X509EncodedKeySpec.");
            }

            try {
                // get the inner type inside the OCTET STRING
                ASN1Type innerType = pki.getDecodedRawKey();

                // now, build and return the actual key
                ASN1Sequence pubKey = (ASN1Sequence) innerType;

                // decode oidString (but we don't need it right now)
                String oidString = pubKey.get(0)
                        .toString();

                // decode <docLength>
                FlexiBigInt fbiDocLength = ASN1Tools
                        .getFlexiBigInt((ASN1Integer) pubKey.get(1));
                int docLength = fbiDocLength.intValue();

                // decode <coeffQuadratic>
                ASN1Sequence asnCoeffQuad = (ASN1Sequence) pubKey.get(2);
                byte[][] coeffQuad = new byte[asnCoeffQuad.size()][];
                for (int i = 0; i < coeffQuad.length; i++) {
                    coeffQuad[i] = ((ASN1OctetString) asnCoeffQuad.get(i))
                            .getByteArray();
                }

                // decode <coeffSingular>
                ASN1Sequence asnCoeffSing = (ASN1Sequence) pubKey.get(3);
                byte[][] coeffSing = new byte[asnCoeffSing.size()][];
                for (int i = 0; i < coeffSing.length; i++) {
                    coeffSing[i] = ((ASN1OctetString) asnCoeffSing.get(i))
                            .getByteArray();
                }

                // decode <coeffScalar>
                byte[] coeffScal = ((ASN1OctetString) pubKey.get(4))
                        .getByteArray();

                return new RainbowPublicKey(new RainbowPublicKeySpec(oidString, docLength,
                        coeffQuad, coeffSing, coeffScal));

            } catch (CorruptedCodeException cce) {
                throw new InvalidKeySpecException(
                        "Unable to decode X509EncodedKeySpec: "
                                + cce.getMessage());
            }
        }

        throw new InvalidKeySpecException("Unknown key specification: "
                + keySpec + ".");
    }

    /**
     * Converts a given key into a key specification, if possible. Currently the
     * following specs are supported:
     * <ul>
     * <li>for RainbowPublicKey: X509EncodedKeySpec, RainbowPublicKeySpec
     * <li>for RainbowPrivateKey: PKCS8EncodedKeySpec, RainbowPrivateKeySpec
     * </ul>
     *
     * @param key     the key
     * @param keySpec the key specification
     * @return the specification of the CMSS key
     * @throws de.flexiprovider.api.exceptions.InvalidKeySpecException if the key type or key specification is not supported.
     * @see de.flexiprovider.pqc.rainbow.RainbowPrivateKey
     * @see de.flexiprovider.pqc.rainbow.RainbowPrivateKeySpec
     * @see de.flexiprovider.pqc.rainbow.RainbowPublicKey
     * @see de.flexiprovider.pqc.rainbow.RainbowPublicKeySpec
     */
    public final KeySpec getKeySpec(Key key, Class keySpec)
            throws InvalidKeySpecException {
        if (key instanceof RainbowPrivateKey) {
            if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new PKCS8EncodedKeySpec(key.getEncoded());
            } else if (RainbowPrivateKeySpec.class.isAssignableFrom(keySpec)) {
                RainbowPrivateKey privKey = (RainbowPrivateKey) key;
                return new RainbowPrivateKeySpec(privKey.getOIDString(), privKey.getA1inv(), privKey
                        .getb1(), privKey.getA2inv(), privKey.getb2(), privKey
                        .getVi(), privKey.getLayers());
            }
        } else if (key instanceof RainbowPublicKey) {
            if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new X509EncodedKeySpec(key.getEncoded());
            } else if (RainbowPublicKeySpec.class.isAssignableFrom(keySpec)) {
                RainbowPublicKey pubKey = (RainbowPublicKey) key;
                return new RainbowPublicKeySpec(pubKey.getDocLength(), pubKey
                        .getCoeffquadratic(), pubKey.getCoeffsingular(), pubKey
                        .getCoeffscalar());
            }
        } else {
            throw new InvalidKeySpecException("Unsupported key type: "
                    + key.getClass() + ".");
        }

        throw new InvalidKeySpecException("Unknown key specification: "
                + keySpec + ".");
    }

    /**
     * Translates a key into a form known by the FlexiProvider. Currently the
     * following key types are supported: RainbowPrivateKey, RainbowPublicKey.
     *
     * @param key the key
     * @return a key of a known key type
     * @throws de.flexiprovider.api.exceptions.InvalidKeyException if the key is not supported.
     */
    public final Key translateKey(Key key) throws InvalidKeyException {
        if (key instanceof RainbowPrivateKey || key instanceof RainbowPublicKey) {
            return key;
        }
        throw new InvalidKeyException("Unsupported key type");
    }

}