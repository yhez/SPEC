package de.flexiprovider.api;

import java.util.Hashtable;
import java.util.Vector;

import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.NoSuchAlgorithmException;
import de.flexiprovider.api.exceptions.NoSuchModeException;
import de.flexiprovider.api.exceptions.NoSuchPaddingException;
import de.flexiprovider.api.exceptions.RegistrationException;
import de.flexiprovider.api.keys.KeyFactory;
import de.flexiprovider.api.keys.KeyPairGenerator;
import de.flexiprovider.api.keys.SecretKeyFactory;
import de.flexiprovider.api.keys.SecretKeyGenerator;
import de.flexiprovider.api.parameters.AlgorithmParameterGenerator;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.common.mode.CBC;
import de.flexiprovider.common.mode.CFB;
import de.flexiprovider.common.mode.CFBParameterSpec;
import de.flexiprovider.common.mode.CTR;
import de.flexiprovider.common.mode.ECB;
import de.flexiprovider.common.mode.ModeParamGenParameterSpec;
import de.flexiprovider.common.mode.ModeParameterGenerator;
import de.flexiprovider.common.mode.ModeParameterSpec;
import de.flexiprovider.common.mode.ModeParameters;
import de.flexiprovider.common.mode.OFB;
import de.flexiprovider.common.mode.OFBParameterSpec;
import de.flexiprovider.common.padding.NoPadding;
import de.flexiprovider.common.padding.OneAndZeroesPadding;
import de.flexiprovider.common.padding.PKCS5Padding;
import de.flexiprovider.common.util.DefaultPRNG;

/**
 * This class is responsible for the registration and instantiation of all
 * cryptographic algorithms of the FlexiProvider. It provides methods for adding
 * registrations of algorithms and methods for instantiating registered
 * algorithms.
 *
 * @author Johannes M�ller
 * @author Martin D�ring
 */
public abstract class Registry {

	/* algorithm type constants */

    /**
     * Constant for asymmetric hybrid ciphers
     */
    public static final int ASYMMETRIC_HYBRID_CIPHER = 1;

    /**
     * Constant for symmetric block ciphers
     */
    public static final int BLOCK_CIPHER = 2;

    /**
     * Constant for modes of operation
     */
    public static final int MODE = 3;

    /**
     * Constant for padding schemes
     */
    public static final int PADDING_SCHEME = 4;

    /**
     * Constant for message authentication codes (MACs)
     */
    public static final int MAC = 6;

    /**
     * Constant for algorithm parameter specifications
     */
    public static final int ALG_PARAM_SPEC = 10;

    /**
     * Constant for algorithm parameters (used to encode and decode parameter
     * specifications)
     */
    public static final int ALG_PARAMS = 11;

    /**
     * Constant for algorithm parameter generators
     */
    public static final int ALG_PARAM_GENERATOR = 12;

    /**
     * Constant for secret key generators
     */
    public static final int SECRET_KEY_GENERATOR = 13;

    /**
     * Constant for key pair generators
     */
    public static final int KEY_PAIR_GENERATOR = 14;

    /**
     * Constant for secret key factories
     */
    public static final int SECRET_KEY_FACTORY = 15;

    /**
     * Constant for key factories
     */
    public static final int KEY_FACTORY = 16;

    /* hash tables for the different algorithm types */

    private static final Hashtable asymBlockCiphers = new Hashtable();
    private static final Hashtable asymHybridCiphers = new Hashtable();
    private static final Hashtable blockCiphers = new Hashtable();
    private static final Hashtable modes = new Hashtable();
    private static final Hashtable paddingSchemes = new Hashtable();
    private static final Hashtable ciphers = new Hashtable();
    private static final Hashtable macs = new Hashtable();
    private static final Hashtable messageDigests = new Hashtable();
    private static final Hashtable secureRandoms = new Hashtable();
    private static final Hashtable signatures = new Hashtable();
    private static final Hashtable algParamSpecs = new Hashtable();
    private static final Hashtable algParams = new Hashtable();
    private static final Hashtable algParamGenerators = new Hashtable();
    private static final Hashtable secretKeyGenerators = new Hashtable();
    private static final Hashtable keyPairGenerators = new Hashtable();
    private static final Hashtable secretKeyFactories = new Hashtable();
    private static final Hashtable keyFactories = new Hashtable();
    private static final Hashtable keyDerivations = new Hashtable();
    private static final Hashtable keyAgreements = new Hashtable();

