package de.flexiprovider.api;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import de.flexiprovider.api.exceptions.NoSuchModeException;
import de.flexiprovider.api.keys.Key;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.common.util.JavaSecureRandomWrapper;

public abstract class Cipher extends javax.crypto.CipherSpi {

    public static final int ENCRYPT_MODE = javax.crypto.Cipher.ENCRYPT_MODE;


    public static final int DECRYPT_MODE = javax.crypto.Cipher.DECRYPT_MODE;

    protected int opMode;

    protected final void engineInit(int opMode, java.security.Key key,
                                    java.security.SecureRandom random)
            throws java.security.InvalidKeyException {

        try {
            engineInit(opMode, key,
                    (java.security.spec.AlgorithmParameterSpec) null, random);
        } catch (java.security.InvalidAlgorithmParameterException e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }


    protected final void engineInit(int opMode, java.security.Key key,
                                    java.security.AlgorithmParameters algParams,
                                    java.security.SecureRandom random)
            throws java.security.InvalidKeyException,
            java.security.InvalidAlgorithmParameterException {

        // if algParams are not specified, initialize without them
        if (algParams == null) {
            engineInit(opMode, key, random);
            return;
        }

        java.security.spec.AlgorithmParameterSpec paramSpec;
        try {
            paramSpec = algParams.getParameterSpec(Registry
                    .getAlgParamSpecClass(algParams.getAlgorithm()));
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidAlgorithmParameterException(
                    "Unknown algorithm parameters.");
        } catch (InvalidParameterSpecException e) {
            throw new RuntimeException(
                    "Internal error: invalid parameters type.");
        }

        engineInit(opMode, key, paramSpec, random);
    }


    protected void engineInit(int opMode, java.security.Key key,
                              java.security.spec.AlgorithmParameterSpec params,
                              java.security.SecureRandom javaRand)
            throws java.security.InvalidKeyException,
            java.security.InvalidAlgorithmParameterException {

        if ((key == null) || !(key instanceof Key)) {
            throw new java.security.InvalidKeyException();
        }

        this.opMode = opMode;

        if (opMode == ENCRYPT_MODE) {
            SecureRandom flexiRand = new JavaSecureRandomWrapper(javaRand);
            initEncrypt((Key) key, params, flexiRand);

        } else if (opMode == DECRYPT_MODE) {
            initDecrypt((Key) key, params);

        }
    }


    protected final byte[] engineDoFinal(byte[] input, int inOff, int inLen)
            throws javax.crypto.IllegalBlockSizeException,
            javax.crypto.BadPaddingException {
        return doFinal(input, inOff, inLen);
    }


    protected final int engineDoFinal(byte[] input, int inOff, int inLen,
                                      byte[] output, int outOff)
            throws javax.crypto.ShortBufferException,
            javax.crypto.IllegalBlockSizeException,
            javax.crypto.BadPaddingException {
        return doFinal(input, inOff, inLen, output, outOff);
    }

    protected final int engineGetBlockSize() {
        return getBlockSize();
    }


    protected final int engineGetKeySize(java.security.Key key)
            throws java.security.InvalidKeyException {
        if (!(key instanceof Key)) {
            throw new java.security.InvalidKeyException("Unsupported key.");
        }
        return getKeySize((Key) key);
    }

    protected final byte[] engineGetIV() {
        return getIV();
    }


    protected final int engineGetOutputSize(int inLen) {
        return getOutputSize(inLen);
    }

    protected final java.security.AlgorithmParameters engineGetParameters() {

        final class JavaAlgorithmParameters extends
                java.security.AlgorithmParameters {
            JavaAlgorithmParameters(AlgorithmParameters params, String algName) {
                super(params, null, algName);
            }
        }

        String algName = getName();
        AlgorithmParameters params;
        try {
            params = Registry.getAlgParams(algName);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        JavaAlgorithmParameters algParams = new JavaAlgorithmParameters(params,
                algName);

        AlgorithmParameterSpec algParamSpec = getParameters();
        if (algParamSpec == null) {
            return null;
        }

        try {
            algParams.init(algParamSpec);
        } catch (InvalidParameterSpecException ipse) {
            throw new RuntimeException("InvalidParameterSpecException: "
                    + ipse.getMessage());
        }

        return algParams;
    }

    protected final void engineSetMode(String modeName)
            throws java.security.NoSuchAlgorithmException {
        setMode(modeName);
    }

    protected final void engineSetPadding(String paddingName)
            throws javax.crypto.NoSuchPaddingException {
        setPadding(paddingName);
    }

    protected final byte[] engineUpdate(byte[] input, int inOff, int inLen) {
        return update(input, inOff, inLen);
    }

    protected final int engineUpdate(final byte[] input, final int inOff,
                                     final int inLen, byte[] output, final int outOff)
            throws javax.crypto.ShortBufferException {
        return update(input, inOff, inLen, output, outOff);
    }

    public abstract void initEncrypt(Key key,
                                     AlgorithmParameterSpec cipherParams, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException;


    public abstract void initDecrypt(Key key,
                                     AlgorithmParameterSpec cipherParams) throws InvalidKeyException,
            InvalidAlgorithmParameterException;


    public abstract String getName();
    public abstract int getBlockSize();


    public abstract int getOutputSize(int inputLen);


    public abstract int getKeySize(Key key) throws InvalidKeyException;


    public abstract AlgorithmParameterSpec getParameters();


    public abstract byte[] getIV();

    protected abstract void setMode(String mode) throws NoSuchModeException;

    protected abstract void setPadding(String padding)
            throws NoSuchPaddingException;

    public abstract byte[] update(byte[] input, int inOff, int inLen);

    public abstract int update(byte[] input, int inOff, int inLen,
                               byte[] output, int outOff) throws ShortBufferException;


    public final byte[] doFinal(byte[] input) throws IllegalBlockSizeException,
            BadPaddingException {
        return doFinal(input, 0, input.length);
    }

    public abstract byte[] doFinal(byte[] input, int inOff, int inLen)
            throws IllegalBlockSizeException, BadPaddingException;

    public abstract int doFinal(byte[] input, int inOff, int inLen,
                                byte[] output, int outOff) throws ShortBufferException,
            IllegalBlockSizeException, BadPaddingException;

}
