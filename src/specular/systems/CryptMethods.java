package specular.systems;

import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;

import de.flexiprovider.common.ies.IESParameterSpec;
import de.flexiprovider.core.FlexiCoreProvider;
import de.flexiprovider.ec.FlexiECProvider;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.CurveRegistry;
import de.flexiprovider.pki.PKCS8EncodedKeySpec;
import de.flexiprovider.pki.X509EncodedKeySpec;

public class CryptMethods {
    public static String encryptedMsgToSend = null;
    public static PrivateKey mPtK = null;
    public static String myName = null, myEmail = null, myPublicKey = null,
            myPrivateKey = null;
    private static boolean notInit = true;
    private static Thread crypter;

    private static void addProviders() {
        Security.addProvider(new FlexiCoreProvider());
        Security.addProvider(new FlexiECProvider());
    }

    public static void createKeys() {
        if (notInit)
            addProviders();
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("ECIES", "FlexiEC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        CurveParams ecParams = new CurveRegistry.BrainpoolP512r1();
        if (kpg != null) {
            try {
                kpg.initialize(ecParams, new SecureRandom());
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
        KeyPair keypair;
        if (kpg != null) {
            keypair = kpg.generateKeyPair();
            myPublicKey = Visual.bin2hex(keypair.getPublic().getEncoded());
            myPrivateKey = Visual.bin2hex(keypair.getPrivate().getEncoded());
        }
    }

    public static String decrypt(String encryptedMessage) {
        if (notInit)
            addProviders();
        try {
            IESParameterSpec iesParams = new IESParameterSpec("AES128_CBC",
                    "HmacSHA1", null, null);
            Cipher cipher = Cipher.getInstance("ECIES", "FlexiEC");
            cipher.init(Cipher.DECRYPT_MODE, mPtK, iesParams);
            byte[] rawMsg = Visual.hex2bin(encryptedMessage);
            if (rawMsg == null)
                Log.e("null", "at visual");
            byte[] decryptedBytes = cipher.doFinal(rawMsg);
            return new String(decryptedBytes);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    public static boolean isAlive() {
        return crypter != null && crypter.isAlive();
    }

    public static void encrypt(final byte[] msg, final String friendPublicKey) {
        if (notInit)
            addProviders();
        crypter = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PublicKey frndPbK = KeyFactory.getInstance("ECIES", "FlexiEC")
                            .generatePublic(new X509EncodedKeySpec(Visual.hex2bin(friendPublicKey)));
                    Cipher cipher = Cipher.getInstance("ECIES", "FlexiEC");
                    IESParameterSpec iesParams = new IESParameterSpec("AES128_CBC",
                            "HmacSHA1", null, null);
                    cipher.init(Cipher.ENCRYPT_MODE, frndPbK, iesParams);
                    encryptedMsgToSend = Visual.bin2hex(cipher.doFinal(msg));
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });
        crypter.start();
    }

    public static boolean formatPrivate() {
        if (notInit)
            addProviders();
        try {
            mPtK = KeyFactory.getInstance("ECIES", "FlexiEC").generatePrivate(
                    new PKCS8EncodedKeySpec(Visual
                            .hex2bin(CryptMethods.myPrivateKey)));
            return true;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void signPublicQR() {

    }

    public static boolean checkSignPublicQR() {
        return false;
    }
}
