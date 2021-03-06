package de.flexiprovider.ec.keys;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.api.keys.KeyPairGenerator;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.ScalarMult;
import de.flexiprovider.ec.parameters.CurveParamsGFP;
import specular.systems.CryptMethods;


public class ECKeyPairGenerator extends KeyPairGenerator {


    public static final int DEFAULT_KEY_SIZE = 192;

    // the EC domain parameters
    private CurveParamsGFP curveParams;

    // the source of randomness
    private SecureRandom mRandom = null;

    // array of precomputed powers of the base point
    private Point[] mOddPowers = null;

    // curve group order
    private BigInteger r;

    // curve group order bit length
    private int rLength;

    // flag indicating whether the key pair generator has been initialized
    private boolean initialized;


    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {

        if (params == null) {
            initialize(DEFAULT_KEY_SIZE, random);
            return;
        }

        if (!(params instanceof CurveParamsGFP)) {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }

        curveParams = (CurveParamsGFP) params;
        r = curveParams.getR();
        rLength = r.bitLength();
        mOddPowers = ScalarMult.pre_oddpowers(curveParams.getG(), 4);
        mRandom = (random != null) ? random : new SecureRandom();

        initialized = true;
    }


    public void initialize(int keySize, SecureRandom random) {
        CurveParamsGFP params;
        params = new CurveParamsGFP();

        try {
            initialize(params, random);
        } catch (InvalidAlgorithmParameterException e) {
            // the parameters are correct and must be accepted
            throw new RuntimeException("internal error");
        }
    }

    private void initializeDefault() {
        initialize(DEFAULT_KEY_SIZE, new SecureRandom());
    }

    public KeyPair genKeyPair() {
        if (!initialized) {
            initializeDefault();
        }

        // find statistically unique and unpredictable integer s in the
        // interval [1, r - 1]
        BigInteger s;
        CryptMethods.randomBits rb = new CryptMethods.randomBits(512);
        do {
            byte[] rand = rb.getRandomBits();
            if (rand != null)
                s = new BigInteger(rand);
            else
                s = new BigInteger(rLength, new SecureRandom());
        } while ((s.compareTo(BigInteger.ONE) < 0) || (s.compareTo(r) >= 0));

        // create new ECPrivateKey with value s
        ECPrivateKey privKey = new ECPrivateKey(s, curveParams);

        // create new ECPublicKey with value W = sQ
        ECPublicKey pubKey = new ECPublicKey(ScalarMult.eval_SquareMultiply(
                ScalarMult.determineNaf(s, 4), mOddPowers), curveParams);

        // return the keypair
        return new KeyPair(pubKey, privKey);
    }
}