    // array holding all hash tables (indexed by algorithm type)
    private static final Hashtable[] hashtables = {asymBlockCiphers,
            asymHybridCiphers, blockCiphers, modes, paddingSchemes, ciphers,
            macs, messageDigests, secureRandoms, signatures, algParamSpecs,
            algParams, algParamGenerators, secretKeyGenerators,
            keyPairGenerators, secretKeyFactories, keyFactories,
            keyDerivations, keyAgreements};

    // array holding all algorithm types (used for registration type checking)
    private static final Class[] algClasses = {AsymmetricBlockCipher.class,
            AsymmetricHybridCipher.class, BlockCipher.class, Mode.class,
            PaddingScheme.class, Cipher.class, Mac.class, MessageDigest.class,
            SecureRandom.class, Signature.class, AlgorithmParameterSpec.class,
            AlgorithmParameters.class, AlgorithmParameterGenerator.class,
            SecretKeyGenerator.class, KeyPairGenerator.class,
            SecretKeyFactory.class, KeyFactory.class, KeyDerivation.class,
            KeyAgreement.class};

    // hash table for standard algorithm parameters
    private static final Hashtable standardAlgParams = new Hashtable();

    static {
        add(ALG_PARAM_SPEC, CFBParameterSpec.class, "CFB");
        add(ALG_PARAM_SPEC, OFBParameterSpec.class, "OFB");
        add(ALG_PARAM_SPEC, ModeParameterSpec.class, new String[]{"Mode",
                "IV"});
        add(ALG_PARAMS, ModeParameters.class, new String[]{"Mode", "IV"});
        add(ALG_PARAM_SPEC, ModeParamGenParameterSpec.class, new String[]{
                "ModeParamGen", "IVParamGen"});
        add(ALG_PARAM_GENERATOR, ModeParameterGenerator.class, new String[]{
                "Mode", "IV"});

        add(MODE, ECB.class, "ECB");
        add(MODE, CBC.class, "CBC");
        add(MODE, OFB.class, "OFB");
        add(MODE, CFB.class, "CFB");
        add(MODE, CTR.class, "CTR");

        add(PADDING_SCHEME, NoPadding.class, "NoPadding");
        add(PADDING_SCHEME, OneAndZeroesPadding.class, "OneAndZeroesPadding");
        add(PADDING_SCHEME, PKCS5Padding.class, "PKCS5Padding");
    }


    public static void add(int type, Class algClass, String algName) {
        add(type, algClass, new String[]{algName});
    }


