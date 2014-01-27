package de.flexiprovider.core.md;

import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.common.util.LittleEndianConversions;


public abstract class MDFamilyDigest extends MessageDigest {

    // the digest length in bytes
    private int digestLength;

    // buffer used to store bytes for
    private byte[] buffer = new byte[64];

    // count the number of bytes to digest
    private int count;

    protected int[] x = new int[16];

    protected int[] state = null;

    protected MDFamilyDigest(int digestLength) {
        this.digestLength = digestLength;
        reset();
    }


    protected void initMessageDigest(int[] initialState) {
        if (state == null) {
            state = new int[initialState.length];
        }
        System.arraycopy(initialState, 0, state, 0, initialState.length);
        count = 0;
    }
    public int getDigestLength() {
        return digestLength;
    }

    public synchronized void update(byte b) {
        buffer[count & 63] = b;
        if ((count & 63) == 63) {
            // 64 bytes arrived -> time for some processing
            for (int i = 15; i >= 0; i--) {
                x[i] = LittleEndianConversions.OS2IP(buffer, 4 * i);
            }
            processBlock();
        }
        count++;
    }

    public synchronized void update(byte[] bytes, int offset, int len) {
        // fill up buffer
        while ((len > 0) & ((count & 63) != 0)) {
            update(bytes[offset++]);
            len--;
        }

        // return if nothing left to do
        if (len == 0) {
            return;
        }

        // process 64 byte blocks at once
        while (len >= 64) {
            for (int i = 0; i <= 15; i++) {
                x[i] = LittleEndianConversions.OS2IP(bytes, offset);
                offset += 4;
            }
            count += 64;
            len -= 64;
            processBlock();
        }

        // process the remaining bytes
        if (len > 0) {
            System.arraycopy(bytes, offset, buffer, 0, len);
            count += len;
        }
    }

    protected void padMessageDigest() {
        // bit length = count * 8
        long len = count << 3;

        // do some padding
        update((byte) 0x80); // add single bit
        while ((count & 63) != 56) {
            update((byte) 0); // fill up with zeros
        }

        // convert byte buffer to int buffer
        for (int i = 0; i < 14; i++) {
            x[i] = LittleEndianConversions.OS2IP(buffer, 4 * i);
        }

        // add length
        x[14] = (int) (len);
        x[15] = (int) ((len >>> 32));

        processBlock();
    }

    protected abstract void processBlock();

    protected static int rotateLeft(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

}
