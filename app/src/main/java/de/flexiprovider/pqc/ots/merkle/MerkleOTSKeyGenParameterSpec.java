package de.flexiprovider.pqc.ots.merkle;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;

public class MerkleOTSKeyGenParameterSpec implements AlgorithmParameterSpec {

    private byte[] seed;


    public MerkleOTSKeyGenParameterSpec(byte[] seed) {
        this.seed = seed;
    }
    public byte[] getSeed() {
        return seed;
    }

}
