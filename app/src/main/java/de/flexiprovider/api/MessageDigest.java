package de.flexiprovider.api;

import de.flexiprovider.api.exceptions.DigestException;

public abstract class MessageDigest extends java.security.MessageDigestSpi {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

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

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    /**
     * @return the digest length in bytes
     */
    public abstract int getDigestLength();

    /**
     * Update the digest using the specified byte.
     *
     * @param input the byte to use for the update
     */
    public abstract void update(byte input);

    /**
     * Update the digest using the specified array of bytes, starting at the
     * specified offset.
     *
     * @param input the array of bytes to use for the update
     */
    public final void update(byte[] input) {
        if (input == null) {
            return;
        }
        update(input, 0, input.length);
    }

    /**
     * Update the digest using the specified array of bytes, starting at the
     * specified offset.
     *
     * @param input  the array of bytes to use for the update
     * @param offset the offset to start from in the array of bytes
     * @param len    the number of bytes to use, starting at <tt>offset</tt>
     */
    public abstract void update(byte[] input, int offset, int len);


    public abstract byte[] digest();


    public final byte[] digest(byte[] input) {
        update(input);
        return digest();
    }


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

    /**
     * Reset the digest for further use.
     */
    public abstract void reset();

}
