package de.flexiprovider.core.md;

public final class SHA256 extends SHA224_256 {

    // Initial hash value H<sup>(0)</sup>. These were obtained by taking the
    // fractional parts of the square roots of the first eight primes.
    private static final int[] H0 = {0x6a09e667, 0xbb67ae85, 0x3c6ef372,
            0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};

    // length of the SHA256 message digest in bytes
    private static final int SHA256_DIGEST_LENGTH = 32;

    public SHA256() {
        super(SHA256_DIGEST_LENGTH);
    }
    public void reset() {
        initMessageDigest(H0);
    }

}
