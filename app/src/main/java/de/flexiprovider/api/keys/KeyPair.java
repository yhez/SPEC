package de.flexiprovider.api.keys;


import java.security.PrivateKey;
import java.security.PublicKey;

public final class KeyPair {

    java.security.KeyPair pair;


    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        pair = new java.security.KeyPair(publicKey, privateKey);
    }

    public PublicKey getPublic() {
        return pair.getPublic();
    }

    public PrivateKey getPrivate() {
        return pair.getPrivate();
    }

}
