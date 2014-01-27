package de.flexiprovider.common.math.linearalgebra;

import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.util.IntUtils;
import de.flexiprovider.common.util.LittleEndianConversions;

public class Permutation {


    private int[] perm;


    public byte[] getEncoded() {
        int n = perm.length;
        int size = IntegerFunctions.ceilLog256(n - 1);
        byte[] result = new byte[4 + n * size];
        LittleEndianConversions.I2OSP(n, result, 0);
        for (int i = 0; i < n; i++) {
            LittleEndianConversions.I2OSP(perm[i], result, 4 + i * size, size);
        }
        return result;
    }


    public int[] getVector() {
        return IntUtils.clone(perm);
    }

    public boolean equals(Object other) {

        if (!(other instanceof Permutation)) {
            return false;
        }
        Permutation otherPerm = (Permutation) other;

        return IntUtils.equals(perm, otherPerm.perm);
    }


    public String toString() {
        String result = "[" + perm[0];
        for (int i = 1; i < perm.length; i++) {
            result += ", " + perm[i];
        }
        result += "]";
        return result;
    }


    public int hashCode() {
        return perm.hashCode();
    }

}
