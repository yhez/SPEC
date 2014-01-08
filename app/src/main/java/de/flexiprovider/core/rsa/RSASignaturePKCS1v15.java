package de.flexiprovider.core.rsa;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.Signature;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.core.md.NullDigest;
import de.flexiprovider.core.rsa.interfaces.RSAPrivateKey;
import de.flexiprovider.core.rsa.interfaces.RSAPublicKey;
import de.flexiprovider.pki.AlgorithmIdentifier;


public abstract class RSASignaturePKCS1v15 extends Signature {

    private MessageDigest md;

    private AlgorithmIdentifier aid;

    private RSAPublicKey pubKey;

    private RSAPrivateKey privKey;

    private int modBitLen;

    public static class MD5 extends RSASignaturePKCS1v15 {

        public static final String OID = "1.2.840.113549.1.1.4";
        public static final String ALTERNATIVE_OID = "1.3.14.3.2.25";

        public MD5() {
            super(de.flexiprovider.core.md.MD5.OID,
                    new de.flexiprovider.core.md.MD5());
        }
    }
    public static class SHA1 extends RSASignaturePKCS1v15 {

        public static final String OID = "1.2.840.113549.1.1.5";

        public static final String ALTERNATIVE_OID = "1.3.14.3.2.29";

        public SHA1() {
            super(de.flexiprovider.core.md.SHA1.OID,
                    new de.flexiprovider.core.md.SHA1());
        }
    }
    public static class SHA224 extends RSASignaturePKCS1v15 {

        public static final String OID = "1.2.840.113549.1.1.14";

        public SHA224() {
            super(de.flexiprovider.core.md.SHA224.OID,
                    new de.flexiprovider.core.md.SHA224());
        }
    }
    public static class SHA256 extends RSASignaturePKCS1v15 {

        public static final String OID = "1.2.840.113549.1.1.11";

        public SHA256() {
            super(de.flexiprovider.core.md.SHA256.OID,
                    new de.flexiprovider.core.md.SHA256());
        }
    }
    public static class SHA384 extends RSASignaturePKCS1v15 {

        public static final String OID = "1.2.840.113549.1.1.12";

        public SHA384() {
            super(de.flexiprovider.core.md.SHA384.OID,
                    new de.flexiprovider.core.md.SHA384());
        }
    }
    public static class SHA512 extends RSASignaturePKCS1v15 {

        public static final String OID = "1.2.840.113549.1.1.13";

        public SHA512() {
            super(de.flexiprovider.core.md.SHA512.OID,
                    new de.flexiprovider.core.md.SHA512());
        }
    }

    public static class RIPEMD160 extends RSASignaturePKCS1v15 {

        public static final String OID = "1.3.36.3.3.1.2";

        public RIPEMD160() {
            super(de.flexiprovider.core.md.RIPEMD160.OID,
                    new de.flexiprovider.core.md.RIPEMD160());
        }
    }

    public static class RawMD5 extends RSASignaturePKCS1v15 {
        public RawMD5() {
            super(de.flexiprovider.core.md.MD5.OID, new NullDigest());
        }
    }

    public static class RawSHA1 extends RSASignaturePKCS1v15 {
        public RawSHA1() {
            super(de.flexiprovider.core.md.SHA1.OID, new NullDigest());
        }
    }
    public static class RawSHA224 extends RSASignaturePKCS1v15 {
        public RawSHA224() {
            super(de.flexiprovider.core.md.SHA224.OID, new NullDigest());
        }
    }
    public static class RawSHA256 extends RSASignaturePKCS1v15 {
        public RawSHA256() {
            super(de.flexiprovider.core.md.SHA256.OID, new NullDigest());
        }
    }
    public static class RawSHA384 extends RSASignaturePKCS1v15 {
        public RawSHA384() {
            super(de.flexiprovider.core.md.SHA384.OID, new NullDigest());
        }
    }
    public static class RawSHA512 extends RSASignaturePKCS1v15 {
        public RawSHA512() {
            super(de.flexiprovider.core.md.SHA512.OID, new NullDigest());
        }
    }
    public static class RawRIPEMD160 extends RSASignaturePKCS1v15 {
        public RawRIPEMD160() {
            super(de.flexiprovider.core.md.RIPEMD160.OID, new NullDigest());
        }
    }
    protected RSASignaturePKCS1v15(String oidStr, MessageDigest md) {
        try {
            aid = new AlgorithmIdentifier(new ASN1ObjectIdentifier(oidStr),
                    new ASN1Null());
        } catch (ASN1Exception ae) {
            throw new RuntimeException("Internal error in CoDec.");
        }
        this.md = md;
    }
    public void initSign(PrivateKey privateKey, SecureRandom secureRandom)
            throws InvalidKeyException {
        if (!(privateKey instanceof RSAPrivateKey)) {
            throw new InvalidKeyException("key is not a RSAPrivateKey.");
        }

        privKey = (RSAPrivateKey) privateKey;
        modBitLen = privKey.getN().bitLength();
    }
    public void initVerify(PublicKey publicKey) throws InvalidKeyException {
        if (!(publicKey instanceof RSAPublicKey)) {
            throw new InvalidKeyException("key is not a RSAPublicKey.");
        }

        pubKey = (RSAPublicKey) publicKey;
        modBitLen = pubKey.getN().bitLength();
    }
    public void setParameters(AlgorithmParameterSpec params) {
    }
    public void update(byte[] b, int offset, int length) {
        md.update(b, offset, length);
    }
    public void update(byte b) {
        md.update(b);
    }
    public byte[] sign() throws SignatureException {
        FlexiBigInt s, m;
        int k = (modBitLen + 7) >> 3;
        byte[] EM;
        try {
            EM = PKCS1Operations.EMSA_PKCS1_v1_5_ENCODE(md.digest(), k, aid);
        } catch (PKCS1Exception pkcs1e) {
            throw new SignatureException(pkcs1e.getMessage());
        }
        m = PKCS1Operations.OS2IP(EM);
        try {
            s = PKCS1Operations.RSADP(privKey, m);
        } catch (PKCS1Exception pkcs1e) {
            throw new SignatureException("encoding error.");
        }
        try {
            return PKCS1Operations.I2OSP(s, k);
        } catch (PKCS1Exception pkcs1e) {
            throw new SignatureException("internal error.");
        }
    }
    public boolean verify(byte[] signature) {
        FlexiBigInt m, s;
        byte[] EM, EM2;
        int k = (modBitLen + 7) >> 3;
        if (signature.length != k) {
            return false;
        }
        s = PKCS1Operations.OS2IP(signature);

        try {
            m = PKCS1Operations.RSAEP(pubKey, s);
            EM = PKCS1Operations.I2OSP(m, k);
            EM2 = PKCS1Operations.EMSA_PKCS1_v1_5_ENCODE(md.digest(), k, aid);
        } catch (PKCS1Exception pkcs1e) {
            return false;
        }
        return ByteUtils.equals(EM, EM2);
    }

}
