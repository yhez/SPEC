package de.flexiprovider.core.mac;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;

import de.flexiprovider.api.keys.SecretKeyFactory;
import de.flexiprovider.api.keys.SecretKeySpec;

public class HMacKeyFactory extends SecretKeyFactory {

    public SecretKey generateSecret(KeySpec keySpec)
            throws InvalidKeySpecException {

        if (keySpec == null) {
            throw new InvalidKeySpecException("key spec is null");
        }

        if (keySpec instanceof SecretKeySpec) {
            SecretKeySpec secKeySpec = (SecretKeySpec) keySpec;
            if (!(secKeySpec.getAlgorithm().startsWith("Hmac"))) {
                throw new InvalidKeySpecException("unsupported type");
            }
            return new HMacKey(secKeySpec.getEncoded());
        }

        throw new InvalidKeySpecException("unsupported type");
    }

    public KeySpec getKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException {

        if ((key == null) || !(key instanceof HMacKey)) {
            throw new InvalidKeySpecException("unsupported key type");
        }
        if ((keySpec == null)
                || !(keySpec.isAssignableFrom(SecretKeySpec.class))) {
            throw new InvalidKeySpecException("unsupported spec type");
        }

        return new SecretKeySpec(key.getEncoded(), "Hmac");
    }

    public SecretKey translateKey(SecretKey key) throws InvalidKeyException {
        if (!(key instanceof HMacKey)) {
            throw new InvalidKeyException("Unsupported key type.");
        }
        return key;
    }

}
