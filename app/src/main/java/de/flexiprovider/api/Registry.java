package de.flexiprovider.api;

import java.security.NoSuchAlgorithmException;

import de.flexiprovider.common.util.DefaultPRNG;
import de.flexiprovider.core.rijndael.Rijndael;


public abstract class Registry {


    public static BlockCipher getBlockCipher(String transformation)
            throws NoSuchAlgorithmException {

        String modeName = null, paddingName = null;
        int endIndex = transformation.indexOf('/');
        if (endIndex >= 0) {
            // transformation is of the form 'algorithm/mode/padding'

            // get 'algorithm'

            // get 'mode/padding'
            String modePadding = transformation.substring(endIndex + 1);
            endIndex = modePadding.indexOf("/");
            if (endIndex == -1) {
                // if no padding is specified
                throw new NoSuchAlgorithmException(
                        "Badly formed transformation: only 'algorithm' "
                                + "or 'algorithm/mode/padding' allowed.");
            }

            // get 'mode'
            modeName = modePadding.substring(0, endIndex);

            // get 'padding'
            paddingName = modePadding.substring(endIndex + 1);

            // if even more information is provided, transformation is invalid
            if (paddingName.contains("/")) {
                throw new NoSuchAlgorithmException(
                        "Badly formed transformation: only 'algorithm' "
                                + "or 'algorithm/mode/padding' allowed.");
            }
        }

        BlockCipher result = new Rijndael.AES.AES128_CBC();
        if (modeName != null)
            result.setMode(modeName);
        if (paddingName != null)
            result.setPadding(paddingName);
        return result;
    }


    public static SecureRandom getSecureRandom() {
        return new DefaultPRNG();
    }
}
