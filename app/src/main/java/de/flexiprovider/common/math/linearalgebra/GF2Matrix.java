package de.flexiprovider.common.math.linearalgebra;

import java.util.Arrays;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.common.util.IntUtils;
import de.flexiprovider.common.util.LittleEndianConversions;

public class GF2Matrix extends Matrix {


    private int[][] matrix;

    private int length;


    public GF2Matrix(int numColumns, int[][] matrix) {
        if (matrix[0].length != (numColumns + 31) >> 5) {
            throw new ArithmeticException(
                    "Int array does not match given number of columns.");
        }
        this.numColumns = numColumns;
        numRows = matrix.length;
        length = matrix[0].length;
        int rest = numColumns & 0x1f;
        int bitMask;
        if (rest == 0) {
            bitMask = 0xffffffff;
        } else {
            bitMask = (1 << rest) - 1;
        }
        for (int i = 0; i < numRows; i++) {
            matrix[i][length - 1] &= bitMask;
        }
        this.matrix = matrix;
    }

    /**
     * Create an nxn matrix of the given type.
     *
     * @param n            the matrix size
     * @param typeOfMatrix the matrix type
     * @param sr           the source of randomness
     */
    public GF2Matrix(int n, char typeOfMatrix, SecureRandom sr) {
        if (n <= 0) {
            throw new ArithmeticException("Size of matrix is non-positive.");
        }

        switch (typeOfMatrix) {

            case Matrix.MATRIX_TYPE_ZERO:
                assignZeroMatrix(n, n);
                break;

            case Matrix.MATRIX_TYPE_UNIT:
                assignUnitMatrix(n);
                break;

            case Matrix.MATRIX_TYPE_RANDOM_LT:
                assignRandomLowerTriangularMatrix(n, sr);
                break;

            case Matrix.MATRIX_TYPE_RANDOM_UT:
                assignRandomUpperTriangularMatrix(n, sr);
                break;

            case Matrix.MATRIX_TYPE_RANDOM_REGULAR:
                assignRandomRegularMatrix(n, sr);
                break;

            default:
                throw new ArithmeticException("Unknown matrix type.");
        }
    }

    /**
     * create the mxn zero matrix
     */
    private GF2Matrix(int m, int n) {
        if ((n <= 0) || (m <= 0)) {
            throw new ArithmeticException("size of matrix is non-positive");
        }

        assignZeroMatrix(m, n);
    }

