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

import javax.crypto.Cipher;

import de.flexiprovider.common.ies.IESParameterSpec;
import de.flexiprovider.core.FlexiCoreProvider;
import de.flexiprovider.ec.FlexiECProvider;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.CurveRegistry;
import de.flexiprovider.pki.PKCS8EncodedKeySpec;
import de.flexiprovider.pki.X509EncodedKeySpec;

public class CryptMethods {
    public static boolean doneCreatingKeys = false;
    public static String encryptedMsgToSend = null;
    private static PrivateKey mPtK = null;
    private static String myName = null, myEmail = null, myPublicKey = null;
    private static PrivateKey tmpPtK = null;
    private static String tmpPublicKey = null;
    private static boolean notInit = true;

    public static byte[] getPrivateToSave() {
        return mPtK.getEncoded();
    }

    static void deleteKeys() {
        mPtK = null;
        MessageFormat.decryptedMsg = null;
        LightMessage.decryptedLightMsg=null;
    }

    public static boolean setPrivate(byte[] p) {
        if (p != null) {
            mPtK = formatPrivate(p);
            return mPtK != null;
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
    }

    public static String[] getMyDetails(Activity a) {
        if(myName==null||myEmail==null||myPublicKey==null)
            FilesManagement.getMyDetails(a);
        return new String[]{myName,myEmail,myPublicKey};
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
        Security.addProvider(new FlexiCoreProvider());
        Security.addProvider(new FlexiECProvider());
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
                tmpPtK = formatPrivate(keypair.getPrivate().getEncoded());
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
        if(!privateExist()){
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
            if(MessageFormat.decryptedMsg.getPublicKey()==null){
                MessageFormat.decryptedMsg=null;
                LightMessage.decryptedLightMsg=new LightMessage(decryptedBytes);
            }else
                LightMessage.decryptedLightMsg=null;
            StaticVariables.encrypted_msg_size=encryptedMessage.length();
            StaticVariables.orig_msg_size=decryptedBytes.length;
        } catch (Exception e) {
            MessageFormat.decryptedMsg = null;
            LightMessage.decryptedLightMsg=null;
            e.printStackTrace();
        }
    }

    public static void encrypt(final byte[] msg,final byte[] light, final String friendPublicKey) {
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
            if(light!=null)
                StaticVariables.encryptedLight = Visual.bin2hex(cipher.doFinal(light));
            encryptedMsgToSend = Visual.bin2hex(cipher.doFinal(msg));
        } catch (Exception ignore) {
            ignore.printStackTrace();
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
    public static String getPrivateTmp() {
        return Visual.bin2hex(tmpPtK.getEncoded());
    }
}
