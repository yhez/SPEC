package de.flexiprovider.api;

public abstract class SecureRandom extends java.security.SecureRandomSpi {

    protected final byte[] engineGenerateSeed(int numBytes) {
        return generateSeed(numBytes);
    }

    protected final void engineNextBytes(byte[] bytes) {
        nextBytes(bytes);
    }

    protected final void engineSetSeed(byte[] seed) {
        setSeed(seed);
    }

    public abstract byte[] generateSeed(int numBytes);

    public abstract void nextBytes(byte[] bytes);

    public abstract void setSeed(byte[] seed);

}
