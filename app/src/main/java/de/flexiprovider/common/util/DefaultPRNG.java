package de.flexiprovider.common.util;

import de.flexiprovider.api.SecureRandom;

public class DefaultPRNG extends SecureRandom {

    java.security.SecureRandom javaRand;

    public DefaultPRNG() {
        javaRand = new java.security.SecureRandom();
    }

    public byte[] generateSeed(int numBytes) {
        return javaRand.generateSeed(numBytes);
    }

    public void nextBytes(byte[] bytes) {
        javaRand.nextBytes(bytes);
    }

    public void setSeed(byte[] seed) {
        javaRand.setSeed(seed);
    }

}
