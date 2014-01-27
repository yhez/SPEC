package de.flexiprovider.core.rijndael;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;


public class RijndaelKeyGenParameterSpec implements AlgorithmParameterSpec {

    public static final int DEFAULT_KEY_SIZE = 128;

    // the key size in bits
    private int keySize;

    public RijndaelKeyGenParameterSpec() {
        keySize = DEFAULT_KEY_SIZE;
    }

    public RijndaelKeyGenParameterSpec(int keySize) {
        if ((keySize != 128) && (keySize != 192) && (keySize != 256)) {
            this.keySize = DEFAULT_KEY_SIZE;
        } else {
            this.keySize = keySize;
        }
    }
    public int getKeySize() {
        return keySize;
    }

}
