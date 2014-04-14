package specular.systems;


import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.api.keys.KeyPairGenerator;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.ec.ECIES;
import de.flexiprovider.ec.keys.ECKeyPairGenerator;
import de.flexiprovider.ec.keys.ECPrivateKey;
import de.flexiprovider.ec.keys.ECPublicKey;
import de.flexiprovider.ec.parameters.CurveParamsGFP;

public class CryptMethods {
    private static ECPrivateKey mPtK;
    private static String myName = null, myEmail = null, myPublicKey = null;
    private static ECPrivateKey tmpPtK = null;
    private static byte[] tmpPrivateKey;
    private static byte[] myPrivateKey;
    private static String tmpPublicKey = null;
    private static boolean lock = true;

    public static byte[] getPrivateToSave() {
        return myPrivateKey;
    }

    public static void deleteKeys() {
        if (lock) {
            lock = false;
            if (mPtK != null) {
                BigInteger bi = mPtK.getS().bigInt;
                try {
                    Field f = bi.getClass().getDeclaredField("bigInt");
                    f.setAccessible(true);
                    Object bigInt = f.get(bi);
                    long addr = (Long) bigInt.getClass().getDeclaredField("bignum").get(bigInt);
                    new NativeDelete().delete(addr, bi.bitLength());
                } catch (Exception ignore) {
                }
                mPtK = null;
            }
            if (myPrivateKey != null) {
                for (int a = 0; a < myPrivateKey.length; a++)
                    myPrivateKey[a] = (byte) System.currentTimeMillis();
                myPrivateKey = null;
            }
            Log.i("key", "deleted");
            MessageFormat.decryptedMsg = null;
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


    private static KeyPair createKeyPair() {
        CurveParamsGFP ecParams = new CurveParamsGFP();
        KeyPairGenerator kpg = new ECKeyPairGenerator();
        try {
            kpg.initialize(ecParams, (SecureRandom) null);
            return kpg.genKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static KeyPair createKeysForGroup() {
        return createKeyPair();
    }

    public static void createKeys() {
        KeyPair keypair = createKeyPair();
            ECPrivateKey tmp = (ECPrivateKey) keypair.getPrivate();
            if (tmp != null) {
                try {
                    tmpPublicKey = Visual.bin2hex(((ECPublicKey) keypair.getPublic()).getW().EC2OSP(1));
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                tmpPtK = tmp;
                tmpPrivateKey = tmpPtK.getS().toByteArray();
            }
    }

    public static int decrypt(String encryptedMessage, byte[] key) {
        MessageFormat.decryptedMsg = null;
        StaticVariables.decryptedBackup = null;
        StaticVariables.decryptedGroup = null;
        if (encryptedMessage == null) {
            return -1;
        }
        if (!privateExist()) {
            return -1;
        }
        byte[] rawMsg = Visual.hex2bin(encryptedMessage);

        byte[] decryptedBytes;
        if (key == null)
            decryptedBytes = decrypt(rawMsg);
        else
            decryptedBytes = decrypt(rawMsg, key);
        if (decryptedBytes == null) {
            return -1;
        }
        StaticVariables.encrypted_msg_size = encryptedMessage.length();
        StaticVariables.orig_msg_size = decryptedBytes.length;
        int result = FileParser.getType(decryptedBytes);
        switch (result) {
            case FileParser.ENCRYPTED_MSG:
                MessageFormat.decryptedMsg = new MessageFormat(decryptedBytes);
                return result;
            case FileParser.ENCRYPTED_QR_MSG:
                return result;
            case FileParser.ENCRYPTED_BACKUP:
                StaticVariables.decryptedBackup = decryptedBytes;
                return result;
            case FileParser.ENCRYPTED_GROUP:
                StaticVariables.decryptedGroup = decryptedBytes;
                return result;
            default:
                return result;
        }
    }

    public static byte[] encrypt(byte[] b, String publicKey) {
        try {
            CurveParamsGFP cp = new CurveParamsGFP();
            ECPublicKey frndPbK = new ECPublicKey(Point.OS2ECP(Visual.hex2bin(publicKey), cp), cp);
            ECIES cipher = new ECIES();
            cipher.initEncrypt(frndPbK, null, null);
            return cipher.doFinal(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] raw) {
        return decrypt(raw, null);
    }

    public static byte[] decrypt(byte[] data, byte[] key) {
        try {
            ECIES cipher = new ECIES();
            if (key == null)
                cipher.initDecrypt(mPtK, null);
            else
                cipher.initDecrypt(formatPrivate(key), null);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ECPrivateKey formatPrivate(byte[] p) {
        return new ECPrivateKey(new FlexiBigInt(p), new CurveParamsGFP());
    }

    public static String getPublicTmp() {
        return tmpPublicKey;
    }

    public static byte[] getPrivateTmp() {
        return tmpPrivateKey;
    }

    public static void removeTemp() {
        tmpPtK = null;
        tmpPrivateKey = null;
        tmpPublicKey = null;
    }

    /*
        public static String getMyLink() {
            return "specular.systems://?name=" + myName + "&email=" + myEmail + "&key=" + myPublicKey;
        }
    */
    public static byte[] getPrivateHash() {
        try {
            MessageDigest md = MessageDigest.getInstance(Visual.strings.SHA);
            return md.digest(myPrivateKey);
        } catch (NoSuchAlgorithmException e) {
            return new byte[0];
        }
    }

    public static class randomBits {
        int numBytes, numBits;
        byte[] randomBits;
        CameraPreview cp;

        public randomBits(int numBits) {
            numBytes = (int) (((long) numBits + 7) / 8); // avoid overflow
            randomBits = new byte[numBytes];
            this.numBits = numBits;
            cp = CameraPreview.getCameraPreview();
        }

        public byte[] getRandomBits() {
            if(cp==null)
                return null;
            while (!cp.ready) {
                synchronized (this) {
                    try {
                        wait(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return cp.getData();
        }
    }
}
