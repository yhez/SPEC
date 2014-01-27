package de.flexiprovider.core.md;


public final class SHA224 extends SHA224_256 {


    // Initial hash value H<sup>(0)</sup>. These were obtained by taking the
    // fractional parts of the square roots of the first eight primes.
    private static final int[] H0 = {0xc1059ed8, 0x367cd507, 0x3070dd17,
            0xf70e5939, 0xffc00b31, 0x68581511, 0x64f98fa7, 0xbefa4fa4};

    private static final int SHA224_DIGEST_LENGTH = 28;

    public SHA224() {
        super(SHA224_DIGEST_LENGTH);
    }

    public void reset() {
        initMessageDigest(H0);
    }

}
