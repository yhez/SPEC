package de.flexiprovider.core.rijndael;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;

import de.flexiprovider.api.keys.SecretKeyFactory;
import de.flexiprovider.api.keys.SecretKeySpec;
import de.flexiprovider.core.rijndael.Rijndael.AES;


public final class RijndaelKeyFactory extends SecretKeyFactory {


    public SecretKey generateSecret(KeySpec keySpec)
            throws InvalidKeySpecException {

        if (keySpec == null) {
            throw new InvalidKeySpecException("Key specification is null.");
        }

        if (keySpec instanceof SecretKeySpec) {
            SecretKeySpec secKeySpec = (SecretKeySpec) keySpec;
            String algorithm = secKeySpec.getAlgorithm();
            if (algorithm.equals(Rijndael.ALG_NAME)
                    || algorithm.startsWith(AES.ALG_NAME)
                    || algorithm.startsWith(AES.OID)) {
                return new RijndaelKey(secKeySpec.getEncoded());
            }
        }

        throw new InvalidKeySpecException("Unsupported key specification type.");
    }


    public KeySpec getKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException {

        if ((keySpec == null) || !keySpec.isAssignableFrom(SecretKeySpec.class)) {
            throw new InvalidKeySpecException("wrong spec type");
        }
        if ((key == null) || !(key instanceof RijndaelKey)) {
            throw new InvalidKeySpecException("wrong key type");
        }

        return new SecretKeySpec(key.getEncoded(), "Rijndael");
    }


    public SecretKey translateKey(SecretKey key) throws InvalidKeyException {
        throw new InvalidKeyException("not implemented");
    }

}
