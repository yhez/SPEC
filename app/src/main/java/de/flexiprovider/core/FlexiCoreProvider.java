package de.flexiprovider.core;

import de.flexiprovider.api.FlexiProvider;
import de.flexiprovider.common.mode.ModeParameterGenerator;
import de.flexiprovider.common.mode.ModeParameters;
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
import de.flexiprovider.core.rijndael.RijndaelKeyGenerator;
import de.flexiprovider.core.rijndael.RijndaelParameters;

public class FlexiCoreProvider extends FlexiProvider {

    public FlexiCoreProvider() {
        super("FlexiCore", 1.76, "");
        CoreRegistry.registerAlgorithms();
        registerCommon();
        registerHMAC();
        registerAESRijndael();
    }

    private void registerCommon() {
        add(ALG_PARAMS, ModeParameters.class, new String[]{"Mode", "IV"});
        add(ALG_PARAM_GENERATOR, ModeParameterGenerator.class, new String[]{
                "Mode", "IV"});
    }

    private void registerHMAC() {
        add(SECRET_KEY_FACTORY, HMacKeyFactory.class, new String[]{"Hmac",
                "HmacSHA1", HMac.SHA1.OID, HMac.SHA1.PKCS5_OID});
        add(SECRET_KEY_GENERATOR, HMacKeyGenerator.SHA1.class, new String[]{
                "HmacSHA1", HMac.SHA1.OID, HMac.SHA1.PKCS5_OID});
        add(MAC, HMac.SHA1.class, new String[]{"HmacSHA1", HMac.SHA1.OID});
    }

    private void registerAESRijndael() {
        add(SECRET_KEY_GENERATOR, RijndaelKeyGenerator.class, new String[]{
                Rijndael.ALG_NAME, AES.ALG_NAME, AES.OID});
        add(SECRET_KEY_FACTORY, RijndaelKeyFactory.class, new String[]{
                Rijndael.ALG_NAME, AES.ALG_NAME, AES.OID});
        add(CIPHER, AES.class, new String[]{AES.ALG_NAME, AES.OID});
        addReverseOID(CIPHER, AES.ALG_NAME, AES.OID);
        add(CIPHER, AES128_ECB.class, new String[]{AES128_ECB.ALG_NAME,
                AES128_ECB.OID});
        addReverseOID(CIPHER, AES128_ECB.ALG_NAME, AES128_ECB.OID);
        add(CIPHER, AES128_CBC.class, new String[]{AES128_CBC.ALG_NAME,
                AES128_CBC.OID});
        addReverseOID(CIPHER, AES128_CBC.ALG_NAME, AES128_CBC.OID);
        add(CIPHER, AES128_OFB.class, new String[]{AES128_OFB.ALG_NAME,
                AES128_OFB.OID});
        addReverseOID(CIPHER, AES128_OFB.ALG_NAME, AES128_OFB.OID);
        add(CIPHER, AES128_CFB.class, new String[]{AES128_CFB.ALG_NAME,
                AES128_CFB.OID});
        addReverseOID(CIPHER, AES128_CFB.ALG_NAME, AES128_CFB.OID);
        add(CIPHER, AES192_ECB.class, new String[]{AES192_ECB.ALG_NAME,
                AES192_ECB.OID});
        addReverseOID(CIPHER, AES192_ECB.ALG_NAME, AES192_ECB.OID);
        add(CIPHER, AES192_CBC.class, new String[]{AES192_CBC.ALG_NAME,
                AES192_CBC.OID});
        addReverseOID(CIPHER, AES192_CBC.ALG_NAME, AES192_CBC.OID);
        add(CIPHER, AES192_OFB.class, new String[]{AES192_OFB.ALG_NAME,
                AES192_OFB.OID});
        addReverseOID(CIPHER, AES192_OFB.ALG_NAME, AES192_OFB.OID);
        add(CIPHER, AES192_CFB.class, new String[]{AES192_CFB.ALG_NAME,
                AES192_CFB.OID});
        addReverseOID(CIPHER, AES192_CFB.ALG_NAME, AES192_CFB.OID);

        add(CIPHER, AES256_ECB.class, new String[]{AES256_ECB.ALG_NAME,
                AES256_ECB.OID});
        addReverseOID(CIPHER, AES256_ECB.ALG_NAME, AES256_ECB.OID);
        add(CIPHER, AES256_CBC.class, new String[]{AES256_CBC.ALG_NAME,
                AES256_CBC.OID});
        addReverseOID(CIPHER, AES256_CBC.ALG_NAME, AES256_CBC.OID);
        add(CIPHER, AES256_OFB.class, new String[]{AES256_OFB.ALG_NAME,
                AES256_OFB.OID});
        addReverseOID(CIPHER, AES256_OFB.ALG_NAME, AES256_OFB.OID);
        add(CIPHER, AES256_CFB.class, new String[]{AES256_CFB.ALG_NAME,
                AES256_CFB.OID});
        addReverseOID(CIPHER, AES256_CFB.ALG_NAME, AES256_CFB.OID);

		/* Rijndael */

        add(ALG_PARAMS, RijndaelParameters.class, Rijndael.ALG_NAME);

        add(CIPHER, Rijndael.class, Rijndael.ALG_NAME);
    }

}
