package de.flexiprovider.core.rsa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.Signature;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.NoSuchAlgorithmException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.core.rsa.interfaces.RSAPrivateKey;
import de.flexiprovider.core.rsa.interfaces.RSAPublicKey;


public class RSASignaturePSS extends Signature {

    private PSSParameterSpec params;
    private MessageDigest md;
    private SecureRandom random;
    private int cipherBlockSize;
    private int modBits;
    private RSAPublicKey pubKey;
    private RSAPrivateKey privKey;
    private ByteArrayOutputStream baos;

    private void initCommon() throws InvalidKeyException {
        try {
            md = Registry.getMessageDigest(params.getMD());
        } catch (NoSuchAlgorithmException nsae) {
            throw new InvalidKeyException(
                    "message digest SHA1 not found (key may be valid nonetheless).");
        }
        cipherBlockSize = (modBits + 7) >> 3;
        baos = new ByteArrayOutputStream();
    }

    public void initSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {

        if (!(privateKey instanceof RSAPrivateKey)) {
            throw new InvalidKeyException(
                    "key is not an instance of RSAPrivateKey");
        }
        privKey = (RSAPrivateKey) privateKey;
        modBits = privKey.getN().bitLength();
        if (random != null) {
            this.random = random;
        } else {
            this.random = Registry.getSecureRandom();
        }
        initCommon();
    }

    public void initVerify(PublicKey publicKey) throws InvalidKeyException {
        if (publicKey instanceof RSAPublicKey) {
            pubKey = (RSAPublicKey) publicKey;
            modBits = pubKey.getN().bitLength();
        } else {
            throw new InvalidKeyException(
                    "key is not an instance of RSAPublicKey");
        }
        initCommon();
    }

    public void setParameters(AlgorithmParameterSpec params) {
    }

    public void update(byte b) {
        baos.write(b);
    }

    public void update(byte[] b, int offset, int length) {
        baos.write(b, offset, length);
    }

    private byte[] getMessage() throws IOException {
        byte[] msg = baos.toByteArray();

        baos.close();
        baos.reset();

        return msg;
    }

    public byte[] sign() throws SignatureException {
        FlexiBigInt s, m;
        byte[] EM;
        try {
            byte[] salt = new byte[params.getSaltLength()];
            random.nextBytes(salt);
            EM = PKCS1Operations.EMSA_PSS_ENCODE(getMessage(), modBits - 1, md,
                    salt);
        } catch (PKCS1Exception pkcs1e) {
            throw new SignatureException(pkcs1e.getMessage());
        } catch (IOException ioe) {
            throw new SignatureException(ioe.getMessage());
        }
        m = PKCS1Operations.OS2IP(EM);
        try {
            s = PKCS1Operations.RSADP(privKey, m);
        } catch (PKCS1Exception pkcs1e) {
            throw new SignatureException("encoding error.");
        }
        try {
            return PKCS1Operations.I2OSP(s, (modBits + 7) >> 3);
        } catch (PKCS1Exception pkcs1e) {
            throw new SignatureException("internal error.");
        }
    }

    public boolean verify(byte[] signature) {
        FlexiBigInt m, s;
        byte[] EM;
        if (signature.length != cipherBlockSize) {
            return false;
        }
        s = PKCS1Operations.OS2IP(signature);
        try {
            m = PKCS1Operations.RSAEP(pubKey, s);
            int emLen = (modBits - 1 + 7) >> 3;
            EM = PKCS1Operations.I2OSP(m, emLen);
        } catch (PKCS1Exception pkcs1e) {
            return false;
        }
        try {
            return PKCS1Operations.EMSA_PSS_VERIFY(getMessage(), EM,
                    modBits - 1, md);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }

}
