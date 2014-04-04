package de.flexiprovider.api.keys;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public abstract class KeyFactory extends java.security.KeyFactorySpi {

    protected java.security.PublicKey engineGeneratePublic(
            java.security.spec.KeySpec keySpec)
            throws java.security.spec.InvalidKeySpecException {

        return generatePublic(keySpec);
    }


    protected java.security.PrivateKey engineGeneratePrivate(
            java.security.spec.KeySpec keySpec)
            throws java.security.spec.InvalidKeySpecException {

        return generatePrivate(keySpec);
    }

    protected final java.security.spec.KeySpec engineGetKeySpec(
            java.security.Key key, Class keySpec)
            throws java.security.spec.InvalidKeySpecException {

        if (key == null) {
            throw new java.security.spec.InvalidKeySpecException();
        }

        return getKeySpec(key, keySpec);
    }

    protected final java.security.Key engineTranslateKey(java.security.Key key)
            throws java.security.InvalidKeyException {

        if (key == null) {
            throw new java.security.InvalidKeyException();
        }

        return translateKey(key);
    }


    public abstract PublicKey generatePublic(KeySpec keySpec)
            throws InvalidKeySpecException;


    public abstract PrivateKey generatePrivate(KeySpec keySpec)
            throws InvalidKeySpecException;

    public abstract KeySpec getKeySpec(Key key, Class keySpec)
            throws InvalidKeySpecException;

    public abstract Key translateKey(Key key) throws InvalidKeyException;

}