    public static void add(int type, Class algClass, String[] algNames) {
        Hashtable table = getHashtable(type);
        // trivial cases
        if ((table == null) || (algClass == null) || (algNames == null)
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
        table.put(algNames[0], algClass);

        // register additional names (aliases)
        for (int i = 1; i < algNames.length; i++) {
            table.put(algNames[i], algNames[0]);
        }
    }

    /**
     * Try to find an algorithm with the specified name inside the corresponding
     * hashtable and return an instance of the algorithm.
     *
     * @param transformation the transformation (either of the form 'algorithm' or
     *                       'algorithm/mode/padding')
     * @return block cipher object
     * @throws de.flexiprovider.api.exceptions.NoSuchAlgorithmException if the block cipher or mode cannot be found.
     * @throws de.flexiprovider.api.exceptions.NoSuchPaddingException   if the padding scheme cannot be found.
     */
    public static BlockCipher getBlockCipher(String transformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException {

        String algName, modeName = null, paddingName = null;
        int endIndex = transformation.indexOf('/');
        if (endIndex < 0) {
            // transformation is of the form 'algorithm'
            algName = transformation;
        } else {
            // transformation is of the form 'algorithm/mode/padding'

            // get 'algorithm'
            algName = transformation.substring(0, endIndex);

            // get 'mode/padding'
            String modePadding = transformation.substring(endIndex + 1);
            endIndex = modePadding.indexOf("/");
            if (endIndex == -1) {
                // if no padding is specified
                throw new NoSuchAlgorithmException(
                        "Badly formed transformation: only 'algorithm' "
                                + "or 'algorithm/mode/padding' allowed.");
            }

            // get 'mode'
            modeName = modePadding.substring(0, endIndex);

            // get 'padding'
            paddingName = modePadding.substring(endIndex + 1);

            // if even more information is provided, transformation is invalid
            if (paddingName.contains("/")) {
                throw new NoSuchAlgorithmException(
                        "Badly formed transformation: only 'algorithm' "
                                + "or 'algorithm/mode/padding' allowed.");
            }
        }

        BlockCipher result = (BlockCipher) getInstance(blockCiphers, algName);
        if (modeName != null)
            result.setMode(modeName);
        if (paddingName != null)
            result.setPadding(paddingName);

        return result;
    }

    /**
     * @return an instance of the default mode (CBC)
     */
    protected static Mode getMode() {
        return new CBC();
    }

    /**
     * Return an instance of the specified mode.
     *
     * @param modeName the name of the mode
     * @return a new {@link Mode} object implementing the chosen algorithm
     * @throws de.flexiprovider.api.exceptions.NoSuchModeException if the mode cannot be found.
     */
    protected static Mode getMode(String modeName)
            throws NoSuchModeException {
        try {
            return (Mode) getInstance(modes, modeName);
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchModeException(e.getMessage());
        }
    }

    /**
     * @return an instance of the default padding scheme (PKCS5Padding)
     */
    protected static PaddingScheme getPaddingScheme() {
        return new PKCS5Padding();
    }

    /**
     * Return an instance of the specified padding scheme.
     *
     * @param paddingName the name of the padding scheme
     * @return a new {@link PaddingScheme} object implementing the chosen
     * algorithm
     * @throws de.flexiprovider.api.exceptions.NoSuchPaddingException if the padding scheme cannot be found.
     */
    protected static PaddingScheme getPaddingScheme(String paddingName)
            throws NoSuchPaddingException {
        try {
            return (PaddingScheme) getInstance(paddingSchemes, paddingName);
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchPaddingException(e.getMessage());
        }
    }

    /**
     * Return an instance of the specified message authentication code (MAC).
     *
     * @param algName the name of the MAC
     * @return a new {@link Mac} object implementing the chosen algorithm
     * @throws de.flexiprovider.api.exceptions.NoSuchAlgorithmException if the MAC cannot be found.
     */
    public static Mac getMAC(String algName)
            throws NoSuchAlgorithmException {
        return (Mac) getInstance(macs, algName);
    }

    /**
     * Return an instance of the specified message digest.
     *
     * @param algName the name of the message digest
     * @return a new {@link MessageDigest} object implementing the chosen
     * algorithm
     * @throws de.flexiprovider.api.exceptions.NoSuchAlgorithmException if the message digest cannot be found.
     */
    public static MessageDigest getMessageDigest(String algName)
            throws NoSuchAlgorithmException {
        return (MessageDigest) getInstance(messageDigests, algName);
    }

    /**
     * Return an instance of the specified source of randomness.
     *
     * @param algName the name of the source of randomness
     * @return a new {@link SecureRandom} object implementing the chosen
     * algorithm
     * @throws de.flexiprovider.api.exceptions.NoSuchAlgorithmException if the source of randomness cannot be found.
     */
    public static SecureRandom getSecureRandom(String algName)
            throws NoSuchAlgorithmException {
        return (SecureRandom) getInstance(secureRandoms, algName);
    }

    /**
     * @return the default secure random
     * @throws RuntimeException if the default secure random cannot be instantiated.
     */
    public static SecureRandom getSecureRandom() {
        return new DefaultPRNG();
    }

    /**
     * Return the algorithm parameter specification class corresponding to the
     * given algorithm name.
     *
     * @param algName the algorithm name
     * @return the corresponding algorithm parameter specification class
     * @throws de.flexiprovider.api.exceptions.NoSuchAlgorithmException if the parameters class cannot be found.
     */
    public static Class getAlgParamSpecClass(String algName)
            throws NoSuchAlgorithmException {
        Class algorithmClass = (Class) resolveAlias(algParamSpecs, algName);
        if (algorithmClass == null) {
            throw new NoSuchAlgorithmException(algName);
        }
        return algorithmClass;
    }

    /**
     * Return an instance of the algorithm parameter specification class
     * corresponding to the given algorithm name.
     *
     * @param paramName the name of the standard algorithm parameters
     * @return the standard algorithm parameters
     * @throws de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException if the parameters cannot be found.
     */
    public static AlgorithmParameterSpec getAlgParamSpec(String paramName)
            throws InvalidAlgorithmParameterException {
        try {
            return (AlgorithmParameterSpec) getInstance(algParamSpecs,
                    paramName);
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidAlgorithmParameterException(
                    "Unknown parameters: '" + paramName + "'.");
        }
    }

    /**
     * Register a list of standardized algorithm parameters for the given list
     * of algorithms. Additionally, each parameter set has to be registered
     * separately using the {@link #add(int, Class, String)} or
     * {@link #add(int, Class, String[])} method with the
     * {@link #ALG_PARAM_SPEC} type.
     *
     * @param algNames   the names of the algorithms
     * @param paramNames the names of the standardized algorithm parameters suitable
     *                   for the specified algorithm
     */
    public static void addStandardAlgParams(String[] algNames,
                                                  String[] paramNames) {

        if ((algNames == null) || (paramNames == null)) {
            return;
        }

        // build vector containing the parameter set names
        Vector params = new Vector(paramNames.length);
        for (int i = 0; i < paramNames.length; i++) {
            params.addElement(paramNames[i]);
        }

        // register first name
        standardAlgParams.put(algNames[0], params);

        // register additional names (aliases)
        for (int i = 1; i < algNames.length; i++) {
            standardAlgParams.put(algNames[i], algNames[0]);
        }
    }
    public static AlgorithmParameters getAlgParams(String algName)
            throws NoSuchAlgorithmException {
        return (AlgorithmParameters) getInstance(algParams, algName);
    }
    public static SecretKeyFactory getSecretKeyFactory(String algName)
            throws NoSuchAlgorithmException {
        return (SecretKeyFactory) getInstance(secretKeyFactories, algName);
    }

    private static Hashtable getHashtable(int type) {
        if (type > hashtables.length) {
            return null;
        }
        return hashtables[type];
    }

    private static Object resolveAlias(Hashtable table, String name) {
        Object value = name;
        do {
            String algName = (String) value;
            value = table.get(algName);
        } while (value != null && (value instanceof String));
        return value;
    }

    /**
     * Try to find an algorithm with the specified name inside the corresponding
     * hashtable and return an instance of the algorithm.
     *
     * @param table hashtable containing the algorithm
     * @param name  the algorithm name
     * @return a new object implementing the chosen algorithm, or <tt>null</tt>
     * if the algorithm name is <tt>null</tt>
     * @throws de.flexiprovider.api.exceptions.NoSuchAlgorithmException if the algorithm cannot be found.
     */
    private static Object getInstance(Hashtable table, String name)
            throws NoSuchAlgorithmException {
        if (name == null) {
            return null;
        }
        Class algClass = (Class) resolveAlias(table, name);
        if (algClass == null) {
            throw new NoSuchAlgorithmException(name);
        }
        try {
            return algClass.newInstance();
        } catch (InstantiationException e) {
            throw new RegistrationException("Instantiation exception: "
                    + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RegistrationException("Illegal access exception: "
                    + e.getMessage());
        }
    }

}
