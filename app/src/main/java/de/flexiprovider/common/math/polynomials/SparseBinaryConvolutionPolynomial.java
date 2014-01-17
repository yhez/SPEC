package de.flexiprovider.common.math.polynomials;

import java.util.Arrays;

import de.flexiprovider.common.util.IntUtils;

public class SparseBinaryConvolutionPolynomial implements ConvolutionPolynomial {

    int N;

    int[] degrees;

    /*
     * Public methods
     */

    /**
     * Compute an array of bit pattern locations (minimal size).
     *
     * @return an array of bit pattern locations
     */
    public int[][] getPatterns() {
        int d = degrees.length;

        int[] numPatterns = new int[N - d + 1];
        for (int pos = d - 1; pos > 0; ) {
            numPatterns[degrees[pos--] - degrees[pos--]]++;
        }

        int[][] L = new int[N - d + 1][];
        int currentPos = d - 1;
        while (currentPos > 0) {
            int dist = degrees[currentPos] - degrees[currentPos - 1];
            if (L[dist] == null) {
                L[dist] = new int[numPatterns[dist] + 1];
            }
            L[dist][L[dist][0]++ + 1] = degrees[currentPos];
            currentPos -= 2;
        }

        if (currentPos == 0) {
            L[0] = new int[]{degrees[0]};
        }

        return L;
    }

    /**
     * Compare this polynomial with the given object.
     *
     * @param other the other object
     * @return the result of the comparison
     */
    public boolean equals(Object other) {
        if (other == null
                || !(other instanceof SparseBinaryConvolutionPolynomial)) {
            return false;
        }

        SparseBinaryConvolutionPolynomial otherPol = (SparseBinaryConvolutionPolynomial) other;

        return (N == otherPol.N) && IntUtils.equals(degrees, otherPol.degrees);

    }

    /**
     * @return the hash code of this polynomial
     */
    public int hashCode() {
        return N + Arrays.hashCode(degrees);
    }

    /**
     * @return a human readable form of this polynomial
     */
    public String toString() {
        if (degrees.length == 0) {
            return "SparseBinaryConvolutionPolynomial (degree 0):\n0*x^0";
        }
        String result = "SparseBinaryConvolutionPolynomial (degree "
                + degrees[degrees.length - 1] + "):\n";
        result += "x^" + degrees[degrees.length - 1];
        for (int i = degrees.length - 2; i >= 0; i--) {
            result += " + x^" + degrees[i];
        }

        // alternative representation (bit string)
        result += "\n";
        int numZeroes;
        for (int i = degrees.length - 1; i >= 1; i--) {
            result += "1";
            numZeroes = degrees[i] - degrees[i - 1] - 1;
            for (int j = numZeroes - 1; j >= 0; j--) {
                result += "0";
            }
        }
        result += "1";
        for (int j = degrees[0] - 1; j >= 0; j--) {
            result += "0";
        }

        return result;
    }

}
