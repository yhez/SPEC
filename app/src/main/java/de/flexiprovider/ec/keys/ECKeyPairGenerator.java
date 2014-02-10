package de.flexiprovider.ec.keys;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.api.keys.KeyPairGenerator;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.ScalarMult;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.CurveRegistry;
import specular.systems.CryptMethods;
import specular.systems.StaticVariables;


public class ECKeyPairGenerator extends KeyPairGenerator {


    public static final int DEFAULT_KEY_SIZE = 192;

    // the EC domain parameters
    private CurveParams curveParams;

    // the source of randomness
    private SecureRandom mRandom = null;

    // array of precomputed powers of the base point
    private Point[] mOddPowers = null;

    // curve group order
    private FlexiBigInt r;

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

        if (!(params instanceof CurveParams)) {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }

        curveParams = (CurveParams) params;
        r = curveParams.getR();
        rLength = r.bitLength();
        mOddPowers = ScalarMult.pre_oddpowers(curveParams.getG(), 4);
        mRandom = (random != null) ? random : Registry.getSecureRandom();

        initialized = true;
    }


    public void initialize(int keySize, SecureRandom random) {
        CurveParams params;
        try {
            String paramName = CurveRegistry.getDefaultCurveParams(keySize);
            params = (CurveParams) Registry.getAlgParamSpec(paramName);
        } catch (InvalidAlgorithmParameterException e) {
            // no default curve exists
            throw new RuntimeException(e.getMessage());
        }

        try {
            initialize(params, random);
        } catch (InvalidAlgorithmParameterException e) {
            // the parameters are correct and must be accepted
            throw new RuntimeException("internal error");
        }
    }

    private void initializeDefault() {
        initialize(DEFAULT_KEY_SIZE, Registry.getSecureRandom());
    }
    public KeyPair genKeyPair() {
        if (!initialized) {
            initializeDefault();
        }

        // find statistically unique and unpredictable integer s in the
        // interval [1, r - 1]
        FlexiBigInt s;
        if(StaticVariables.creating_new_keys){
        CryptMethods.randomBits rb = new CryptMethods.randomBits(512);
        do {
            s = new FlexiBigInt(rb.getRandomBits());
        } while ((s.compareTo(FlexiBigInt.ONE) < 0) || (s.compareTo(r) >= 0));
        }else{
            do {
                s = new FlexiBigInt(rLength,mRandom);
            } while ((s.compareTo(FlexiBigInt.ONE) < 0) || (s.compareTo(r) >= 0));
        }
        // create new ECPrivateKey with value s
        ECPrivateKey privKey = new ECPrivateKey(s, curveParams);

        // create new ECPublicKey with value W = sQ
        ECPublicKey pubKey = new ECPublicKey(ScalarMult.eval_SquareMultiply(
                ScalarMult.determineNaf(s, 4), mOddPowers), curveParams);

        // return the keypair
        return new KeyPair(pubKey, privKey);
    }

}
