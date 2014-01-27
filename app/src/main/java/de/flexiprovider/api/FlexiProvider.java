package de.flexiprovider.api;

import java.security.AlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParametersSpi;
import java.security.KeyFactorySpi;
import java.security.KeyPairGeneratorSpi;
import java.security.MessageDigestSpi;
import java.security.Provider;
import java.security.SecureRandomSpi;
import java.security.SignatureSpi;

import javax.crypto.CipherSpi;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.MacSpi;
import javax.crypto.SecretKeyFactorySpi;

import de.flexiprovider.api.exceptions.RegistrationException;

public abstract class FlexiProvider extends Provider {


    protected static final int CIPHER = 0;


    protected static final int MAC = 1;


    protected static final int ALG_PARAMS = 5;

    protected static final int ALG_PARAM_GENERATOR = 6;

    protected static final int SECRET_KEY_GENERATOR = 7;


    protected static final int KEY_PAIR_GENERATOR = 8;

    protected static final int SECRET_KEY_FACTORY = 9;

    protected static final int KEY_FACTORY = 10;

    // array holding the algorithm type prefixes (indexed by algorithm type)
    private static final String[] prefixes = {"Cipher.", "Mac.",
            "MessageDigest.", "SecureRandom.", "Signature.",
            "AlgorithmParameters.", "AlgorithmParameterGenerator.",
            "KeyGenerator.", "KeyPairGenerator.", "SecretKeyFactory.",
            "KeyFactory.", "KeyAgreement."};

    // array holding all algorithm types (used for registration type checking)
    private static final Class[] algClasses = {CipherSpi.class, MacSpi.class,
            MessageDigestSpi.class, SecureRandomSpi.class, SignatureSpi.class,
            AlgorithmParametersSpi.class, AlgorithmParameterGeneratorSpi.class,
            KeyGeneratorSpi.class, KeyPairGeneratorSpi.class,
            SecretKeyFactorySpi.class, KeyFactorySpi.class,
            KeyAgreementSpi.class};

    protected FlexiProvider(String name, double version, String info) {
        super(name, version, info);
    }

    protected void add(int type, Class algClass, String algName)
            throws RegistrationException {
        add(type, algClass, new String[]{algName});
    }

    protected void add(int type, Class algClass, String[] algNames)
            throws RegistrationException {

        String prefix = getPrefix(type);
        // trivial cases
        if ((prefix == null) || (algClass == null) || (algNames == null)
                || (algNames.length == 0)) {
            return;
        }

        // type checking
        Class expClass = algClasses[type];
        if (!expClass.isAssignableFrom(algClass)) {
            throw new RegistrationException(
                    "expected and actual algorithm types do not match");
        }

        // register first name
        put(prefix + algNames[0], algClass.getName());

        // register additional names (aliases)
        for (int i = 1; i < algNames.length; i++) {
            put("Alg.Alias." + prefix + algNames[i], algNames[0]);
        }
    }


    protected void addReverseOID(int type, String algName, String oid)
            throws RegistrationException {
        // get prefix
        String prefix = getPrefix(type);
        if (prefix == null) {
            // unknown type
            return;
        }

        // check if algorithm is registered
        Object alg = get(prefix + algName);
        if (alg == null) {
            throw new RegistrationException("no such algorithm: " + algName);
        }

        put("Alg.Alias." + prefix + "OID." + oid, algName);
    }

    private static String getPrefix(int type) {
        if (type > prefixes.length) {
            return null;
        }
        return prefixes[type];
    }

}
