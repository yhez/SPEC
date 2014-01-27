package de.flexiprovider.common.ies;

import de.flexiprovider.api.exceptions.InvalidParameterException;
import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.util.ByteUtils;

public class IESParameterSpec implements AlgorithmParameterSpec {


    public static final String DEFAULT_SYM_CIPHER = "AES128_CBC";


    public static final String DEFAULT_MAC = "HmacSHA1";

    // the ephemeral key pair
    private KeyPair ephKeyPair;

    // the name of the desired symmetric cipher
    private String symCipherName;

    // the name of the key factory for the symmetric cipher
    private String mSymKFName;

    // the key size for the symmetric cipher
    private int mSymKeyLength;

    // the name of the desired MAC
    private String macName;

    // the name of the key factory for the MAC
    private String mMacKFName;

    // the encoding parameter used for the MAC
    private byte[] macEncParam;

    // the shared data used for the key derivation function
    private byte[] sharedInfo;

    public IESParameterSpec() {
        this(null, DEFAULT_SYM_CIPHER, DEFAULT_MAC, null, null);
    }


    public IESParameterSpec(String symCipherName, String macName,
                            byte[] macEncParam, byte[] sharedInfo)
            throws InvalidParameterException {
        this(null, symCipherName, macName, macEncParam, sharedInfo);
    }

    public IESParameterSpec(KeyPair ephKeyPair, String symCipherName,
                            String macName, byte[] macEncParam, byte[] sharedInfo)
            throws InvalidParameterException {

        this.ephKeyPair = ephKeyPair;
        setSymCipher(symCipherName);
        setMac(macName);
        this.macEncParam = ByteUtils.clone(macEncParam);
        this.sharedInfo = ByteUtils.clone(sharedInfo);
    }

    public KeyPair getEphKeyPair() {
        return ephKeyPair;
    }


    public String getSymCipherName() {
        return symCipherName;
    }


    protected String getSymKFName() {
        return mSymKFName;
    }


    protected int getSymKeyLength() {
        return mSymKeyLength;
    }


    public String getMacName() {
        return macName;
    }


    protected String getMacKFName() {
        return mMacKFName;
    }


    public byte[] getMacEncParam() {
        return ByteUtils.clone(macEncParam);
    }


    public byte[] getSharedInfo() {
        return ByteUtils.clone(sharedInfo);
    }

    private void setSymCipher(String symCipherName)
            throws InvalidParameterException {

        if (symCipherName == null || symCipherName.equals("")) {
            symCipherName = DEFAULT_SYM_CIPHER;
        }

        if (symCipherName.equals("internal")) {
            // internal cipher (one-time pad)
            this.symCipherName = null;
            mSymKFName = null;
            mSymKeyLength = 0;
        } else if (symCipherName.equals("DESede_CBC")) {
            this.symCipherName = symCipherName;
            mSymKFName = "DESede";
            mSymKeyLength = 24;
        } else if (symCipherName.equals("AES128_CBC")) {
            this.symCipherName = symCipherName;
            mSymKFName = "AES";
            mSymKeyLength = 16;
        } else if (symCipherName.equals("AES192_CBC")) {
            this.symCipherName = symCipherName;
            mSymKFName = "AES";
            mSymKeyLength = 24;
        } else if (symCipherName.equals("AES256_CBC")) {
            this.symCipherName = symCipherName;
            mSymKFName = "AES";
            mSymKeyLength = 32;
        } else {
            throw new InvalidParameterException(
                    "Unsupported symmetric cipher algorithm: '" + symCipherName
                            + "'.");
        }
    }

    private void setMac(String macName) throws InvalidParameterException {
        // if no MAC function is specified, use the default one (HmacSHA1)
        if (macName == null || macName.equals("")) {
            this.macName = DEFAULT_MAC;
            mMacKFName = "Hmac";
        } else if ((macName.equals("HmacSHA1"))
                || (macName.equals("HmacSHA256"))
                || (macName.equals("HmacSHA384"))
                || (macName.equals("HmacSHA512"))
                || (macName.equals("HmacRIPEMD160"))) {
            this.macName = macName;
            mMacKFName = "Hmac";
        } else {
            throw new InvalidParameterException("Unsupported MAC function: '"
                    + macName + "'.");
        }
    }

}
