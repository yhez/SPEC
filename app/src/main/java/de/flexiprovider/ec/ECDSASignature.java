package de.flexiprovider.ec;

import java.io.IOException;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Sequence;
import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.Signature;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.InvalidParameterException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.ScalarMult;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.common.util.FlexiBigIntUtils;
import de.flexiprovider.ec.keys.ECPrivateKey;
import de.flexiprovider.ec.keys.ECPublicKey;
import de.flexiprovider.ec.parameters.CurveParams;

public abstract class ECDSASignature extends Signature {

    // the source of randomness
    private SecureRandom mSecureRandom;

    // the message digest
    private MessageDigest md;

    // the private key
    private FlexiBigInt mS;

    // the public key
    private Point mW;

    // the EC domain parameters
    private CurveParams mParams;

    // the base point G
    private Point mG;

    // the order r of the base point G
    private FlexiBigInt mR;

    // the bit length of r
    private int rLength;

    // the windows size
    private int w;

    private Point[] mOddPowers;

    private static final FlexiBigInt ONE = FlexiBigInt.ONE;

    private static class ECDSAASN1Signature extends ASN1Sequence {

        // the value r
        private ASN1Integer r;

        // the value s
        private ASN1Integer s;

        public ECDSAASN1Signature() {
            super(2);
            r = new ASN1Integer();
            s = new ASN1Integer();
            add(r);
            add(s);
        }

        public ECDSAASN1Signature(FlexiBigInt mR, FlexiBigInt mS) {
            super(2);
            r = new ASN1Integer(1, FlexiBigIntUtils.toMinimalByteArray(mR));
            s = new ASN1Integer(1, FlexiBigIntUtils.toMinimalByteArray(mS));
            add(r);
            add(s);
        }
        public FlexiBigInt getR() {
            return ASN1Tools.getFlexiBigInt(r);
        }
        public FlexiBigInt getS() {
            return ASN1Tools.getFlexiBigInt(s);
        }
    }
    public static final class SHA1 extends ECDSASignature {


        public static final String OID = "1.2.840.10045.4.1";

    }

    public static final class SHA224 extends ECDSASignature {

        public static final String OID = "1.2.840.10045.4.3.1";

    }

    public static final class SHA256 extends ECDSASignature {

        public static final String OID = "1.2.840.10045.4.3.2";

    }
    public static final class SHA384 extends ECDSASignature {

        public static final String OID = "1.2.840.10045.4.3.3";

    }


    public static final class SHA512 extends ECDSASignature {

        /**
         * The OID of SHA512withECDSA
         */
        public static final String OID = "1.2.840.10045.4.3.4";

    }


    public void initSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {

        if (!(privateKey instanceof ECPrivateKey)) {
            throw new InvalidKeyException(privateKey + " is not an instance"
                    + " of ECPrivateKey");
        }

        ECPrivateKey privKey = (ECPrivateKey) privateKey;
        mS = privKey.getS();
        mParams = privKey.getParams();
        mR = mParams.getR();
        rLength = mR.bitLength();
        mG = mParams.getG();
        w = 5;
        // only for GF(p):
        mOddPowers = ScalarMult.precomputationCMO(mG, w + 1, 0);
        md.reset();
        mSecureRandom = (random != null) ? random : Registry.getSecureRandom();
    }


    public void initVerify(PublicKey publicKey) throws InvalidKeyException {

        if (!(publicKey instanceof ECPublicKey)) {
            throw new InvalidKeyException(publicKey + " is not an instance"
                    + " of ECPublicKey");
        }

        ECPublicKey pubKey = (ECPublicKey) publicKey;
        mW = pubKey.getW();
        mParams = pubKey.getParams();
        mR = mParams.getR();
        rLength = mR.bitLength();
        mG = mParams.getG();
        md.reset();
    }


    public void setParameters(AlgorithmParameterSpec params)
            throws InvalidParameterException {
        if (params instanceof CurveParams) {
            mParams = (CurveParams) params;
        } else {
            throw new InvalidParameterException("params is not an instance"
                    + " of ECParameterSpec");
        }
    }
    public void update(byte input) {
        md.update(input);
    }


    public void update(byte[] input, int offset, int length) {
        int l = length;
        if (l == -1) {
            l = 0;
        }
        md.update(input, offset, l);
    }

