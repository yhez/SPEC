package de.flexiprovider.common.util;

import de.flexiprovider.common.math.FlexiBigInt;

public final class FlexiBigIntUtils {

    /**
     * Default constructor (private).
     */
    private FlexiBigIntUtils() {
        // empty
    }

    /**
     * Return the value of <tt>big</tt> as a byte array. Although FlexiBigInt
     * has such a method, it uses an extra bit to indicate the sign of the
     * number. For elliptic curve cryptography, the numbers usually are
     * positive. Thus, this helper method returns a byte array of minimal
     * length, ignoring the sign of the number.
     *
     * @param value the <tt>FlexiBigInt</tt> value to be converted to a byte
     *              array
     * @return the value <tt>big</tt> as byte array
     */
    public static byte[] toMinimalByteArray(FlexiBigInt value) {
        byte[] valBytes = value.toByteArray();
        if ((valBytes.length == 1) || (value.bitLength() & 0x07) != 0) {
            return valBytes;
        }
        byte[] result = new byte[value.bitLength() >> 3];
        System.arraycopy(valBytes, 1, result, 0, result.length);
        return result;
    }

}
