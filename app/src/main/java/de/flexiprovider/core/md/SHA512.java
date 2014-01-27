package de.flexiprovider.core.md;


public final class SHA512 extends SHA384_512 {

    // Initial hash value H<sup>(0)</sup>. These were obtained by taking the
    // fractional parts of the square roots of the first eight primes.
    private static final long[] H0 = {0x6a09e667f3bcc908L,
            0xbb67ae8584caa73bL, 0x3c6ef372fe94f82bL, 0xa54ff53a5f1d36f1L,
            0x510e527fade682d1L, 0x9b05688c2b3e6c1fL, 0x1f83d9abfb41bd6bL,
            0x5be0cd19137e2179L};

    // length of the SHA512 message digest in bytes
    private static final int SHA512_DIGEST_LENGTH = 64;

    public SHA512() {
        super(SHA512_DIGEST_LENGTH);
    }

    public void reset() {
        initMessageDigest(H0);
    }

}
