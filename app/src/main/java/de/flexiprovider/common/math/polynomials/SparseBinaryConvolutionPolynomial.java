package de.flexiprovider.common.math.polynomials;

import java.util.Arrays;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.util.BigEndianConversions;
import de.flexiprovider.common.util.IntUtils;

public class SparseBinaryConvolutionPolynomial implements ConvolutionPolynomial {

    int N;

    int[] degrees;

    public SparseBinaryConvolutionPolynomial(final int N, final int d,
                                             SecureRandom sr) {

        if (N <= 0) {
            this.N = 1;
        } else {
            this.N = N;
        }

        int degree = d;
        if (degree < 0) {
            degree = 0;
        }
        if (degree > N + 1) {
            degree = N + 1;
        }

        degrees = new int[degree];
        int[] index = new int[N];
        for (int i = N - 1; i >= 0; i--) {
            index[i] = i;
        }

        int k = N;
        for (int i = 0; i < d; i++) {
            int n = sr.nextInt(k);
            degrees[i] = index[n];
            k--;
            index[n] = index[k];
        }

        IntUtils.quicksort(degrees);
    }

    /**
     * Copy constructor.
     *
     * @param other another <tt>SparseBinaryPolynomial</tt>
     */
    public SparseBinaryConvolutionPolynomial(
            final SparseBinaryConvolutionPolynomial other) {
        N = other.N;
        degrees = IntUtils.clone(other.degrees);
    }

    /**
     * Create an empty sparse binary polynomial ring element of
     * <tt>Z(X)/(X^N-1)</tt>.
     */
    private SparseBinaryConvolutionPolynomial(int N) {
        this.N = N;
    }

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
     * Encode this polynomial as a byte array.
     *
     * @return the encoded polynomial
     */
    public byte[] RE2OSP() {
        int oLen = IntegerFunctions.ceilLog256(N - 1);
        byte[] result = new byte[oLen * degrees.length];
        for (int i = 0; i < degrees.length; i++) {
            byte[] os = BigEndianConversions.I2OSP(degrees[degrees.length - 1
                    - i], oLen);
            System.arraycopy(os, 0, result, i * oLen, oLen);
        }
        return result;
    }

    /**
     * Create a sparse binary polynomial out of the given byte array.
     *
     * @param N       the degree of the reduction polynomial
     * @param d       the number of non-zero coefficients
     * @param encoded an encoded sparse binary polynomial
     * @return the decoded polynomial
     * @throws IllegalArgumentException if the encoded polynomial has wrong length.
     */
    public static SparseBinaryConvolutionPolynomial OS2REP(int N, int d,
                                                           byte[] encoded) throws IllegalArgumentException {
        int oLen = IntegerFunctions.ceilLog256(N - 1);
        if (encoded.length != oLen * d) {
            throw new IllegalArgumentException(
                    "Encoded sparse binary polynomial has wrong length.");
        }

        // create result polynomial
        SparseBinaryConvolutionPolynomial result = new SparseBinaryConvolutionPolynomial(
                N);

        // create degree array
        result.degrees = new int[d];
        for (int i = 0; i < d; i++) {
            result.degrees[d - 1 - i] = BigEndianConversions.OS2IP(encoded, i
                    * oLen, oLen);
        }

        // sorting is not necessary (since it was encoded the right way)

        // return result
        return result;
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
