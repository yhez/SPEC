package de.flexiprovider.core.md;


public final class SHA384 extends SHA384_512 {

    // Initial hash value H<sup>(0)</sup>. These were obtained by taking the
    // fractional parts of the square roots of the ninth to sixteenth prime.
    private static final long[] H0 = {0xcbbb9d5dc1059ed8L,
            0x629a292a367cd507L, 0x9159015a3070dd17L, 0x152fecd8f70e5939L,
            0x67332667ffc00b31L, 0x8eb44a8768581511L, 0xdb0c2e0d64f98fa7L,
            0x47b5481dbefa4fa4L};

    private static final int SHA384_DIGEST_LENGTH = 48;

    public SHA384() {
        super(SHA384_DIGEST_LENGTH);
    }

    public void reset() {
        initMessageDigest(H0);
    }

}
