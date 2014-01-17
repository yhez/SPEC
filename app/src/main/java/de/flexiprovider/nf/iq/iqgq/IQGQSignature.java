package de.flexiprovider.nf.iq.iqgq;

import java.io.IOException;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.Signature;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.quadraticfields.IQClassGroup;
import de.flexiprovider.common.math.quadraticfields.IQEncodingException;
import de.flexiprovider.common.math.quadraticfields.QuadraticIdeal;
import de.flexiprovider.common.util.ASN1Tools;


public abstract class IQGQSignature extends Signature {
    private MessageDigest md;

    private IQClassGroup classGroup;

    private FlexiBigInt exponent;

    private QuadraticIdeal alpha;

    // array of precomputed powers of theta used to speed up signing
    private QuadraticIdeal[] powersOfTheta = null;

    /**
     * Inner class providing the IQGQ ASN.1 signature structure.
     * <p/>
     * The ASN.1 signature structure is defined as follows:
     * <p/>
     * <pre>
     * IQGQ-Signature ::= SEQUENCE {
     *   sigma  Quadratic-Ideal,
     *   l      INTEGER
     *  }
     *
     * Quadratic-Ideal ::= OCTET STRING
     * </pre>
     */
    private static class IQGQASN1Signature extends ASN1Sequence {

        private ASN1OctetString sigma;

        private ASN1Integer l;

        public IQGQASN1Signature() {
            super(2);
            sigma = new ASN1OctetString();
            l = new ASN1Integer();
            add(sigma);
            add(l);
        }

        public IQGQASN1Signature(FlexiBigInt discriminant,
                                 QuadraticIdeal sigma, FlexiBigInt l) {
            super(2);
            this.sigma = new ASN1OctetString(sigma.idealToOctets(discriminant,
                    false));
            this.l = new ASN1Integer(l.toByteArray());
            add(this.sigma);
            add(this.l);
        }

        public QuadraticIdeal getSigma(FlexiBigInt discriminant)
                throws IQEncodingException {
            return QuadraticIdeal.octetsToIdeal(discriminant, sigma
                    .getByteArray());
        }

        public FlexiBigInt getL() {
            return ASN1Tools.getFlexiBigInt(l);
        }

    }

    public static class SHA1 extends IQGQSignature {

        /**
         * The OID of IQGQwithSHA1.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.1.5";

        public SHA1() {
            super(new de.flexiprovider.core.md.SHA1());
        }
    }


    protected IQGQSignature(MessageDigest md) {
        this.md = md;
    }

    public void initSign(PrivateKey key, SecureRandom prng)
            throws InvalidKeyException {
        md.reset();

        if (!(key instanceof IQGQPrivateKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        IQGQPrivateKey privKey = (IQGQPrivateKey) key;

        classGroup = new IQClassGroup(privKey.getParams().getDiscriminant(),
                prng);
        QuadraticIdeal theta = privKey.getTheta();
        exponent = privKey.getExponent();

        // precompute powers of theta
        powersOfTheta = classGroup.precomputeGordonBrickell(theta, (md
                .getDigestLength() << 3) + 1);
    }


    public void initVerify(PublicKey key) throws InvalidKeyException {
        md.reset();
        if (!(key instanceof IQGQPublicKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        IQGQPublicKey pubKey = (IQGQPublicKey) key;

        classGroup = new IQClassGroup(pubKey.getParams().getDiscriminant());
        alpha = pubKey.getAlpha();
        exponent = pubKey.getExponent();
    }

    /**
     * Updates the data to be signed or verified using the specified byte.
     *
     * @param b the byte to be updated.
     */
    public void update(byte b) {
        md.update(b);
    }


    public void update(byte[] b, int off, int len) {
        md.update(b, off, len > 0 ? len : 0);
    }


    public byte[] sign() {
        QuadraticIdeal sigma, tau, chi;
        FlexiBigInt l;

        // K -> chi
        chi = classGroup.randomIdeal();

        // tau = chi^exponent
        tau = classGroup.power(chi, exponent);

        // h = hash(M||tau)
        md.update(tau.idealToOctets(classGroup.getDiscriminant(), false));

        l = new FlexiBigInt(1, md.digest());

        // sigma = theta^l * chi
        sigma = classGroup.multiply(classGroup.power(powersOfTheta, l), chi);

        // create1 an ASN.1 sequence from the signature (sigma, l)
        IQGQASN1Signature sigValue = new IQGQASN1Signature(classGroup
                .getDiscriminant(), sigma, l);

        // return the DER encoded ASN.1 sequence
        return ASN1Tools.derEncode(sigValue);
    }

    /**
     * Verifies the signature passed in as <tt>sigBytes</tt>
     *
     * @param sigBytes the signature bytes to be verified.
     * @return <tt>true</tt> if the signature was verified, <tt>false</tt>
     * if not.
     * @throws SignatureException if this signature object is not initialized properly.
     */
    public boolean verify(byte[] sigBytes) throws SignatureException {
        QuadraticIdeal tau, sigma;
        FlexiBigInt h, l;

        IQGQASN1Signature sigValue = new IQGQASN1Signature();
        try {
            ASN1Tools.derDecode(sigBytes, sigValue);
            sigma = sigValue.getSigma(classGroup.getDiscriminant());
        } catch (ASN1Exception asn1e) {
            throw new SignatureException(
                    "ASN1Exception: can not decode signature: "
                            + asn1e.getMessage());
        } catch (IOException ioe) {
            throw new SignatureException("IOException: " + ioe.getMessage());
        } catch (IQEncodingException iqee) {
            throw new SignatureException("IQEncodingException: "
                    + iqee.getMessage());
        }

        l = sigValue.getL();

        // tau = alpha^l* sigma^exponent
        FlexiBigInt[] exponents = {l, exponent};
        QuadraticIdeal[] bases = {alpha, sigma};
        QuadraticIdeal[][] gLUT = classGroup.precomputeSimPowerWNAF(bases, 3);
        tau = classGroup.simPowerWNAF(gLUT, exponents, 3);

        // h = hash(M||tau)
        md.update(tau.idealToOctets(classGroup.getDiscriminant(), false));

        h = new FlexiBigInt(1, md.digest());

        // verification:
        // l ?= hash(m || tau)
        return h.equals(l);
    }

    /**
     * Set the parameters for the signature.
     *
     * @param params the parameters
     * @throws InvalidAlgorithmParameterException if the parameters are not an instance of
     *                                            {@link IQGQParameterSpec}.
     */
    public void setParameters(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        if (!(params instanceof IQGQParameterSpec)) {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }
        classGroup = new IQClassGroup(((IQGQParameterSpec) params)
                .getDiscriminant());
    }
}
