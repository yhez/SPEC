package de.flexiprovider.api.keys;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactorySpi;

public abstract class SecretKeyFactory extends SecretKeyFactorySpi {

    protected javax.crypto.SecretKey engineGenerateSecret(
            java.security.spec.KeySpec keySpec)
            throws java.security.spec.InvalidKeySpecException {

        if (keySpec == null) {
            throw new java.security.spec.InvalidKeySpecException();
        }

        return generateSecret(keySpec);
    }

    protected java.security.spec.KeySpec engineGetKeySpec(
            javax.crypto.SecretKey key, Class keySpec)
            throws java.security.spec.InvalidKeySpecException {

        if ((key == null) || (keySpec == null)) {
            throw new java.security.spec.InvalidKeySpecException();
        }
        return getKeySpec(key, keySpec);
    }

    protected javax.crypto.SecretKey engineTranslateKey(
            javax.crypto.SecretKey key)
            throws java.security.InvalidKeyException {

        if ((key == null)) {
            throw new java.security.InvalidKeyException();
        }
        return translateKey(key);
    }

    public abstract SecretKey generateSecret(KeySpec keySpec)
            throws InvalidKeySpecException;

    public abstract KeySpec getKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException;

    public abstract SecretKey translateKey(SecretKey key)
            throws InvalidKeyException;

}
