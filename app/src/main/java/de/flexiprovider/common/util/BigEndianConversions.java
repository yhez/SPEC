package de.flexiprovider.common.util;

public final class BigEndianConversions {

    private BigEndianConversions() {
        // empty
    }


    public static byte[] I2OSP(int x) {
        byte[] result = new byte[4];
        result[0] = (byte) (x >>> 24);
        result[1] = (byte) (x >>> 16);
        result[2] = (byte) (x >>> 8);
        result[3] = (byte) x;
        return result;
    }

    public static void I2OSP(int input, byte[] output, int outOff) {
        output[outOff++] = (byte) (input >>> 24);
        output[outOff++] = (byte) (input >>> 16);
        output[outOff++] = (byte) (input >>> 8);
        output[outOff] = (byte) input;
    }

    public static void I2OSP(long input, byte[] output, int outOff) {
        output[outOff++] = (byte) (input >>> 56);
        output[outOff++] = (byte) (input >>> 48);
        output[outOff++] = (byte) (input >>> 40);
        output[outOff++] = (byte) (input >>> 32);
        output[outOff++] = (byte) (input >>> 24);
        output[outOff++] = (byte) (input >>> 16);
        output[outOff++] = (byte) (input >>> 8);
        output[outOff] = (byte) input;
    }


    public static int OS2IP(byte[] input, int inOff) {
        int result = (input[inOff++] & 0xff) << 24;
        result |= (input[inOff++] & 0xff) << 16;
        result |= (input[inOff++] & 0xff) << 8;
        result |= input[inOff] & 0xff;
        return result;
    }

    public static long OS2LIP(byte[] input, int inOff) {
        long result = ((long) input[inOff++] & 0xff) << 56;
        result |= ((long) input[inOff++] & 0xff) << 48;
        result |= ((long) input[inOff++] & 0xff) << 40;
        result |= ((long) input[inOff++] & 0xff) << 32;
        result |= ((long) input[inOff++] & 0xff) << 24;
        result |= (input[inOff++] & 0xff) << 16;
        result |= (input[inOff++] & 0xff) << 8;
        result |= input[inOff] & 0xff;
        return result;
    }

}
