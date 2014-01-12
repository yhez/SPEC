package specular.systems;


import android.app.Activity;
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
import java.util.Random;

import javax.crypto.Cipher;

import de.flexiprovider.common.ies.IESParameterSpec;
import de.flexiprovider.core.FlexiCoreProvider;
import de.flexiprovider.ec.FlexiECProvider;
import de.flexiprovider.ec.keys.ECPrivateKey;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.CurveRegistry;
import de.flexiprovider.pki.PKCS8EncodedKeySpec;
import de.flexiprovider.pki.X509EncodedKeySpec;

public class CryptMethods {
    public static boolean doneCreatingKeys = false;
    public static String encryptedMsgToSend;
    private static PrivateKey mPtK;
    private static String myName = null, myEmail = null, myPublicKey = null;
    private static PrivateKey tmpPtK = null;
    private static byte[] tmpPrivateKey;
    private static byte[] myPrivateKey;
    private static String tmpPublicKey = null;
    private static boolean notInit = true;
    private static boolean lock = true;

    public static byte[] getPrivateToSave() {
        return myPrivateKey;
    }

    public static void deleteKeys() {
        if (lock) {
            lock = false;
            if (mPtK != null) {
                ((ECPrivateKey) mPtK).getS().bigInt.delete();
                mPtK = null;
            }
            if (myPrivateKey != null) {
                for (int a = 0; a < myPrivateKey.length; a++) {
                    myPrivateKey[a] = (byte) (System.currentTimeMillis() * new Random().nextLong());
                }
                Log.d("to prevent java from skipping it", "" + myPrivateKey[new Random().nextInt(myPrivateKey.length)]);
                myPrivateKey = null;
            }
            MessageFormat.decryptedMsg = null;
            LightMessage.decryptedLightMsg = null;
            lock = true;
        }
    }

    public static boolean setPrivate(byte[] p) {
        if (p != null) {
            mPtK = formatPrivate(p);
            if (mPtK != null) {
                myPrivateKey = p;
                return true;
            }
        }
        return false;
    }

    public static void setDetails(String name, String email) {
        if (name != null)
            myName = name;
        if (email != null)
            myEmail = email;
    }

    public static void moveKeysFromTmp() {
        myPublicKey = tmpPublicKey;
        tmpPublicKey = null;
        mPtK = tmpPtK;
        tmpPtK = null;
        myPrivateKey = tmpPrivateKey;
        tmpPrivateKey = null;
    }

    public static String[] getMyDetails(Activity a) {
        if (myName == null || myEmail == null || myPublicKey == null)
            FilesManagement.getMyDetails(a);
        return new String[]{myName, myEmail, myPublicKey};
    }

    public static String getPublic() {
        return myPublicKey;
    }

    public static void setPublic(String p) {
        myPublicKey = p;
    }

    public static boolean privateExist() {
        return mPtK != null;
    }

    public static boolean publicExist() {
        return myPublicKey != null;
    }

    private static void addProviders() {
        Security.addProvider(new FlexiECProvider());
        Security.addProvider(new FlexiCoreProvider());
    }

    public static void createKeys() {
        if (notInit) {
            addProviders();
            notInit = false;
        }
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("ECIES", "FlexiEC");
            CurveParams ecParams = new CurveRegistry.BrainpoolP512r1();
            try {
                //todo add random data from camera
                kpg.initialize(ecParams, new SecureRandom());
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            KeyPair keypair;
            keypair = kpg.generateKeyPair();
            if (!doneCreatingKeys) {
                tmpPublicKey = Visual.bin2hex(keypair.getPublic().getEncoded());
                tmpPtK = keypair.getPrivate();
                tmpPrivateKey = tmpPtK.getEncoded();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public static void decrypt(String encryptedMessage) {
        if (encryptedMessage == null) {
            Log.d("null", "null message");
            return;
        }
        if (!privateExist()) {
            Log.e("no private", "message");
            return;
        }
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
            byte[] decryptedBytes = cipher.doFinal(rawMsg);
            MessageFormat.decryptedMsg = new MessageFormat(decryptedBytes);
            if (MessageFormat.decryptedMsg.getPublicKey() == null) {
                MessageFormat.decryptedMsg = null;
                LightMessage.decryptedLightMsg = new LightMessage(decryptedBytes);
            } else
                LightMessage.decryptedLightMsg = null;
            StaticVariables.encrypted_msg_size = encryptedMessage.length();
            StaticVariables.orig_msg_size = decryptedBytes.length;
        } catch (Exception e) {
            MessageFormat.decryptedMsg = null;
            LightMessage.decryptedLightMsg = null;
            e.printStackTrace();
        }
    }

    public static String encrypt(final byte[] msg, final byte[] light, final String friendPublicKey) {
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
            if (light != null)
                StaticVariables.encryptedLight = Visual.bin2hex(cipher.doFinal(light));
            encryptedMsgToSend = Visual.bin2hex(cipher.doFinal(msg));
            return encryptedMsgToSend;
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }

    public static byte[] encrypyt(byte[] b,String publicKey){
        if (notInit) {
            addProviders();
            notInit = false;
        }
        try {
            PublicKey frndPbK = KeyFactory.getInstance("ECIES", "FlexiEC")
                    .generatePublic(new X509EncodedKeySpec(Visual.hex2bin(publicKey)));
            Cipher cipher = Cipher.getInstance("ECIES", "FlexiEC");
            IESParameterSpec iesParams = new IESParameterSpec("AES128_CBC",
                    "HmacSHA1", null, null);
            cipher.init(Cipher.ENCRYPT_MODE, frndPbK, iesParams);
            return cipher.doFinal(b);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }
    private static PrivateKey formatPrivate(byte[] p) {
        if (notInit) {
            addProviders();
            notInit = false;
        }
        try {
            return KeyFactory.getInstance("ECIES", "FlexiEC").generatePrivate(
                    new PKCS8EncodedKeySpec(p));

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPublicTmp() {
        return tmpPublicKey;
    }

    public static byte[] getPrivateTmp() {
        return tmpPrivateKey;
    }
}
