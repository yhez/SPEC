package de.flexiprovider.common.math.linearalgebra;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.util.IntUtils;
import de.flexiprovider.common.util.LittleEndianConversions;

public class Permutation {

    /**
     * perm holds the elements of the permutation vector, i.e. <tt>[perm(0),
     * perm(1), ..., perm(n-1)]</tt>
     */
    private int[] perm;


    public Permutation(int n, SecureRandom sr) {
        if (n <= 0) {
            throw new IllegalArgumentException("invalid length");
        }

        perm = new int[n];

        int[] help = new int[n];
        for (int i = 0; i < n; i++) {
            help[i] = i;
        }

        int k = n;
        for (int j = 0; j < n; j++) {
            int i = sr.nextInt(k);
            k--;
            perm[j] = help[i];
            help[i] = help[k];
        }
    }
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

    /**
     * @return the permutation vector <tt>(perm(0),perm(1),...,perm(n-1))</tt>
     */
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

    /**
     * @return a human readable form of the permutation
     */
    public String toString() {
        String result = "[" + perm[0];
        for (int i = 1; i < perm.length; i++) {
            result += ", " + perm[i];
        }
        result += "]";
        return result;
    }

    /**
     * @return the hash code of this permutation
     */
    public int hashCode() {
        return perm.hashCode();
    }

}
