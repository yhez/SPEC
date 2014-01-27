package de.flexiprovider.common.util;

/**
 * This is a utility class containing data type conversions using little-endian
 * byte order.
 *
 * @see BigEndianConversions
 */
public final class LittleEndianConversions {

    /**
     * Default constructor (private).
     */
    private LittleEndianConversions() {
        // empty
    }

    public static int OS2IP(byte[] input, int inOff) {
        int result = input[inOff++] & 0xff;
        result |= (input[inOff++] & 0xff) << 8;
        result |= (input[inOff++] & 0xff) << 16;
        result |= (input[inOff] & 0xff) << 24;
        return result;
    }

    public static long OS2LIP(byte[] input, int inOff) {
        long result = input[inOff++] & 0xff;
        result |= (input[inOff++] & 0xff) << 8;
        result |= (input[inOff++] & 0xff) << 16;
        result |= ((long) input[inOff++] & 0xff) << 24;
        result |= ((long) input[inOff++] & 0xff) << 32;
        result |= ((long) input[inOff++] & 0xff) << 40;
        result |= ((long) input[inOff++] & 0xff) << 48;
        result |= ((long) input[inOff] & 0xff) << 56;
        return result;
    }

    public static byte[] I2OSP(int x) {
        byte[] result = new byte[4];
        result[0] = (byte) x;
        result[1] = (byte) (x >>> 8);
        result[2] = (byte) (x >>> 16);
        result[3] = (byte) (x >>> 24);
        return result;
    }

    public static void I2OSP(int value, byte[] output, int outOff) {
        output[outOff++] = (byte) value;
        output[outOff++] = (byte) (value >>> 8);
        output[outOff++] = (byte) (value >>> 16);
        output[outOff] = (byte) (value >>> 24);
    }


    public static void I2OSP(int value, byte[] output, int outOff, int outLen) {
        for (int i = outLen - 1; i >= 0; i--) {
            output[outOff + i] = (byte) (value >>> (8 * i));
        }
    }

    /**
     * Convert an integer to a byte array of length 8.
     *
     * @param input  the integer to convert
     * @param output byte array holding the output
     * @param outOff offset in output array where the result is stored
     */
    public static void I2OSP(long input, byte[] output, int outOff) {
        output[outOff++] = (byte) input;
        output[outOff++] = (byte) (input >>> 8);
        output[outOff++] = (byte) (input >>> 16);
        output[outOff++] = (byte) (input >>> 24);
        output[outOff++] = (byte) (input >>> 32);
        output[outOff++] = (byte) (input >>> 40);
        output[outOff++] = (byte) (input >>> 48);
        output[outOff] = (byte) (input >>> 56);
    }

    /**
     * Convert an int array to a byte array of the specified length. No length
     * checking is performed (i.e., if the last integer cannot be encoded with
     * <tt>length % 4</tt> octets, it is truncated).
     *
     * @param input  the int array
     * @param outLen the length of the converted array
     * @return the converted array
     */
    public static byte[] toByteArray(int[] input, int outLen) {
        int intLen = input.length;
        byte[] result = new byte[outLen];
        int index = 0;
        for (int i = 0; i <= intLen - 2; i++, index += 4) {
            I2OSP(input[i], result, index);
        }
        I2OSP(input[intLen - 1], result, index, outLen - index);
        return result;
    }

}
