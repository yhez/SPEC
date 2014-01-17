package de.flexiprovider.pqc.rainbow.util;

public class ComputeInField {

    private short[][] A;

    /**
     * Constructor with no parameters
     */
    public ComputeInField() {
    }


    public short[][] inverse(short[][] coef) {
        try {
            /** Initialization: **/
            short factor;
            short[][] inverse;
            A = new short[coef.length][2 * coef.length];
            if (coef.length != coef[0].length) {
                throw new RuntimeException(
                        "The matrix is not invertible. Please choose another one!");
            }

            /** prepare: Copy coef and the identity matrix into the global A. **/
            for (int i = 0; i < coef.length; i++) {
                System.arraycopy(coef[i], 0, A[i], 0, coef.length);
                // copy the identity matrix into A.
                for (int j = coef.length; j < 2 * coef.length; j++) {
                    A[i][j] = 0;
                }
                A[i][i + A.length] = 1;
            }

            /** Elimination operations to get the identity matrix from the left side of A. **/
            // modify A to get 0s under the diagonal.
            computeZerosUnder(true);

            // modify A to get only 1s on the diagonal: A[i][j] =A[i][j]/A[i][i].
            for (int i = 0; i < A.length; i++) {
                factor = GF2Field.invElem(A[i][i]);
                for (int j = i; j < 2 * A.length; j++) {
                    A[i][j] = GF2Field.multElem(A[i][j], factor);
                }
            }

            //modify A to get only 0s above the diagonal.
            computeZerosAbove();

            // copy the result (the second half of A) in the matrix inverse.
            inverse = new short[A.length][A.length];
            for (int i = 0; i < A.length; i++) {
                System.arraycopy(A[i], A.length, inverse[i], A.length - A.length, 2 * A.length - A.length);
            }
            return inverse;

        } catch (RuntimeException rte) {
            // The matrix is not invertible! A new one should be generated!
            return null;
        }
    }


    private void computeZerosUnder(boolean usedForInverse)
            throws RuntimeException {

        //the number of columns in the global A where the tmp results are stored
        int length;
        short tmp;

        //the function is used in inverse() - A should have 2 times more columns than rows
        if (usedForInverse) {
            length = 2 * A.length;
        }
        else {
            length = A.length + 1;
        }

        //elimination operations to modify A so that that it contains only 0s under the diagonal
        for (int k = 0; k < A.length - 1; k++) { // the fixed row
            for (int i = k + 1; i < A.length; i++) { // rows
                short factor1 = A[i][k];
                short factor2 = GF2Field.invElem(A[k][k]);

                //The element which multiplicative inverse is needed, is 0
                //in this case is the input matrix not invertible
                if (factor2 == 0) {
                    throw new RuntimeException("Matrix not invertible! We have to choose another one!");
                }

                for (int j = k; j < length; j++) {// columns
                    // tmp=A[k,j] / A[k,k]
                    tmp = GF2Field.multElem(A[k][j], factor2);
                    // tmp = A[i,k] * A[k,j] / A[k,k]
                    tmp = GF2Field.multElem(factor1, tmp);
                    // A[i,j]=A[i,j]-A[i,k]/A[k,k]*A[k,j];
                    A[i][j] = GF2Field.addElem(A[i][j], tmp);
                }
            }
        }
    }

    /**
     * Elimination above the diagonal.
     * This function changes a matrix so that it contains only zeros above the
     * diagonal(Ai,i) using only Gauss-Elimination operations.
     * <p/>
     * It is used in the inverse-function
     * The result is stored in the global matrix A
     *
     * @throws RuntimeException in case a multiplicative inverse of 0 is needed
     */
    private void computeZerosAbove() throws RuntimeException {
        short tmp;
        for (int k = A.length - 1; k > 0; k--) { // the fixed row
            for (int i = k - 1; i >= 0; i--) { // rows
                short factor1 = A[i][k];
                short factor2 = GF2Field.invElem(A[k][k]);
                if (factor2 == 0) {
                    throw new RuntimeException("The matrix is not invertible");
                }
                for (int j = k; j < 2 * A.length; j++) { // columns
                    // tmp = A[k,j] / A[k,k]
                    tmp = GF2Field.multElem(A[k][j], factor2);
                    // tmp = A[i,k] * A[k,j] / A[k,k]
                    tmp = GF2Field.multElem(factor1, tmp);
                    // A[i,j] = A[i,j] - A[i,k] / A[k,k] * A[k,j];
                    A[i][j] = GF2Field.addElem(A[i][j], tmp);
                }
            }
        }
    }


    public short[] addVect(short[] vector1, short[] vector2) {
        if (vector1.length != vector2.length) {
            throw new RuntimeException("Multiplication is not possible!");
        }
        short rslt[] = new short[vector1.length];
        for (int n = 0; n < rslt.length; n++)
            rslt[n] = GF2Field.addElem(vector1[n], vector2[n]);
        return rslt;
    }

    public short[][] multVects(short[] vector1, short[] vector2) {
        if (vector1.length != vector2.length) {
            throw new RuntimeException("Multiplication is not possible!");
        }
        short rslt[][] = new short[vector1.length][vector2.length];
        for (int i = 0; i < vector1.length; i++) {
            for (int j = 0; j < vector2.length; j++) {
                rslt[i][j] = GF2Field.multElem(vector1[i], vector2[j]);
            }
        }
        return rslt;
    }

    /**
     * Multiplies vector with scalar
     *
     * @param scalar galois element to multiply vector with
     * @param vector vector to be multiplied
     * @return vector multiplied with scalar
     */
    public short[] multVect(short scalar, short[] vector) {
        short rslt[] = new short[vector.length];
        for (int n = 0; n < rslt.length; n++)
            rslt[n] = GF2Field.multElem(scalar, vector[n]);
        return rslt;
    }

    public short[][] multMatrix(short scalar, short[][] matrix) {
        short[][] rslt = new short[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                rslt[i][j] = GF2Field.multElem(scalar, matrix[i][j]);
            }
        }
        return rslt;
    }

    public short[][] addSquareMatrix(short[][] matrix1, short[][] matrix2) {
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new RuntimeException("Addition is not possible!");
        }

        short[][] rslt = new short[matrix1.length][matrix1.length];//
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2.length; j++) {
                rslt[i][j] = GF2Field.addElem(matrix1[i][j], matrix2[i][j]);
            }
        }
        return rslt;
    }

}
