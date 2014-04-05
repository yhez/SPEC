package de.flexiprovider.common.util;

import de.flexiprovider.common.math.FlexiBigInt;

public final class FlexiBigIntUtils {

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
