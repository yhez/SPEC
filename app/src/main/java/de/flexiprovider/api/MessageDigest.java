package de.flexiprovider.api;

import java.security.DigestException;

public abstract class MessageDigest extends java.security.MessageDigestSpi {

    protected final int engineGetDigestLength() {
        return getDigestLength();
    }

    protected final void engineUpdate(byte input) {
        update(input);
    }

    protected final void engineUpdate(byte[] input, int offset, int len) {
        update(input, offset, len);
    }

    protected final byte[] engineDigest() {
        return digest();
    }

    protected final void engineReset() {
        reset();
    }
    public abstract int getDigestLength();

    public abstract void update(byte input);

    public final void update(byte[] input) {
        if (input == null) {
            return;
        }
        update(input, 0, input.length);
    }

    public abstract void update(byte[] input, int offset, int len);


    public abstract byte[] digest();


    public final int digest(byte[] buf, int offset, int len)
            throws DigestException {

        byte[] digest = digest();
        if (len < digest.length) {
            throw new DigestException("partial digests not returned");
        }
        if (buf.length - offset < digest.length) {
            throw new DigestException("insufficient space in the output "
                    + "buffer to store the digest");
        }
        System.arraycopy(digest, 0, buf, offset, digest.length);
        return digest.length;
    }

    public abstract void reset();

}
