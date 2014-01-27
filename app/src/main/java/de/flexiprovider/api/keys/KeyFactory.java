package de.flexiprovider.api.keys;

import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.InvalidKeySpecException;
import de.flexiprovider.pki.PKCS8EncodedKeySpec;
import de.flexiprovider.pki.X509EncodedKeySpec;

public abstract class KeyFactory extends java.security.KeyFactorySpi {

    protected java.security.PublicKey engineGeneratePublic(
            java.security.spec.KeySpec keySpec)
            throws java.security.spec.InvalidKeySpecException {

        if (keySpec != null && !(keySpec instanceof KeySpec)) {
            if (keySpec instanceof java.security.spec.X509EncodedKeySpec) {
                KeySpec encKeySpec = new X509EncodedKeySpec(
                        (java.security.spec.X509EncodedKeySpec) keySpec);
                return generatePublic(encKeySpec);
            }

            throw new java.security.spec.InvalidKeySpecException();
        }

        return generatePublic((KeySpec) keySpec);
    }


    protected java.security.PrivateKey engineGeneratePrivate(
            java.security.spec.KeySpec keySpec)
            throws java.security.spec.InvalidKeySpecException {

        if (keySpec != null && !(keySpec instanceof KeySpec)) {
            if (keySpec instanceof java.security.spec.PKCS8EncodedKeySpec) {
                KeySpec encKeySpec = new PKCS8EncodedKeySpec(
                        (java.security.spec.PKCS8EncodedKeySpec) keySpec);
                return generatePrivate(encKeySpec);
            }

            throw new java.security.spec.InvalidKeySpecException();
        }

        return generatePrivate((KeySpec) keySpec);
    }

    protected final java.security.spec.KeySpec engineGetKeySpec(
            java.security.Key key, Class keySpec)
            throws java.security.spec.InvalidKeySpecException {

        if (!(key instanceof Key)) {
            throw new java.security.spec.InvalidKeySpecException();
        }

        return getKeySpec((Key) key, keySpec);
    }

    protected final java.security.Key engineTranslateKey(java.security.Key key)
            throws java.security.InvalidKeyException {

        if (!(key instanceof Key)) {
            throw new java.security.InvalidKeyException();
        }

        return translateKey((Key) key);
    }


    public abstract PublicKey generatePublic(KeySpec keySpec)
            throws InvalidKeySpecException;


    public abstract PrivateKey generatePrivate(KeySpec keySpec)
            throws InvalidKeySpecException;

    public abstract KeySpec getKeySpec(Key key, Class keySpec)
            throws InvalidKeySpecException;

    public abstract Key translateKey(Key key) throws InvalidKeyException;

}
