package de.flexiprovider.common.ies;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.flexiprovider.api.AsymmetricHybridCipher;
import de.flexiprovider.api.BlockCipher;
import de.flexiprovider.api.KeyAgreement;
import de.flexiprovider.api.KeyDerivation;
import de.flexiprovider.api.Mac;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.core.kdf.KDF2;
import de.flexiprovider.core.kdf.KDFParameterSpec;
import de.flexiprovider.core.mac.HMac;
import de.flexiprovider.core.mac.HMacKey;
import de.flexiprovider.core.rijndael.RijndaelKey;


public abstract class IES extends AsymmetricHybridCipher {


    protected SecureRandom random;
    protected AlgorithmParameterSpec keyParams;
    // the public key used for encryption
    private PublicKey pubKey;
    // the private key used for decryption
    private PrivateKey privKey;
    // the IES parameters
    private IESParameterSpec iesParams;
    // flag indicating the IES mode (internal (XOR) or symmetric cipher)
    private boolean isInternal = false;
    // the name of the symmetric cipher
    private String symCipherName;
    // the instantiated symmetric cipher for symmetric cipher mode
    private BlockCipher symCipher;
    // the length of the symmetric cipher key
    private int symKeyLength;

    // MAC function name
    private String macName;

    // the MAC instance
    private Mac mac;

    // the MAC length
    private int macLen;

    // the MAC encoding parameters
    private byte[] macEncParams;

    // the shared data for the key agreement
    private byte[] sharedData;

    // the key agreement module
    private KeyAgreement kag;

    // the key derivation function
    private KeyDerivation kdf;

    // the ephemeral public key
    private PublicKey ephPubKey;

    // the ephemeral private key
    private PrivateKey ephPrivKey;

    // the size of the encoded ephemeral public key
    private int encEphPubKeySize;

    // the cipher mode we are in (Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE)
    private int opMode;

    // buffer to store the input data
    private ByteArrayOutputStream buf;


    protected IES() {
        buf = new ByteArrayOutputStream();
        kag = getKeyAgreement();
        kdf = new KDF2();
    }

    public byte[] update(byte[] input, int inOff, int inLen) {
        if (input != null) {
            buf.write(input, inOff, inLen);
        }
        return new byte[0];
    }

    public byte[] doFinal(byte[] input, int inOff, int inLen)
            throws BadPaddingException {
        update(input, inOff, inLen);
        byte[] data = buf.toByteArray();
        buf.reset();
        if (opMode == ENCRYPT_MODE) {
            return messageEncrypt(data);
        } else if (opMode == DECRYPT_MODE) {
            return messageDecrypt(data);
        }
        return null;
    }

