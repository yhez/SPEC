package de.flexiprovider.core.rsa.interfaces;

import de.flexiprovider.api.keys.KeyFactory;
import de.flexiprovider.api.keys.KeySpec;
import de.flexiprovider.core.rsa.RSAPrivateCrtKeySpec;
import de.flexiprovider.core.rsa.RSAPrivateKeySpec;
import de.flexiprovider.core.rsa.RSAPublicKeySpec;

public abstract class RSAKeyFactory extends KeyFactory {

    protected java.security.PublicKey engineGeneratePublic(
            java.security.spec.KeySpec keySpec)
            throws java.security.spec.InvalidKeySpecException {

        if (keySpec != null && !(keySpec instanceof KeySpec)
                && (keySpec instanceof java.security.spec.RSAPublicKeySpec)) {
            KeySpec rsaKeySpec = new RSAPublicKeySpec(
                    (java.security.spec.RSAPublicKeySpec) keySpec);
            return super.engineGeneratePublic(rsaKeySpec);
        }

        return super.engineGeneratePublic(keySpec);
    }

    protected java.security.PrivateKey engineGeneratePrivate(
            java.security.spec.KeySpec keySpec)
            throws java.security.spec.InvalidKeySpecException {

        if (keySpec != null && !(keySpec instanceof KeySpec)) {
            if (keySpec instanceof java.security.spec.RSAPrivateCrtKeySpec) {
                KeySpec rsaKeySpec = new RSAPrivateCrtKeySpec(
                        (java.security.spec.RSAPrivateCrtKeySpec) keySpec);
                return super.engineGeneratePrivate(rsaKeySpec);
            }

            if (keySpec instanceof java.security.spec.RSAPrivateKeySpec) {
                KeySpec rsaKeySpec = new RSAPrivateKeySpec(
                        (java.security.spec.RSAPrivateKeySpec) keySpec);
                return super.engineGeneratePrivate(rsaKeySpec);
            }
        }

        return super.engineGeneratePrivate(keySpec);
    }
}
