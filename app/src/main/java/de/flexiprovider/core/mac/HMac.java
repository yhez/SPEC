package de.flexiprovider.core.mac;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.Mac;
import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.keys.SecretKey;


public class HMac extends Mac {


    public static class SHA1 extends HMac {

        /**
         * The OID of HmacSHA1 (defined by RFC 3370).
         */
        public static final String OID = "1.3.6.1.5.5.8.1.2";

        /**
         * An alternative OID of HmacSHA1 (defined by PKCS #5 v2.0)
         */
        public static final String PKCS5_OID = "1.2.840.113549.2.7";

        public SHA1() {
            super(new de.flexiprovider.core.md.SHA1(), 64);
        }
    }

    private static final byte IPAD_BYTE = 0x36;

    private static final byte OPAD_BYTE = 0x5c;

    private int L, B;

    private byte[] opadKey;

    private MessageDigest md;


    HMac(MessageDigest md, int blockSize) {
        this.md = md;
        L = md.getDigestLength();
        B = blockSize;
    }

    public int getMacLength() {
        return L;
    }

    public void init(SecretKey key, AlgorithmParameterSpec params)
            throws InvalidKeyException {

        if (!(key instanceof HMacKey)) {
            throw new InvalidKeyException("unsupported type");
        }

        byte[] keyBytes = key.getEncoded();
        byte[] macKey = new byte[B];
        if (keyBytes.length <= B) {
            System.arraycopy(keyBytes, 0, macKey, 0, keyBytes.length);
        } else {
            md.update(keyBytes);
            byte[] keyDigest = md.digest();
            System.arraycopy(keyDigest, 0, macKey, 0, keyDigest.length);
        }

        // initialize ipadKey and opadKey
        byte[] ipadKey = new byte[B];
        opadKey = new byte[B];
        for (int i = B - 1; i >= 0; i--) {
            ipadKey[i] = (byte) (macKey[i] ^ IPAD_BYTE);
            opadKey[i] = (byte) (macKey[i] ^ OPAD_BYTE);
        }

        // feed the message digest with (macKey XOR ipadKey)
        md.update(ipadKey);
    }


    public void update(byte[] input, int inOff, int inLen) {
        md.update(input, inOff, inLen);
    }


    public void update(byte input) {
        md.update(input);
    }

    public byte[] doFinal() {
        byte[] hash1 = md.digest();
        md.update(opadKey);
        md.update(hash1);
        return md.digest();
    }


    public void reset() {
        md.reset();
    }

}