    protected void initCipherEncrypt(Key key, AlgorithmParameterSpec params,
                                     SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {

        pubKey = checkPubKey(key);
        iesParams = checkParameters(params);

        // obtain the ephemeral key pair
        KeyPair ephKeyPair = iesParams.getEphKeyPair();
        // if it is null
        if (ephKeyPair == null) {
            // generate it
            ephKeyPair = generateEphKeyPair();
            // and assign the keys
            ephPubKey = ephKeyPair.getPublic();
            ephPrivKey = ephKeyPair.getPrivate();
        } else {
            // check and assign the keys contained in the key pair
            ephPubKey = checkPubKey(ephKeyPair.getPublic());
            ephPrivKey = checkPrivKey(ephKeyPair.getPrivate());
        }
        // compute the size of the encoded ephemeral public key
        encEphPubKeySize = getEncEphPubKeySize();

        symCipherName = iesParams.getSymCipherName();
        sharedData = iesParams.getSharedInfo();

        isInternal = symCipherName == null;
        if (!isInternal) {
            initSymCipher();
        }
        initMAC();

        this.random = (random != null) ? random : Registry.getSecureRandom();
        opMode = ENCRYPT_MODE;
    }

    protected void initCipherDecrypt(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        privKey = checkPrivKey(key);
        iesParams = checkParameters(params);

        symCipherName = iesParams.getSymCipherName();
        sharedData = iesParams.getSharedInfo();

        isInternal = symCipherName == null;
        if (!isInternal) {
            initSymCipher();
        }

        encEphPubKeySize = getEncEphPubKeySize();
        initMAC();

        opMode = DECRYPT_MODE;
    }

    protected int decryptOutputSize() {
        // TODO integrate correct computation
        return 0;
    }

    protected int encryptOutputSize() {
        // TODO integrate correct computation
        return 0;
    }

    protected byte[] messageEncrypt(byte[] input) throws BadPaddingException {

        // generate key stream
        byte[] keyStream = generateKeyStream(ephPrivKey, pubKey, input.length);

        // symmetrically encrypt the plaintext
        byte[] cText;
        try {
            cText = processMessage(keyStream, input);
        } catch (Exception e) {
            throw new BadPaddingException(e.getMessage());
        }

        // compute MAC tag
        byte[] macTag = generateMAC(keyStream, cText);

        // pack output
        return packCiphertext(cText, macTag);
    }

    protected byte[] messageDecrypt(byte[] input) throws BadPaddingException {

        // unpack the IES ciphertext ...
        byte[][] cm = unpackCiphertext(input);
        // ... and obtain the symmetric ciphertext ...
        byte[] cText = cm[0];
        // ... and the MAC tag
        byte[] macTag = cm[1];

        // generate key stream
        byte[] keyStream = generateKeyStream(privKey, ephPubKey, cText.length);

        // compute MAC tag
        byte[] newMacTag = generateMAC(keyStream, cText);
        // check if MAC tags match
        if (!ByteUtils.equals(macTag, newMacTag)) {
            throw new BadPaddingException("invalid ciphertext");
        }

        try {
            // symmetrically decrypt the ciphertext and return
            return processMessage(keyStream, cText);
        } catch (IllegalBlockSizeException e) {
            throw new BadPaddingException("invalid ciphertext");
        }
    }


    private IESParameterSpec checkParameters(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {

        // if the parameters are null
        if (params == null) {
            // return the default parameters
            return new IESParameterSpec();
        }

        // check if the parameters are of the correct type
        if (!(params instanceof IESParameterSpec)) {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }

        // return the checked parameters
        return (IESParameterSpec) params;
    }


    private void initSymCipher() {
        try {
            symCipher = Registry.getBlockCipher(symCipherName);
        } catch (Exception ex) {
            throw new RuntimeException("IES Init (checkSymCipher): "
                    + ex.getMessage());
        }
        symKeyLength = iesParams.getSymKeyLength();
    }


    private void initMAC() {
        macName = iesParams.getMacName();
        try {
            mac = new HMac.SHA1();
        } catch (Exception ex) {
            throw new RuntimeException("IES Init (checkMac): "
                    + ex.getMessage());
        }
        macLen = mac.getMacLength();
        macEncParams = iesParams.getMacEncParam();
    }


    private byte[] generateKeyStream(PrivateKey privKey, PublicKey pubKey,
                                     int len) {

        int tLen = (isInternal ? len : symKeyLength) + macLen;

        try {
            // use key agreement to obtain secret key
            kag.init(privKey, null, random);
            byte[] secretKey = kag.doPhase(pubKey, true).getEncoded();

            // generate key stream with the key derivation function
            KDFParameterSpec kdfParams = new KDFParameterSpec(sharedData);
            kdf.init(secretKey, kdfParams);
            byte[] keyStream = kdf.deriveKey(tLen);

            if (!isInternal) {
                byte[] symKeyData = new byte[symKeyLength];
                System.arraycopy(keyStream, 0, symKeyData, 0, symKeyLength);
                SecretKeySpec keySpec = new SecretKeySpec(symKeyData,
                        symCipherName);
                SecretKey symKey = new RijndaelKey(keySpec.getEncoded());
                if (opMode == ENCRYPT_MODE) {
                    symCipher.initEncrypt(symKey);
                } else if (opMode == DECRYPT_MODE) {
                    symCipher.initDecrypt(symKey);
                }
            }

            return keyStream;

        } catch (Exception e) {
            throw new RuntimeException("internal error");
        }
    }


    private byte[] processMessage(byte[] keyStream, byte[] message)
            throws IllegalBlockSizeException, BadPaddingException {

        byte[] result;
        if (isInternal) {
            result = new byte[message.length];
            for (int i = message.length - 1; i >= 0; i--) {
                result[i] = (byte) (message[i] ^ keyStream[i]);
            }
        } else {
            result = symCipher.doFinal(message);
        }

        return result;
    }


    private byte[] generateMAC(byte[] keyStream, byte[] cText) {

        int macKeyLen = isInternal ? cText.length : symKeyLength;

        byte[] macKeyStream = new byte[keyStream.length - macKeyLen];
        System.arraycopy(keyStream, macKeyLen, macKeyStream, 0,
                macKeyStream.length);

        // initialize the MAC function with a MAC key
        SecretKeySpec macKeySpec = new SecretKeySpec(macKeyStream, macName);
        SecretKey macKey;
        macKey = new HMacKey(macKeySpec.getEncoded());
        mac.init(macKey);

        macKeyLen = cText.length;
        byte[] macInput = cText;
        // if MAC encoding parameter is specified, append it to the
        // encrypted message and call the function
        if (macEncParams != null) {
            macInput = new byte[macKeyLen + macEncParams.length];
            System.arraycopy(cText, 0, macInput, 0, macKeyLen);
            System.arraycopy(macEncParams, 0, macInput, macKeyLen,
                    macEncParams.length);
        }

        return mac.doFinal(macInput);

    }


    private byte[] packCiphertext(byte[] cText, byte[] macTag) {
        byte[] result = new byte[encEphPubKeySize + cText.length + macLen];
        byte[] encEphPubKey = encodeEphPubKey(ephPubKey);

        System.arraycopy(encEphPubKey, 0, result, 0, encEphPubKeySize);
        System.arraycopy(cText, 0, result, encEphPubKeySize, cText.length);
        System.arraycopy(macTag, 0, result, encEphPubKeySize + cText.length,
                macLen);

        return result;
    }


    private byte[][] unpackCiphertext(byte[] input) {
        int cLen = input.length - encEphPubKeySize - macLen;
        byte[] encEphPubKey = new byte[encEphPubKeySize];
        byte[] cText = new byte[cLen];
        byte[] macTag = new byte[macLen];

        System.arraycopy(input, 0, encEphPubKey, 0, encEphPubKeySize);
        System.arraycopy(input, encEphPubKeySize, cText, 0, cLen);
        System.arraycopy(input, encEphPubKeySize + cLen, macTag, 0, macLen);

        ephPubKey = decodeEphPubKey(encEphPubKey);

        return new byte[][]{cText, macTag};
    }


    protected abstract PublicKey checkPubKey(Key key)
            throws InvalidKeyException;


    protected abstract PrivateKey checkPrivKey(Key key)
            throws InvalidKeyException;


    protected abstract KeyAgreement getKeyAgreement();


    protected abstract KeyPair generateEphKeyPair();


    protected abstract byte[] encodeEphPubKey(PublicKey ephPubKey);


    protected abstract int getEncEphPubKeySize();


    protected abstract PublicKey decodeEphPubKey(byte[] encEphPubKey);

}
