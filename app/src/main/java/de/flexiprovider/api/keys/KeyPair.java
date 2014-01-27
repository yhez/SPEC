package de.flexiprovider.api.keys;


public final class KeyPair {

    java.security.KeyPair pair;


    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        pair = new java.security.KeyPair(publicKey, privateKey);
    }

    public PublicKey getPublic() {
        return (PublicKey) pair.getPublic();
    }

    public PrivateKey getPrivate() {
        return (PrivateKey) pair.getPrivate();
    }

}
