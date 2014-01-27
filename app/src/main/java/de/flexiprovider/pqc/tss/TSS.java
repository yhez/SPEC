package de.flexiprovider.pqc.tss;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Vector;

import codec.asn1.ASN1Exception;
import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.Signature;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.IntegerFunctions;

public abstract class TSS extends Signature {


    public static class SHA1 extends TSS {

        public SHA1() {
            super(new de.flexiprovider.core.md.SHA1());
        }
    }

    public static class SHA256 extends TSS {

        public SHA256() {
            super(new de.flexiprovider.core.md.SHA256());
        }
    }


    public static int floor2Log(int i) {
        int counter = 0;
        while (i >= 1024) {
            i = i >>> 10;
            counter += 10;
        }
        while (i != 1) {
            i = i >>> 1;
            counter += 1;
        }

        return counter;
    }

    private TSSPrivateKey privKey = null;

    private TSSPublicKey pubKey = null;

    private TSSPolynomial refGfp;

    private byte[] message = null;


    protected MessageDigest messageDigest = null;

    protected TSSHashFunction hashFunction = null;

    protected int n = 0;

    protected int m = 0;

    // prime
    protected long p = 0;

    protected TSS(MessageDigest md) {
        messageDigest = md;
    }

    public Vector addGFPVector(Vector a, Vector b) {
        Vector result = new Vector();
        int aSize = a.size();
        int bSize = b.size();
        int max = Math.max(aSize, bSize);
        int min = Math.min(aSize, bSize);

        result.setSize(max);

        if (bSize > aSize) {
            Vector c = a;
            a = b;
            b = c;
        }

        for (int i = max - 1; i >= 0; i--) {
            if (i >= min) {
                result.setElementAt((a.elementAt(i)), i);
            } else {
                result.setElementAt((((TSSPolynomial) a.elementAt(i))
                        .add((TSSPolynomial) b.elementAt(i))), i);
            }
        }

        return result;
    }

