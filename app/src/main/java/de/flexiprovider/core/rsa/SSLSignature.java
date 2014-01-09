package de.flexiprovider.core.rsa;

import de.flexiprovider.api.AsymmetricBlockCipher;
import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.Signature;
import de.flexiprovider.api.exceptions.BadPaddingException;
import de.flexiprovider.api.exceptions.IllegalBlockSizeException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.core.md.MD5;
import de.flexiprovider.core.md.SHA1;

public class SSLSignature extends Signature {

    private MessageDigest mdMD5_;

    private MessageDigest mdSHA1_;

    private AsymmetricBlockCipher cipher_;

    private void initCommon() {
        mdSHA1_ = new SHA1();
        mdMD5_ = new MD5();
        cipher_ = new RSA_PKCS1_v1_5();
    }

    public void initSign(PrivateKey privateKey, SecureRandom secureRandom)
            throws InvalidKeyException {
        cipher_.initEncrypt(privateKey, secureRandom);
    }

    public void initVerify(PublicKey publicKey) throws InvalidKeyException {
        initCommon();
        cipher_.initDecrypt(publicKey);
    }

    public void setParameters(AlgorithmParameterSpec params) {
    }

    public void update(byte[] b, int offset, int length) {
        mdMD5_.update(b, offset, length);
        mdSHA1_.update(b, offset, length);
    }

    public void update(byte b) {
        mdMD5_.update(b);
        mdSHA1_.update(b);
    }

    public byte[] sign() throws SignatureException {

        byte[] out;
        byte[] shaMBytes = mdSHA1_.digest();
        byte[] mdMBytes = mdMD5_.digest();
        byte[] plainSig = new byte[16 + 20];

        System.arraycopy(mdMBytes, 0, plainSig, 0, 16);
        System.arraycopy(shaMBytes, 0, plainSig, 16, 20);

        try {
            out = cipher_.doFinal(plainSig);
            return out;
        } catch (IllegalBlockSizeException ibse) {
            throw new SignatureException(
                    "SSLSignature: failure in cipher.doFinal() (illegal block size)");
        } catch (BadPaddingException bpe) {
            throw new SignatureException(
                    "SSLSignature: failure in cipher.doFinal() (bad padding)");
        }
    }

    public boolean verify(byte[] signature) {

        byte[] shaMBytes = mdSHA1_.digest();
        byte[] mdMBytes = mdMD5_.digest();
        byte[] plain;

        try {
            plain = cipher_.doFinal(signature);

            for (int i = 0; i < 16; i++) {
                if (plain[i] != mdMBytes[i]) {
                    return false;
                }
                if (plain[16 + i] != shaMBytes[i]) {
                    return false;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (plain[16 + 16 + i] != shaMBytes[16 + i]) {
                    return false;
                }
            }

            return true;

        } catch (IllegalBlockSizeException ibse) {
            System.err.println("RSASignature: cipher.doFinal");
            ibse.printStackTrace();
        } catch (BadPaddingException bpe) {
            System.err.println("RSASignature: cipher.doFinal");
            bpe.printStackTrace();
        }

        return false;
    }

}
