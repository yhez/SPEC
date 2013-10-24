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
    private static PrivateKey mPtK = null;
    private static String myName = null, myEmail = null, myPublicKey = null,
            myPrivateKey = null;
    private static boolean notInit = true;

    public static String getPrivateToSave() {
        return myPrivateKey != null ? myPrivateKey : "the key is on nfc";
    }

    public static void deleteKeys() {
        myPrivateKey = null;
        mPtK = null;
    }

    public static boolean setPrivate(String p) {
        if (p != null && formatPrivate(p)) {
            myPrivateKey = p;
            return true;
        }
        return false;
    }

    public static void setDetails(String name, String email) {
        myName = name;
        myEmail = email;
    }

    public static String getName() {
        return myName;
    }

    public static String getPublic() {
        return myPublicKey;
    }

    public static void setPublic(String p) {
        myPublicKey = p;
    }

    public static String getEmail() {
        return myEmail;
    }

    public static boolean privateExist() {
        return myPrivateKey != null;
    }

    public static boolean publicExist() {
        return myPublicKey != null;
    }

    private static void addProviders() {
        Security.addProvider(new FlexiCoreProvider());
        Security.addProvider(new FlexiECProvider());
    }

    public static void createKeys() {
        if (notInit) {
            addProviders();
            notInit = false;
        }
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("ECIES", "FlexiEC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        if (kpg != null) {
            CurveParams ecParams = new CurveRegistry.BrainpoolP512r1();
            try {
                kpg.initialize(ecParams, new SecureRandom());
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            KeyPair keypair;
            keypair = kpg.generateKeyPair();
            myPublicKey = Visual.bin2hex(keypair.getPublic().getEncoded());
            myPrivateKey = Visual.bin2hex(keypair.getPrivate().getEncoded());
            formatPrivate(myPrivateKey);
        }

    }

    public static String decrypt(String encryptedMessage) {
        if (notInit) {
            addProviders();
            notInit = false;
        }
        try {
            IESParameterSpec iesParams = new IESParameterSpec("AES128_CBC",
                    "HmacSHA1", null, null);
            Cipher cipher = Cipher.getInstance("ECIES", "FlexiEC");
            cipher.init(Cipher.DECRYPT_MODE, mPtK, iesParams);
            byte[] rawMsg = Visual.hex2bin(encryptedMessage);
            if (rawMsg == null)
                Log.e("null", "at visual");
            Log.e("myp", myPrivateKey);
            byte[] decryptedBytes = cipher.doFinal(rawMsg);
            return new String(decryptedBytes);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    public static void encrypt(final byte[] msg, final String friendPublicKey) {
        if (notInit) {
            addProviders();
            notInit = false;
        }
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

    private static boolean formatPrivate(String p) {
        if (notInit) {
            addProviders();
            notInit = false;
        }
        try {
            mPtK = KeyFactory.getInstance("ECIES", "FlexiEC").generatePrivate(
                    new PKCS8EncodedKeySpec(Visual
                            .hex2bin(p)));
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
//TODO
 /*   public static void signPublicQR() {

    }

    public static boolean checkSignPublicQR() {
        return false;
    }*/
}