    public byte[] sign() {

	/* generate a one-time key pair (u, V) */

        // compute V=uG for 0<u< r, where r=ord(G), V!=0, and gcd(u, r)=1
        FlexiBigInt u;
        Point V;
        do {
            // select uniformly at random a FlexiBigInt u in the interval [1,
            // r-1] with gcd(u, r) == 1
            do {
                u = new FlexiBigInt(rLength, mSecureRandom);
            } while ((u.compareTo(ONE) < 0) || (u.compareTo(mR) >= 0)
                    || (u.gcd(mR).compareTo(ONE) != 0));

            // compute V = uG
            // int[] uRec = ScalarMult.determineNaf(u, w);
            int[] uRec = ScalarMult.determineNaf(u, w);
            V = ScalarMult.eval_SquareMultiply(uRec, mOddPowers);
        } while (V.isZero()); // while V = 0

        // compute u^-1 mod r
        FlexiBigInt invU = u.modInverse(mR);

        // compute c = i mod r, where i is the x-coordinate of V
        FlexiBigInt c = V.getXAffin().toFlexiBigInt().mod(mR);

        // generate message representative f
        FlexiBigInt f = computeMessageRepresentative();

        // compute f + sc mod r
        FlexiBigInt tmp = (f.add(mS.multiply(c))).remainder(mR);

        // compute d = (u^-1)*(f + sc) mod(r)
        FlexiBigInt d = invU.multiply(tmp).mod(mR);

        // ASN.1 encode the signature (c, d)
        ECDSAASN1Signature ecdsaSigVal = new ECDSAASN1Signature(c, d);

        // return the DER encoded signature
        return ASN1Tools.derEncode(ecdsaSigVal);
    }


    public boolean verify(byte[] sigBytes) throws SignatureException {

        FlexiBigInt h = FlexiBigInt.valueOf(1);
        FlexiBigInt c1 = FlexiBigInt.valueOf(1);

        // the signer's public key
        Point W = mW;

        // sigBytes is an encoded ECDSASigValue. It first has to be decoded
        // to extract the values c and d
        ECDSAASN1Signature eSigVal = new ECDSAASN1Signature();
        try {
            ASN1Tools.derDecode(sigBytes, eSigVal);
        } catch (ASN1Exception ASN1Exc) {
            throw new SignatureException("ASN1Exception: "
                    + ASN1Exc.getMessage());
        } catch (IOException IOExc) {
            throw new SignatureException("IOException: " + IOExc.getMessage());
        }

        // c and d
        FlexiBigInt c = eSigVal.getR();
        FlexiBigInt d = eSigVal.getS();

        // if c < 1 or c > r -1 return false
        if ((c.compareTo(ONE) < 0) || (c.compareTo(mR) > -1)) {
            return false;
        }

        // if d < 1 or d > r -1 return false
        if ((d.compareTo(ONE) < 0) || (d.compareTo(mR) > -1)) {
            return false;
        }

        // generate message representative
        FlexiBigInt f = computeMessageRepresentative();

        // h = (d^-1) mod r
        if (d.gcd(mR).compareTo(ONE) != 0) {
            throw new SignatureException("gcd(d, r) !=1");
        }
        h = d.modInverse(mR);

        // h1 = fh mod r
        FlexiBigInt h1 = f.multiply(h).remainder(mR);

        // h2 = ch mod r
        FlexiBigInt h2 = c.multiply(h).remainder(mR);

        // P = h1 * G + h2 * W
        FlexiBigInt[] H = {h1, h2};
        Point[] W1 = {mG, W};

        Point P = ScalarMult.multiply(H, W1);

        // c1 = i mod r, i x-coordinate of P
        c1 = P.getXAffin().toFlexiBigInt().mod(mR);

        return c1.equals(c);
    }

    private FlexiBigInt computeMessageRepresentative() {
        // Let hLen be the output length (in octets) of the hash function. If
        // rLength (the bit length of r) is smaller than 8*hLen, the rightmost
        // 8*hLen-rLength bits of the message digest have to be truncated. This
        // is done by a right shift of the integer generated from the hash
        // value.
        byte[] hash = md.digest();
        FlexiBigInt f = new FlexiBigInt(1, hash);

        int hLen = md.getDigestLength();
        int trunc = 8 * hLen - rLength;
        if (trunc > 0) {
            f = f.shiftRight(trunc);
        }

        return f;
    }

}
