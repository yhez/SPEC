package specular.systems;

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
    private static PrivateKey mPtK = null;
    private static String myName = null, myEmail = null, myPublicKey = null,
            myPrivateKey = null;
    private static PrivateKey tmpPtK = null;
    private static String tmpPublicKey = null,
            tmpPrivateKey = null;
    private static boolean notInit = true;

    public static String getPrivateToSave() {
        return myPrivateKey;
    }

    public static void deleteKeys() {
        myPrivateKey = null;
        mPtK = null;
        PublicStaticVariables.decryptedMsg = null;
    }

    public static boolean setPrivate(String p) {
        if (p != null) {
            myPrivateKey = p;
            mPtK = formatPrivate(p);
            return mPtK != null;
        }
        return false;
    }

    public static void setDetails(String name, String email) {
        myName = name;
        myEmail = email;
    }

    public static void moveKeysFromTmp() {
        myPublicKey = tmpPublicKey;
        tmpPublicKey=null;
        mPtK = tmpPtK;
        tmpPtK=null;
        myPrivateKey = tmpPrivateKey;
        tmpPrivateKey=null;
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
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("ECIES", "FlexiEC");
            CurveParams ecParams = new CurveRegistry.BrainpoolP512r1();
            try {
                kpg.initialize(ecParams, new SecureRandom());
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            KeyPair keypair;
            keypair = kpg.generateKeyPair();
            tmpPublicKey = Visual.bin2hex(keypair.getPublic().getEncoded());
            tmpPrivateKey = Visual.bin2hex(keypair.getPrivate().getEncoded());
            tmpPtK = formatPrivate(tmpPrivateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public static void decrypt(String encryptedMessage) {
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
            PublicStaticVariables.decryptedMsg = new MessageFormat(decryptedBytes);
            if(PublicStaticVariables.decryptedMsg==null){
                PublicStaticVariables.flag_hash=null;
            }
        } catch (Exception e) {
            PublicStaticVariables.decryptedMsg = null;
            e.printStackTrace();
        }
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
            PublicStaticVariables.encryptedMsgToSend = Visual.bin2hex(cipher.doFinal(msg));
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    private static PrivateKey formatPrivate(String p) {
        if (notInit) {
            addProviders();
            notInit = false;
        }
        try {
            return KeyFactory.getInstance("ECIES", "FlexiEC").generatePrivate(
                    new PKCS8EncodedKeySpec(Visual
                            .hex2bin(p)));

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }
//TODO
 /*   public static void signPublicQR() {

    }

    public static boolean checkSignPublicQR() {
        return false;
    }*/
}
