package de.flexiprovider.core;

import de.flexiprovider.api.Registry;
import de.flexiprovider.core.mac.HMac;
import de.flexiprovider.core.mac.HMacKeyFactory;
import de.flexiprovider.core.mac.HMacKeyGenerator;
import de.flexiprovider.core.rijndael.Rijndael;
import de.flexiprovider.core.rijndael.Rijndael.AES;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES128_CBC;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES128_CFB;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES128_ECB;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES128_OFB;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES192_CBC;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES192_CFB;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES192_ECB;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES192_OFB;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES256_CBC;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES256_CFB;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES256_ECB;
import de.flexiprovider.core.rijndael.Rijndael.AES.AES256_OFB;
import de.flexiprovider.core.rijndael.RijndaelKeyFactory;
import de.flexiprovider.core.rijndael.RijndaelKeyGenParameterSpec;
import de.flexiprovider.core.rijndael.RijndaelKeyGenerator;
import de.flexiprovider.core.rijndael.RijndaelParameterSpec;
import de.flexiprovider.core.rijndael.RijndaelParameters;


public abstract class CoreRegistry extends Registry {

    // flag indicating if algorithms already have been registered
    private static boolean registered = false;

    public static void registerAlgorithms() {
        if (!registered) {
            registerHMAC();
            registerAESRijndael();
            registered = true;
        }
    }

    private static void registerHMAC() {
        // OIDs are defined by RFC 3370
        add(SECRET_KEY_FACTORY, HMacKeyFactory.class, new String[]{"Hmac",
                "HmacSHA1", HMac.SHA1.OID, HMac.SHA1.PKCS5_OID, "HmacSHA224",
                "HmacSHA256", "HmacSHA384", "HmacSHA512", "HmacMD4", "HmacMD5",
                HMac.MD5.OID, "HmacRIPEMD128", "HmacRIPEMD160",
                HMac.RIPEMD160.OID, "HmacRIPEMD256", "HmacRIPEMD320",
                "HmacTiger", HMac.Tiger.OID, "HmacDHA256", "HmacFORK256",});

        // HmacSHA1
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.SHA1.class, new String[]{
                "Hmac", "HmacSHA1", HMac.SHA1.OID, HMac.SHA1.PKCS5_OID});
        add(MAC, HMac.SHA1.class, new String[]{"Hmac", "HmacSHA1",
                HMac.SHA1.OID});

        // HmacSHA224
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.SHA224.class, "HmacSHA224");
        add(MAC, HMac.SHA224.class, "HmacSHA224");

        // HmacSHA256
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.SHA256.class, "HmacSHA256");
        add(MAC, HMac.SHA256.class, "HmacSHA256");

        // HmacSHA384
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.SHA384.class, "HmacSHA384");
        add(MAC, HMac.SHA384.class, "HmacSHA384");

        // HmacSHA512
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.SHA512.class, "HmacSHA512");
        add(MAC, HMac.SHA512.class, "HmacSHA512");

        // HmacMD4
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.MD4.class, "HmacMD4");
        add(MAC, HMac.MD4.class, "HmacMD4");

        // HmacMD5
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.MD5.class, new String[]{
                "HmacMD5", HMac.MD5.OID});
        add(MAC, HMac.MD5.class, new String[]{"HmacMD5", HMac.MD5.OID});

        // HmacRIPEMD128
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.RIPEMD128.class,
                "HmacRIPEMD128");
        add(MAC, HMac.RIPEMD128.class, "HmacRIPEMD128");

        // HmacRIPEMD160
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.RIPEMD160.class,
                new String[]{"HmacRIPEMD160", HMac.RIPEMD160.OID});
        add(MAC, HMac.RIPEMD160.class, new String[]{"HmacRIPEMD160",
                HMac.RIPEMD160.OID});

        // HmacRIPEMD256
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.RIPEMD256.class,
                "HmacRIPEMD256");
        add(MAC, HMac.RIPEMD256.class, "HmacRIPEMD256");

        // HmacRIPEMD320
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.RIPEMD320.class,
                "HmacRIPEMD320");
        add(MAC, HMac.RIPEMD320.class, "HmacRIPEMD320");

        // HmacTiger
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.Tiger.class, new String[]{
                "HmacTiger", HMac.Tiger.OID});
        add(MAC, HMac.Tiger.class, new String[]{"HmacTiger", HMac.Tiger.OID});

        // HmacDHA256
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.DHA256.class, "HmacDHA256");
        add(MAC, HMac.DHA256.class, "HmacDHA256");

        // HmacFORK256
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.FORK256.class, "HmacFORK256");
        add(MAC, HMac.FORK256.class, "HmacFORK256");
    }

    private static void registerAESRijndael() {
        /* common */

        add(ALG_PARAM_SPEC, RijndaelKeyGenParameterSpec.class,
                new String[]{Rijndael.ALG_NAME + "KeyGen",
                        AES.ALG_NAME + "KeyGen", AES.OID});
        add(SECRET_KEY_GENERATOR, RijndaelKeyGenerator.class, new String[]{
                Rijndael.ALG_NAME, AES.ALG_NAME, AES.OID});
        add(SECRET_KEY_FACTORY, RijndaelKeyFactory.class, new String[]{
                Rijndael.ALG_NAME, AES.ALG_NAME, AES.OID});

		/* AES */

        add(BLOCK_CIPHER, AES.class, new String[]{AES.ALG_NAME, AES.OID});

        add(BLOCK_CIPHER, AES128_ECB.class, new String[]{AES128_ECB.ALG_NAME,
                AES128_ECB.OID});
        add(BLOCK_CIPHER, AES128_CBC.class, new String[]{AES128_CBC.ALG_NAME,
                AES128_CBC.OID});
        add(BLOCK_CIPHER, AES128_OFB.class, new String[]{AES128_OFB.ALG_NAME,
                AES128_OFB.OID});
        add(BLOCK_CIPHER, AES128_CFB.class, new String[]{AES128_CFB.ALG_NAME,
                AES128_CFB.OID});

        add(BLOCK_CIPHER, AES192_ECB.class, new String[]{AES192_ECB.ALG_NAME,
                AES192_ECB.OID});
        add(BLOCK_CIPHER, AES192_CBC.class, new String[]{AES192_CBC.ALG_NAME,
                AES192_CBC.OID});
        add(BLOCK_CIPHER, AES192_OFB.class, new String[]{AES192_OFB.ALG_NAME,
                AES192_OFB.OID});
        add(BLOCK_CIPHER, AES192_CFB.class, new String[]{AES192_CFB.ALG_NAME,
                AES192_CFB.OID});

        add(BLOCK_CIPHER, AES256_ECB.class, new String[]{AES256_ECB.ALG_NAME,
                AES256_ECB.OID});
        add(BLOCK_CIPHER, AES256_CBC.class, new String[]{AES256_CBC.ALG_NAME,
                AES256_CBC.OID});
        add(BLOCK_CIPHER, AES256_OFB.class, new String[]{AES256_OFB.ALG_NAME,
                AES256_OFB.OID});
        add(BLOCK_CIPHER, AES256_CFB.class, new String[]{AES256_CFB.ALG_NAME,
                AES256_CFB.OID});

		/* Rijndael */

        add(ALG_PARAM_SPEC, RijndaelParameterSpec.class, Rijndael.ALG_NAME);
        add(ALG_PARAMS, RijndaelParameters.class, Rijndael.ALG_NAME);

        add(BLOCK_CIPHER, Rijndael.class, Rijndael.ALG_NAME);
    }

}
