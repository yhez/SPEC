package de.flexiprovider.common.ies;

import java.security.InvalidParameterException;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.common.util.ByteUtils;

public class IESParameterSpec implements AlgorithmParameterSpec {


    public static final String DEFAULT_SYM_CIPHER = "AES128_CBC";


    public static final String DEFAULT_MAC = "HmacSHA1";

    // the ephemeral key pair
    private KeyPair ephKeyPair;

    // the name of the desired symmetric cipher
    private String symCipherName;

    // the key size for the symmetric cipher
    private int mSymKeyLength;

    // the name of the desired MAC
    private String macName;

    // the encoding parameter used for the MAC
    private byte[] macEncParam;

    // the shared data used for the key derivation function
    private byte[] sharedInfo;

    public IESParameterSpec() {
        this(null, DEFAULT_SYM_CIPHER, DEFAULT_MAC, null, null);
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


    protected int getSymKeyLength() {
        return mSymKeyLength;
    }


    public String getMacName() {
        return macName;
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
            mSymKeyLength = 0;
        } else if (symCipherName.equals("AES128_CBC")) {
            this.symCipherName = symCipherName;
            mSymKeyLength = 16;
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
        } else if ((macName.equals("HmacSHA1"))
                || (macName.equals("HmacSHA256"))
                || (macName.equals("HmacSHA384"))
                || (macName.equals("HmacSHA512"))
                || (macName.equals("HmacRIPEMD160"))) {
            this.macName = macName;
        } else {
            throw new InvalidParameterException("Unsupported MAC function: '"
                    + macName + "'.");
        }
    }

}
