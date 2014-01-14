package de.flexiprovider.pqc.pflash;

import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.api.keys.KeyPairGenerator;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.codingtheory.GF2mField;
import de.flexiprovider.common.math.finitefields.GF2Polynomial;
import de.flexiprovider.common.math.linearalgebra.GF2Vector;
import de.flexiprovider.common.math.linearalgebra.GF2mMatrix;
import de.flexiprovider.common.math.linearalgebra.GF2mVector;


public class PFlashKeyPairGenerator extends KeyPairGenerator {

    /**
     * The OID of the algorithm
     */
    public static final String OID = "pFLASH";

    // field F_q = GF(2^m)
    private GF2mField field;

    // m = degree of the Field GF(2^m)
    private int m;

    // dimension of K^n
    private int n;

    private int r;

    // the source of randomness
    private SecureRandom srandom;

    // flag indicating whether the key pair generator has been initialized
    private boolean initialized = false;

    public void initialize(int keysize, SecureRandom srandom) {
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom srandom)
            throws InvalidAlgorithmParameterException {

        this.srandom = (srandom != null) ? srandom : Registry.getSecureRandom();

        // if no parameters are specified, load defaults
        if (params == null) {
            initializeDefault();
            return;
        }

        if (!(params instanceof PFlashKeyGenParameterSpec)) {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }

        PFlashKeyGenParameterSpec pflashParamSpec = (PFlashKeyGenParameterSpec) params;
        field = pflashParamSpec.getField();
        m = field.getDegree();
        n = pflashParamSpec.getN();
        r = pflashParamSpec.getR();

        initialized = true;
    }

    private void initializeDefault() {

        // generate default parameters
        PFlashKeyGenParameterSpec defaults = new PFlashKeyGenParameterSpec();
        try {
            initialize(defaults, Registry.getSecureRandom());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a pFLASH key pair, consisting of a
     * {@link de.flexiprovider.pqc.pflash.PFlashPublicKey} and a {@link de.flexiprovider.pqc.pflash.PFlashPrivateKey}.
     *
     * @return the generated key pair
     * @see de.flexiprovider.api.keys.KeyPair
     */
    public KeyPair genKeyPair() {

        if (!initialized)
            initializeDefault();

        // --- generate private key --- //

        GF2Polynomial poly_384 = new GF2Polynomial(385, "RANDOM");
        while (!poly_384.isIrreducible()) {
            poly_384 = new GF2Polynomial(385, "RANDOM");
        }

        // choose all elements of m_S, m_T at random and check if inverse exists

        // map S = m_S + c_S
        byte[] mBytes = new byte[4 + n * n];
        mBytes[0] = (byte) n;
        byte[] tmp;
        for (int i = 4; i < mBytes.length; i += 96) {
            tmp = genRandomGF2mVector().getEncoded();
            System.arraycopy(tmp, 0, mBytes, i, tmp.length);
        }
        GF2mMatrix m_S = new GF2mMatrix(field, mBytes);

        // test if invertible
        GF2mMatrix invTest;
        boolean mInv = false;
        while (!mInv) {
            try {
                invTest = (GF2mMatrix) m_S.computeInverse();
                mInv = true;
            } catch (ArithmeticException ae) {
                mBytes[0] = (byte) n;
                for (int i = 4; i < mBytes.length; i += 96) {
                    tmp = genRandomGF2mVector().getEncoded();
                    System.arraycopy(tmp, 0, mBytes, i, tmp.length);
                }
                m_S = new GF2mMatrix(field, mBytes);
                mInv = false;
            }
        }

        GF2mVector c_S = genRandomGF2mVector();

        // map T = m_T + c_T
        for (int i = 4; i < mBytes.length; i += 96) {
            tmp = genRandomGF2mVector().getEncoded();
            System.arraycopy(tmp, 0, mBytes, i, tmp.length);
        }
        GF2mMatrix m_T = new GF2mMatrix(field, mBytes);

        // test if invertible
        mInv = false;
        while (!mInv) {
            try {
                invTest = (GF2mMatrix) m_T.computeInverse();
                mInv = true;
            } catch (ArithmeticException ae) {
                mBytes[0] = (byte) n;
                for (int i = 4; i < mBytes.length; i += 96) {
                    tmp = genRandomGF2mVector().getEncoded();
                    System.arraycopy(tmp, 0, mBytes, i, tmp.length);
                }
                m_T = new GF2mMatrix(field, mBytes);
                mInv = false;
            }
        }

        GF2mVector c_T = genRandomGF2mVector();

        // generate PFlashPrivateKey
        PFlashPrivateKey privateKey = new PFlashPrivateKey(m_S, c_S, m_T, c_T, poly_384);

        // --- generate public key --- //

        PFlashPublicKey publicKey = new PFlashPublicKey(n - r);

        publicKey.addElement(null);

        // return the KeyPair
        return (new KeyPair(publicKey, privateKey));
    }

    private GF2mVector genRandomGF2mVector() {
        GF2Vector v = new GF2Vector(n * m, srandom);
        return v.toExtensionFieldVector(field);
    }
}