    /**
     * Create the mxn zero matrix.
     *
     * @param m number of rows
     * @param n number of columns
     */
    private void assignZeroMatrix(int m, int n) {
        numRows = m;
        numColumns = n;
        length = (n + 31) >>> 5;
        matrix = new int[numRows][length];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < length; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    /**
     * Create the mxn unit matrix.
     *
     * @param n number of rows (and columns)
     */
    private void assignUnitMatrix(int n) {
        numRows = n;
        numColumns = n;
        length = (n + 31) >>> 5;
        matrix = new int[numRows][length];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < length; j++) {
                matrix[i][j] = 0;
            }
        }
        for (int i = 0; i < numRows; i++) {
            int rest = i & 0x1f;
            matrix[i][i >>> 5] = 1 << rest;
        }
    }

    /**
     * Create a nxn random lower triangular matrix.
     *
     * @param n  number of rows (and columns)
     * @param sr source of randomness
     */
    private void assignRandomLowerTriangularMatrix(int n, SecureRandom sr) {
        numRows = n;
        numColumns = n;
        length = (n + 31) >>> 5;
        matrix = new int[numRows][length];
        for (int i = 0; i < numRows; i++) {
            int q = i >>> 5;
            int r = i & 0x1f;
            int s = 31 - r;
            r = 1 << r;
            for (int j = 0; j < q; j++) {
                matrix[i][j] = sr.nextInt();
            }
            matrix[i][q] = (sr.nextInt() >>> s) | r;
            for (int j = q + 1; j < length; j++) {
                matrix[i][j] = 0;
            }

        }

    }

    /**
     * Create a nxn random upper triangular matrix.
     *
     * @param n  number of rows (and columns)
     * @param sr source of randomness
     */
    private void assignRandomUpperTriangularMatrix(int n, SecureRandom sr) {
        numRows = n;
        numColumns = n;
        length = (n + 31) >>> 5;
        matrix = new int[numRows][length];
        int rest = n & 0x1f;
        int help;
        if (rest == 0) {
            help = 0xffffffff;
        } else {
            help = (1 << rest) - 1;
        }
        for (int i = 0; i < numRows; i++) {
            int q = i >>> 5;
            int r = i & 0x1f;
            int s = r;
            r = 1 << r;
            for (int j = 0; j < q; j++) {
                matrix[i][j] = 0;
            }
            matrix[i][q] = (sr.nextInt() << s) | r;
            for (int j = q + 1; j < length; j++) {
                matrix[i][j] = sr.nextInt();
            }
            matrix[i][length - 1] &= help;
        }

    }

    /**
     * Create an nxn random regular matrix.
     *
     * @param n  number of rows (and columns)
     * @param sr source of randomness
     */
    private void assignRandomRegularMatrix(int n, SecureRandom sr) {
        numRows = n;
        numColumns = n;
        length = (n + 31) >>> 5;
        matrix = new int[numRows][length];
        GF2Matrix lm = new GF2Matrix(n, Matrix.MATRIX_TYPE_RANDOM_LT, sr);
        GF2Matrix um = new GF2Matrix(n, Matrix.MATRIX_TYPE_RANDOM_UT, sr);
        GF2Matrix rm = (GF2Matrix) lm.rightMultiply(um);
        Permutation perm = new Permutation(n, sr);
        int[] p = perm.getVector();
        for (int i = 0; i < n; i++) {
            System.arraycopy(rm.matrix[i], 0, matrix[p[i]], 0, length);
        }
    }

    /**
     * @return the length of each array representing a row of this matrix
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns encoded matrix, i.e., this matrix in byte array form
     *
     * @return the encoded matrix
     */
    public byte[] getEncoded() {
        int n = (numColumns + 7) >>> 3;
        n *= numRows;
        n += 8;
        byte[] enc = new byte[n];

        LittleEndianConversions.I2OSP(numRows, enc, 0);
        LittleEndianConversions.I2OSP(numColumns, enc, 4);

        // number of "full" integer
        int q = numColumns >>> 5;
        // number of bits in non-full integer
        int r = numColumns & 0x1f;

        int count = 8;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < q; j++, count += 4) {
                LittleEndianConversions.I2OSP(matrix[i][j], enc, count);
            }
            for (int j = 0; j < r; j += 8) {
                enc[count++] = (byte) ((matrix[i][q] >>> j) & 0xff);
            }

        }
        return enc;
    }


    /**
     * Check if this is the zero matrix (i.e., all entries are zero).
     *
     * @return <tt>true</tt> if this is the zero matrix
     */
    public boolean isZero() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < length; j++) {
                if (matrix[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get the quadratic submatrix of this matrix consisting of the leftmost
     * <tt>numRows</tt> columns.
     *
     * @return the <tt>(numRows x numRows)</tt> submatrix
     */
    public GF2Matrix getLeftSubMatrix() {
        if (numColumns <= numRows) {
            throw new ArithmeticException("empty submatrix");
        }
        int length = (numRows + 31) >> 5;
        int[][] result = new int[numRows][length];
        int bitMask = (1 << (numRows & 0x1f)) - 1;
        if (bitMask == 0) {
            bitMask = -1;
        }
        for (int i = numRows - 1; i >= 0; i--) {
            System.arraycopy(matrix[i], 0, result[i], 0, length);
            result[i][length - 1] &= bitMask;
        }
        return new GF2Matrix(numRows, result);
    }

    /**
     * Get the submatrix of this matrix consisting of the rightmost
     * <tt>numColumns-numRows</tt> columns.
     *
     * @return the <tt>(numRows x (numColumns-numRows))</tt> submatrix
     */
    public GF2Matrix getRightSubMatrix() {
        if (numColumns <= numRows) {
            throw new ArithmeticException("empty submatrix");
        }

        int q = numRows >> 5;
        int r = numRows & 0x1f;

        GF2Matrix result = new GF2Matrix(numRows, numColumns - numRows);

        for (int i = numRows - 1; i >= 0; i--) {
            // if words have to be shifted
            if (r != 0) {
                int ind = q;
                // process all but last word
                for (int j = 0; j < result.length - 1; j++) {
                    // shift to correct position
                    result.matrix[i][j] = (matrix[i][ind++] >>> r)
                            | (matrix[i][ind] << (32 - r));
                }
                // process last word
                result.matrix[i][result.length - 1] = matrix[i][ind++] >>> r;
                if (ind < length) {
                    result.matrix[i][result.length - 1] |= matrix[i][ind] << (32 - r);
                }
            } else {
                // no shifting necessary
                System.arraycopy(matrix[i], q, result.matrix[i], 0,
                        result.length);
            }
        }
        return result;
    }

    /**
     * Compute the inverse of this matrix.
     *
     * @return the inverse of this matrix (newly created).
     * @throws ArithmeticException if this matrix is not invertible.
     */
    public Matrix computeInverse() {
        if (numRows != numColumns) {
            throw new ArithmeticException("Matrix is not invertible.");
        }

        // clone this matrix
        int[][] tmpMatrix = new int[numRows][length];
        for (int i = numRows - 1; i >= 0; i--) {
            tmpMatrix[i] = IntUtils.clone(matrix[i]);
        }

        // initialize inverse matrix as unit matrix
        int[][] invMatrix = new int[numRows][length];
        for (int i = numRows - 1; i >= 0; i--) {
            int q = i >> 5;
            int r = i & 0x1f;
            invMatrix[i][q] = 1 << r;
        }

        // simultaneously compute Gaussian reduction of tmpMatrix and unit
        // matrix
        for (int i = 0; i < numRows; i++) {
            // i = q * 32 + (i mod 32)
            int q = i >> 5;
            int bitMask = 1 << (i & 0x1f);
            // if diagonal element is zero
            if ((tmpMatrix[i][q] & bitMask) == 0) {
                boolean foundNonZero = false;
                // find a non-zero element in the same column
                for (int j = i + 1; j < numRows; j++) {
                    if ((tmpMatrix[j][q] & bitMask) != 0) {
                        // found it, swap rows ...
                        foundNonZero = true;
                        swapRows(tmpMatrix, i, j);
                        swapRows(invMatrix, i, j);
                        // ... and quit searching
                        j = numRows;
                    }
                }
                // if no non-zero element was found ...
                if (!foundNonZero) {
                    // ... the matrix is not invertible
                    throw new ArithmeticException("Matrix is not invertible.");
                }
            }

            // normalize all but i-th row
            for (int j = numRows - 1; j >= 0; j--) {
                if ((j != i) && ((tmpMatrix[j][q] & bitMask) != 0)) {
                    addToRow(tmpMatrix[i], tmpMatrix[j], q);
                    addToRow(invMatrix[i], invMatrix[j], 0);
                }
            }
        }

        return new GF2Matrix(numColumns, invMatrix);
    }

    public Matrix rightMultiply(Matrix mat) {
        if (!(mat instanceof GF2Matrix)) {
            throw new ArithmeticException("matrix is not defined over GF(2)");
        }

        if (mat.numRows != numColumns) {
            throw new ArithmeticException("length mismatch");
        }

        GF2Matrix a = (GF2Matrix) mat;
        GF2Matrix result = new GF2Matrix(numRows, mat.numColumns);

        int d;
        int rest = numColumns & 0x1f;
        if (rest == 0) {
            d = length;
        } else {
            d = length - 1;
        }
        for (int i = 0; i < numRows; i++) {
            int count = 0;
            for (int j = 0; j < d; j++) {
                int e = matrix[i][j];
                for (int h = 0; h < 32; h++) {
                    int b = e & (1 << h);
                    if (b != 0) {
                        for (int g = 0; g < a.length; g++) {
                            result.matrix[i][g] ^= a.matrix[count][g];
                        }
                    }
                    count++;
                }
            }
            int e = matrix[i][length - 1];
            for (int h = 0; h < rest; h++) {
                int b = e & (1 << h);
                if (b != 0) {
                    for (int g = 0; g < a.length; g++) {
                        result.matrix[i][g] ^= a.matrix[count][g];
                    }
                }
                count++;
            }

        }

        return result;
    }

    /**
     * Compute the product of this matrix and a permutation matrix which is
     * generated from an n-permutation.
     *
     * @param p the permutation
     * @return {@link GF2Matrix} <tt>this*P</tt>
     */
    public Matrix rightMultiply(Permutation p) {

        int[] pVec = p.getVector();
        if (pVec.length != numColumns) {
            throw new ArithmeticException("length mismatch");
        }

        GF2Matrix result = new GF2Matrix(numRows, numColumns);

        for (int i = numColumns - 1; i >= 0; i--) {
            int q = i >>> 5;
            int r = i & 0x1f;
            int pq = pVec[i] >>> 5;
            int pr = pVec[i] & 0x1f;
            for (int j = numRows - 1; j >= 0; j--) {
                result.matrix[j][q] |= ((matrix[j][pq] >>> pr) & 1) << r;
            }
        }

        return result;
    }

    public boolean equals(Object other) {

        if (!(other instanceof GF2Matrix)) {
            return false;
        }
        GF2Matrix otherMatrix = (GF2Matrix) other;

        if ((numRows != otherMatrix.numRows)
                || (numColumns != otherMatrix.numColumns)
                || (length != otherMatrix.length)) {
            return false;
        }

        for (int i = 0; i < numRows; i++) {
            if (!IntUtils.equals(matrix[i], otherMatrix.matrix[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return the hash code of this matrix
     */
    public int hashCode() {
        int hash = (numRows * 31 + numColumns) * 31 + length;
        for (int i = 0; i < numRows; i++) {
            hash = hash * 31 + Arrays.hashCode(matrix[i]);
        }
        return hash;
    }

    /**
     * @return a human readable form of the matrix
     */
    public String toString() {
        int rest = numColumns & 0x1f;
        int d;
        if (rest == 0) {
            d = length;
        } else {
            d = length - 1;
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < numRows; i++) {
            buf.append(i).append(": ");
            for (int j = 0; j < d; j++) {
                int a = matrix[i][j];
                for (int k = 0; k < 32; k++) {
                    int b = (a >>> k) & 1;
                    if (b == 0) {
                        buf.append('0');
                    } else {
                        buf.append('1');
                    }
                }
                buf.append(' ');
            }
            int a = matrix[i][length - 1];
            for (int k = 0; k < rest; k++) {
                int b = (a >>> k) & 1;
                if (b == 0) {
                    buf.append('0');
                } else {
                    buf.append('1');
                }
            }
            buf.append('\n');
        }

        return buf.toString();
    }

    /**
     * Swap two rows of the given matrix.
     *
     * @param matrix the matrix
     * @param first  the index of the first row
     * @param second the index of the second row
     */
    private static void swapRows(int[][] matrix, int first, int second) {
        int[] tmp = matrix[first];
        matrix[first] = matrix[second];
        matrix[second] = tmp;
    }

    /**
     * Partially add one row to another.
     *
     * @param fromRow    the addend
     * @param toRow      the row to add to
     * @param startIndex the array index to start from
     */
    private static void addToRow(int[] fromRow, int[] toRow, int startIndex) {
        for (int i = toRow.length - 1; i >= startIndex; i--) {
            toRow[i] = fromRow[i] ^ toRow[i];
        }
    }

}
