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
import de.flexiprovider.common.math.ellipticcurves.PointGF2n;
import de.flexiprovider.common.math.ellipticcurves.ScalarMult;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.common.util.FlexiBigIntUtils;
import de.flexiprovider.core.md.SHA1;
import de.flexiprovider.ec.keys.ECPrivateKey;
import de.flexiprovider.ec.keys.ECPublicKey;
import de.flexiprovider.ec.parameters.CurveParams;


public class ECNRSignature extends Signature {

    private SecureRandom mSecureRandom;

    private MessageDigest md;

    // private key
    private FlexiBigInt mS;

    // public key
    private Point mW;

    // domain parameters
    private CurveParams mParams;

    // domain parameter r
    private FlexiBigInt mR;

    // domain Parameter g
    private Point mG;

    // precomputed powers
    private Point[] mOddPowers;

    // bitlength of mR
    private int rLength;


    private static class ECNRASN1Signature extends ASN1Sequence {

        // the value r
        private ASN1Integer r;

        // the value s
        private ASN1Integer s;

        public ECNRASN1Signature() {
            super(2);
            r = new ASN1Integer();
            s = new ASN1Integer();
            add(r);
            add(s);
        }

        public ECNRASN1Signature(FlexiBigInt mR, FlexiBigInt mS) {
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

    public ECNRSignature() {
        md = new SHA1();
    }


    public void initSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {
        if (!(privateKey instanceof ECPrivateKey)) {
            throw new InvalidKeyException(
                    "The given private Key is not an ECPrivateKey.");
        }
        ECPrivateKey privKey = (ECPrivateKey) privateKey;
        mS = privKey.getS();
        mParams = privKey.getParams();
        mR = mParams.getR();
        rLength = mR.bitLength();
        mG = mParams.getG();
        mOddPowers = ScalarMult.pre_oddpowers(mG, 4);
        md.reset();
        if (random != null) {
            mSecureRandom = random;
        } else {
            mSecureRandom = Registry.getSecureRandom();
        }
    }


    public void initVerify(PublicKey publicKey) throws InvalidKeyException {
        if (!(publicKey instanceof ECPublicKey)) {
            throw new InvalidKeyException(
                    "The given publicKey is not an ECPublicKey.");
        }
        ECPublicKey pubKey = (ECPublicKey) publicKey;
        mW = pubKey.getW();
        mParams = pubKey.getParams();
        mR = mParams.getR();
        mG = mParams.getG();
        md.reset();
    }


    public void setParameters(AlgorithmParameterSpec params)
            throws InvalidParameterException {
        if (!(params instanceof CurveParams)) {
            throw new InvalidParameterException("unsupported type");
        }
        mParams = (CurveParams) params;
    }

    public byte[] sign() {
        byte[] hash = md.digest();
        FlexiBigInt f = new FlexiBigInt(1, hash);
        FlexiBigInt i, c;

        // the bitlength of f must not be bigger than 2^r.bitlength
        if (f.compareTo(mR) >= 0) {
            f = f.mod(mR);
        }

        FlexiBigInt d;

        // generate a one-time key pair (u,V)
        // gcd(d, r) = 1
        // 1 < u < r -1 and gcd(u, r) = 1
        FlexiBigInt u = FlexiBigInt.ZERO;
        do {
            u = new FlexiBigInt(rLength, mSecureRandom);
        } while ((u.compareTo(FlexiBigInt.ONE) < 0) || // u < 1
                (u.compareTo(mR) >= 0) || // u >= r
                (u.gcd(mR).compareTo(FlexiBigInt.ONE) != 0)); // gcd(u, r) !=
        // 1

        Point v = ScalarMult.eval_SquareMultiply(ScalarMult.determineNaf(u, 4),
                mOddPowers);

        if (mG instanceof PointGF2n) {
            i = v.getXAffin().toFlexiBigInt();
        } else {
            i = v.getXAffin().toFlexiBigInt();
        }

        // c=i+f mod r
        c = i.add(f).mod(mR);
        while ((c.compareTo(FlexiBigInt.ZERO) == 0)
                || (u.compareTo(FlexiBigInt.ZERO) == 0)) {
            // generate a one-time key pair (u,V)
            // gcd(d, r) = 1
            // 1 < u < r -1 and gcd(u, r) = 1
            do {
                u = new FlexiBigInt(rLength, mSecureRandom);
            } while ((u.compareTo(FlexiBigInt.ONE) < 0) || // u < 1
                    (u.compareTo(mR) >= 0) || // u >= r
                    (u.gcd(mR).compareTo(FlexiBigInt.ONE) != 0)); // gcd(u, r)
            // != 1
            // V=uG
            // V = mG.multiply(u);
            v = ScalarMult.eval_SquareMultiply(ScalarMult.determineNaf(u, 4),
                    mOddPowers);
            // c = x(v)
            // c = i+f mod r
            c = i.add(f).mod(mR);
        }

        // d = u - sc mod r
        d = u.subtract(mS.multiply(c)).mod(mR);

        // an ASN1 ECDSSigValue is generated and DER encoded;
        // the output of the DER encoder is the signature, a byte array
        ECNRASN1Signature ecnrSigVal = new ECNRASN1Signature(c, d);

        // return the DER encoded object
        return ASN1Tools.derEncode(ecnrSigVal);
    }

    /**
     * Updates the data to be signed or verified using the specified byte.
     *
     * @param b the byte to be updated.
     */
    public void update(byte b) {
        md.update(b);
    }

    /**
     * Updates the data to be signed or verified, using the specified array of
     * bytes, starting at the specified offset.
     *
     * @param b   the array of bytes.
     * @param off the offset to start from in the array of bytes.
     * @param len the number of bytes to use, starting at offset.
     */
    public void update(byte[] b, int off, int len) {
        int l = len;
        if (l == -1) {
            l = 0;
        }
        md.update(b, off, l);
    }

    /**
     * Verify the passed-in signature.
     *
     * @param sigBytes the signature bytes to be verified
     * @return <tt>true</tt> if the signature was verified, <tt>false</tt>
     * if not.
     * @throws SignatureException if this signature object is not initialized properly.
     */
    public boolean verify(byte[] sigBytes) throws SignatureException {
        // sigBytes is an encoded ECNRSigValue. It first has to be decoded
        // to extract the values c and d
        ECNRASN1Signature eSigVal = new ECNRASN1Signature();
        try {
            ASN1Tools.derDecode(sigBytes, eSigVal);
        } catch (ASN1Exception ASN1Exc) {
            throw new SignatureException("ASN1Exception: "
                    + ASN1Exc.getMessage());
        } catch (IOException IOExc) {
            throw new SignatureException("IOException: " + IOExc.getMessage());
        }

        FlexiBigInt c = eSigVal.getR();
        FlexiBigInt d = eSigVal.getS();
        // if c < 1 or c > r -1 return false
        if ((c.compareTo(FlexiBigInt.ONE) < 0) || (c.compareTo(mR) >= 0)) {
            return false;
        }
        // if d < 1 or d > r -1 return false
        if ((d.compareTo(FlexiBigInt.ONE) < 0) || (d.compareTo(mR) >= 0)) {
            return false;
        }

        // P = dG + cW
        // Point P = mG.multiply(d).add(mW.multiply(c));

        // P = h1 * G + h2 * W
        Point[] mW1 = {mG, mW};
        FlexiBigInt[] dc = {d, c};
        Point P = ScalarMult.multiply(dc, mW1);

        if (P.isZero()) {
            return false;
        }
        // i = xP
        FlexiBigInt i;
        if (P instanceof PointGF2n) {
            i = P.getXAffin().toFlexiBigInt();
        } else {
            i = P.getXAffin().toFlexiBigInt();
        }
        // f = c-i mod (r)
        FlexiBigInt f = c.subtract(i).mod(mR);

        FlexiBigInt f1 = new FlexiBigInt(1, md.digest());
        f1 = f1.mod(mR);
        // System.out.println(f.toString(16)+", "+f1.toString(16));
        return f.compareTo(f1) == 0;
    }

}