    public long[] binary2ternary(byte[] binary) {
        SecureRandom rand = null;
        try {
            rand = SecureRandom.getInstance("SHA1PRNG");
            rand.setSeed(binary);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        long[] ternary = new long[n];
        // System.arraycopy(binary, 0, ternary, 0, n);
        for (int i = n - 1; i >= 0; i--) {
            ternary[i] = rand.nextInt(3) - 1;
        }

        return ternary;
    }


    public boolean checkBound(long[] arr, long bound) {
        for (int i = arr.length - 1; i >= 0; i--) {
            if (Math.abs(arr[i]) > bound) {
                System.out.println(arr[i] + " > " + bound + " at index " + i);
                return false;
            }
        }
        return true;
    }


    public boolean checkBound(Vector v, long bound) {
        for (int i = v.size() - 1; i >= 0; i--) {
            if (!checkBound(((TSSPolynomial) v.elementAt(i)).getCompressed(),
                    bound)) {
                return false;
            }
        }
        return true;
    }

    public Vector getPolyVector(int limit, int amount) {
        Vector result = new Vector();
        result.setSize(amount);

        for (int i = amount - 1; i >= 0; i--) {
            result.setElementAt(refGfp.generatePoly(limit, true), i);
        }

        return result;
    }

    public void initSign(PrivateKey privKey,
                         de.flexiprovider.api.SecureRandom random)
            throws InvalidKeyException {
        if (((Object)privKey).getClass().equals(TSSPrivateKey.class)) {
            this.privKey = (TSSPrivateKey) privKey;
        } else {
            throw new InvalidKeyException();
        }
    }

    public void initVerify(PublicKey pubKey) throws InvalidKeyException {
        if (((Object)pubKey).getClass().equals(TSSPublicKey.class)) {
            this.pubKey = (TSSPublicKey) pubKey;
        } else {
            throw new InvalidKeyException();
        }
    }
    public TSSPolynomial oracle(TSSPolynomial gfp, byte[] b) {
        byte[] gfpArr = null;

        try {
            gfpArr = gfp.getEncoded();
        } catch (ASN1Exception asn1Ex) {
            // TODO
            asn1Ex.printStackTrace();
        } catch (IOException ioEx) {
            // TODO
            ioEx.printStackTrace();
        }

        byte[] combined = new byte[gfpArr.length + b.length];

        System.arraycopy(gfpArr, 0, combined, 0, gfpArr.length);
        System.arraycopy(b, 0, combined, gfpArr.length, b.length);

        combined = messageDigest.digest(combined);

        long[] encoded = binary2ternary(combined);

        return new TSSPolynomial(gfp.getF(), p, encoded);
    }

    private byte[] parse2TSSByte(Vector v, TSSPolynomial gfp) {
        int size = v.size() + 1;
        v.setSize(size);

        v.setElementAt(gfp, size - 1);

        return new TSSVectorSerial(v).getArrayRepresentation();
    }

    public void setParameters(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        if (!(params instanceof TSSParameterSpec)) {
            throw new InvalidAlgorithmParameterException("Wrong Parameterclass");
        }

        if (params == null) {
            throw new InvalidAlgorithmParameterException(
                    "Params cannot be null");
        }

        TSSParameterSpec paramSpec = (TSSParameterSpec) params;

        hashFunction = paramSpec.getHFunction();

        n = paramSpec.getN();
        m = paramSpec.getM();
        p = paramSpec.getP();
        refGfp = paramSpec.getRefGFP();

        int[] f = new int[n + 1];
        f[n] = 1;
        f[0] = 1;
    }

    public byte[] sign() {

        message = messageDigest.digest();
        messageDigest.reset();

        int yBound;
        int gBound;

        yBound = m
                * (int) Math.floor(IntegerFunctions.intRoot(IntegerFunctions
                .pow(n, 3), 2)
                * IntegerFunctions.floatLog(n));
        gBound = yBound - (int) (Math.sqrt(n) * IntegerFunctions.floatLog(n));

        int counter = 0;

        TSSPolynomial e;
        Vector z;

        Vector y;
        System.out.println("Attempting to find match...");
        do {
            Date time1 = new Date();
            y = getPolyVector(yBound + 1, m);
            e = oracle(hashFunction.calculatHash(y), message);

            z = addGFPVector(e.multiply(privKey.getKey()), y);
            if (counter > 1000) {
                try {
                    throw new Exception("could not generate a valid Signature");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    break;
                }
            }
            counter += 1;
            Date time2 = new Date();
            System.out.println("Signature generation took "
                    + (time2.getTime() - time1.getTime()) + " ms.");
        } while (!checkBound(z, gBound));
        System.out.println("Match found.");
        return parse2TSSByte(z, e);
    }

    public void update(byte input) throws SignatureException {
        messageDigest.update(input);
    }

    public void update(byte[] input, int inOff, int inLen)
            throws SignatureException {
        messageDigest.update(input, inOff, inLen);
    }

    public boolean verify(byte[] signature) throws SignatureException {

        byte[] mue = message;
        Vector z;
        long gBound;
        long yBound;

        z = new TSSVectorSerial(signature).getVectorRepresentation();

        int vSize = z.size() - 1;

        TSSPolynomial e = (TSSPolynomial) z.elementAt(vSize);
        z.removeElementAt(vSize);
        z.setSize(vSize);

        yBound = m
                * (long) Math.floor(IntegerFunctions.intRoot(IntegerFunctions
                .pow(n, 3), 2)
                * IntegerFunctions.floatLog(n));

        gBound = yBound - (long) (Math.sqrt(n) * IntegerFunctions.floatLog(n));
        e.print();

        return checkBound(z, gBound) && e.equals(oracle(hashFunction.calculatHash(z).subtract(pubKey.getS().multiply(e)), mue));
    }
}
