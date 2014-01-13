package de.flexiprovider.common.math.polynomials;

import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.util.IntUtils;

/**
 * This class implements convolution polynomials in the ring
 * <tt>(Z/qZ)/(X^N-1)</tt> and their arithmetic , where <tt>q</tt> and
 * <tt>N</tt> lie in the interval <tt>[2, Integer.MAX_VALUE]</tt>.
 *
 * @author Martin Dring
 */
public class ModQConvolutionPolynomial implements ConvolutionPolynomial {

    /*
     * Private fields
     */

    /**
     * The degree of the reduction polynomial
     */
    private int N;

    /**
     * The modulus
     */
    private int q;

    /**
     * The degree of the polynomial
     */
    private int degree;

    /**
     * The coefficient array. The coefficients are stored in ascending order
     * (i.e. <tt>coefficients[i]</tt> holds the <tt>i</tt>th coefficient.
     */
    private int[] coefficients;

    /*
     * Constructors
     */

    /**
     * Construct a polynomial of the given degree. The only non-zero coefficient
     * will be the head coefficient which is set to <tt>headCoef</tt>.
     *
     * @param N        the degree of the reduction polynomial
     * @param q        the modulus
     * @param degree   the degree
     * @param headCoef the head coefficient
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public ModQConvolutionPolynomial(int N, int q, int degree, int headCoef)
            throws IllegalArgumentException {

        if (N < 1 || q == 0 || q == 1 || degree < 0) {
            throw new IllegalArgumentException();
        }

        this.N = N;
        if (q < 0) {
            this.q = -q;
        } else {
            this.q = q;
        }
        this.degree = degree;
        coefficients = new int[N];
        coefficients[degree] = headCoef % q;
    }

    /**
     * Construct a polynomial out of the given coefficient array. The
     * coefficients are reduced modulo <tt>q</tt> into the interval
     * <tt>[0, q)</tt>..
     *
     * @param N            the degree of the reduction polynomial
     * @param q            the modulus
     * @param coefficients the coefficient array
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public ModQConvolutionPolynomial(int N, int q, int[] coefficients)
            throws IllegalArgumentException {

        if (N < 1 || q == 0 || q == 1 || coefficients.length > N) {
            throw new IllegalArgumentException();
        }
        this.N = N;
        if (q < 0) {
            this.q = -q;
        } else {
            this.q = q;
        }
        this.coefficients = new int[N];
        System.arraycopy(coefficients, 0, this.coefficients, 0,
                coefficients.length);
        reduceCoeffThis();
        computeDegree();
    }

    /**
     * Copy constructor
     *
     * @param other another <tt>ModQPolynomial</tt>
     */
    public ModQConvolutionPolynomial(ModQConvolutionPolynomial other) {
        N = other.N;
        q = other.q;
        degree = other.degree;
        coefficients = IntUtils.clone(other.coefficients);
    }

    /*
     * Public methods
     */

    /**
     * Add the addend to this polynomial.
     *
     * @param addend the addend
     * @return <tt>this + addend</tt> (newly created)
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring.
     */
    public ModQConvolutionPolynomial add(ModQConvolutionPolynomial addend) {
        ModQConvolutionPolynomial result = new ModQConvolutionPolynomial(this);
        result.addThis(addend);
        return result;
    }

    /**
     * Add the addend to this polynomial (overwriting this polynomial).
     *
     * @param addend the addend
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring.
     */
    public void addThis(ModQConvolutionPolynomial addend) {
        if (N != addend.N || q != addend.q) {
            throw new ArithmeticException(
                    "Polynomials are not defined over the same ring.");
        }

        for (int i = N - 1; i >= 0; i--) {
            coefficients[i] += addend.coefficients[i];
            if (coefficients[i] >= q) {
                coefficients[i] -= q;
            }
        }

        computeDegree();
    }

    /**
     * Subtract the minuend from this polynomial.
     *
     * @param minuend the minuend
     * @return <tt>this - minuend</tt> (newly created)
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring.
     */
    public ModQConvolutionPolynomial subtract(ModQConvolutionPolynomial minuend) {
        ModQConvolutionPolynomial result = new ModQConvolutionPolynomial(this);
        result.subtractThis(minuend);
        return result;
    }

    /**
     * Subtract the minuend from this polynomial (overwriting this polynomial).
     *
     * @param minuend the minuend
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring.
     */
    public void subtractThis(ModQConvolutionPolynomial minuend) {
        if (N != minuend.N || q != minuend.q) {
            throw new ArithmeticException(
                    "Polynomials are not defined over the same ring.");
        }

        for (int i = N - 1; i >= 0; i--) {
            coefficients[i] -= minuend.coefficients[i];
            if (coefficients[i] < 0) {
                coefficients[i] += q;
            }
        }

        computeDegree();
    }

    /**
     * Adapter method: multiply this polynomial with the factor.
     *
     * @param factor the factor
     * @return <tt>this * factor</tt> (newly created)
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring or if the type of the factor is unknown.
     */
    public ModQConvolutionPolynomial multiply(ConvolutionPolynomial factor)
            throws ArithmeticException {

        if (factor instanceof SparseBinaryConvolutionPolynomial) {
            return multiplyPatterns((SparseBinaryConvolutionPolynomial) factor);

        } else if (factor instanceof ProductFormConvolutionPolynomial) {
            return multiply((ProductFormConvolutionPolynomial) factor);

        } else if (factor instanceof ModQConvolutionPolynomial) {
            return multiply((ModQConvolutionPolynomial) factor);

        } else {
            throw new ArithmeticException("Unknown polynomial type.");
        }
    }

    /**
     * Multiply this polynomial with the factor and reduce the result modulo
     * <tt>X^N-1</tt>. This algorithm is described in IEEE P1363.1-D9,
     * Section 6.2.5, Algorithm 1.
     *
     * @param factor the factor
     * @return <tt>this * factor mod X^N-1</tt> (newly created)
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring.
     */
    public ModQConvolutionPolynomial multiply(
            SparseBinaryConvolutionPolynomial factor) {

        if (N != factor.N) {
            throw new ArithmeticException(
                    "Polynomials are not defined over the same ring.");
        }

        int[] a = coefficients;
        int[] b = factor.degrees;
        int df = b.length;

        // step a)
        int[] coeff = new int[N];

        // step b)
        int i, j, k;

        // step c)
        for (i = 0; i < df; i++) {
            // step 1)
            j = b[i];
            // step 2)
            for (k = 0; k <= degree; k++) {
                // step i)
                coeff[j++] += a[k];
                if (j == N) {
                    j = 0;
                }
            }
        }

        // steps d) and e)
        return new ModQConvolutionPolynomial(N, q, coeff);
    }

    /**
     * Multiply this polynomial with the factor and reduce the result modulo
     * <tt>X^N-1</tt>. This algorithm is described in IEEE P1363.1-D9,
     * Section 6.2.6, Algorithm 2.
     *
     * @param factor the factor
     * @return <tt>this * factor mod X^N-1</tt> (newly created)
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring.
     */
    public ModQConvolutionPolynomial multiply(
            ProductFormConvolutionPolynomial factor) {

        if (N != factor.N) {
            throw new ArithmeticException(
                    "Polynomials are not defined over the same ring.");
        }

        int[] a = coefficients;
        int[] b1 = factor.f1.degrees;
        int[] b2 = factor.f2.degrees;
        int[] b3 = factor.f3.degrees;

        int[] coeff = new int[N];
        int[] temp = new int[N];

        int i, index, k;
        for (i = b1.length - 1; i >= 0; i--) {
            index = b1[i];
            for (k = 0; k <= degree; k++) {
                temp[index++] += a[k];
                if (index == N) {
                    index = 0;
                }
            }
        }

        for (i = b2.length - 1; i >= 0; i--) {
            index = b2[i];
            for (k = 0; k < N; k++) {
                coeff[index++] += temp[k];
                if (index == N) {
                    index = 0;
                }
            }
        }

        temp = new int[N];
        for (i = b3.length - 1; i >= 0; i--) {
            index = b3[i];
            for (k = 0; k <= degree; k++) {
                temp[index++] += a[k];
                if (index == N) {
                    index = 0;
                }
            }
        }

        coeff = add(coeff, temp);

        return new ModQConvolutionPolynomial(N, q, coeff);
    }

    /**
     * Multiply this polynomial with the factor.
     *
     * @param factor the factor
     * @return <tt>this * factor</tt> (newly created)
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring.
     */
    public ModQConvolutionPolynomial multiply(ModQConvolutionPolynomial factor)
            throws ArithmeticException {
        ModQConvolutionPolynomial result = new ModQConvolutionPolynomial(this);
        result.multiplyThis(factor);
        return result;
    }

    /**
     * Multiply this polynomial with the factor (overwriting this polynomial).
     *
     * @param factor the factor
     * @throws ArithmeticException if this polynomial and the factor are not elements of the
     *                             same ring.
     */
    public void multiplyThis(ModQConvolutionPolynomial factor)
            throws ArithmeticException {

        if (N != factor.N || q != factor.q) {
            throw new ArithmeticException(
                    "Polynomials are not defined over the same ring.");
        }

        int[] temp = new int[N];
        int resIndex;
        for (int i = 0; i <= degree; i++) {
            resIndex = i;
            for (int j = 0; j <= factor.degree; j++) {
                temp[resIndex++] += coefficients[i] * factor.coefficients[j];
                if (resIndex == N) {
                    resIndex = 0;
                }
            }
        }

        coefficients = temp;
        reduceCoeffThis();
        computeDegree();
    }

    public ModQConvolutionPolynomial multiplyPatterns(
            SparseBinaryConvolutionPolynomial factor)
            throws ArithmeticException {

        if (N != factor.N) {
            throw new ArithmeticException(
                    "Polynomials are not defined over the same ring.");
        }

        int[] coeff = new int[N];

        final int[][] L = factor.getPatterns();
        int[] P = new int[N];

        for (int i = L.length - 1; i >= 1; i--) {
            if (L[i] != null) {
                // compute this + this*x^i
                int index = i;
                for (int j = 0; j < N; j++) {
                    P[j] = coefficients[j] + coefficients[index++];
                    if (index == N) {
                        index = 0;
                    }
                }

                // multiply using polynomial P computed above
                for (int j = L[i][0] - 1; j >= 0; j--) {
                    index = L[i][j + 1];
                    for (int k = 0; k < N; k++) {
                        coeff[index++] += P[k];
                        if (index == N) {
                            index = 0;
                        }
                    }
                }
            }
        }

        // treat possibly remaining single coefficient
        if (L[0] != null) {
            int index = L[0][0];
            for (int k = 0; k < N; k++) {
                coeff[index++] += coefficients[k];
                if (index == N) {
                    index = 0;
                }
            }
        }

        // create and return result polynomial
        return new ModQConvolutionPolynomial(N, q, coeff);
    }

    /**
     * Compute the inverse of this polynomial in the ring
     * <tt>(Z/qZ)[X]/(X^N-1)</tt>.
     *
     * @return the inverse of this polynomial if it is invertible, <tt>null</tt>
     * otherwise.
     */
    public ModQConvolutionPolynomial invert() {
        final ModQConvolutionPolynomial unit = new ModQConvolutionPolynomial(N,
                q, 0, 1);
        ModQConvolutionPolynomial[] g = extGCD();
        if (g[0].equals(unit)) {
            return g[1];
        }
        return null;
    }

    /**
     * Compare this polynomial with the given object.
     *
     * @param other the other object
     * @return the result of the comparison
     */
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ModQConvolutionPolynomial)) {
            return false;
        }

        ModQConvolutionPolynomial otherPol = (ModQConvolutionPolynomial) other;

        return (degree == otherPol.degree) && (q == otherPol.q)
                && IntUtils.equals(coefficients, otherPol.coefficients);

    }

    /**
     * @return the hash code of this polynomial
     */
    public int hashCode() {
        return degree + q + coefficients.hashCode();
    }

    /**
     * @return a human readable form of the polynomial
     */
    public String toString() {
        String result = "ModQPolynomial (degree " + degree + ", modulus " + q
                + "):\n";
        if (degree == 0) {
            result += Integer.toString(coefficients[0]);
            return result;
        }
        result += coefficients[degree] + "*x^" + degree;
        int coefficient;
        for (int i = degree - 1; i > 0; i--) {
            coefficient = coefficients[i];
            if (coefficient == 0) {
                continue;
            }
            result += " + " + coefficient + "*x^" + i;
        }
        if (coefficients[0] != 0) {
            result += " + " + coefficients[0];
        }
        return result;
    }

    // --------------------------
    // private helper methods
    // --------------------------

    /**
     * Reduce the coefficients of this polynomial into the interval
     * <tt>[0, q)</tt>.
     */
    private void reduceCoeffThis() {
        for (int i = N - 1; i >= 0; i--) {
            coefficients[i] %= q;
            if (coefficients[i] < 0) {
                coefficients[i] += q;
            }
        }
    }

    /**
     * Reduce the coefficients of a polynomial (given as the int array of its
     * coefficients) modulo <tt>q</tt> into the interval <tt>[0, q)</tt>.
     *
     * @param p the polynomial
     */
    private void reduceCoeffThis(int[] p) {
        for (int i = p.length - 1; i >= 0; i--) {
            p[i] %= q;
            if (p[i] < 0) {
                p[i] += q;
            }
        }
    }

    /**
     * Compute and set the degree of this polynomial.
     */
    private void computeDegree() {
        for (degree = coefficients.length - 1; degree > 0
                && coefficients[degree] == 0; degree--)
            ;
    }

    /**
     * Compute the degree of a polynomial, given as the array of its
     * coefficients.
     *
     * @param p the polynomial
     * @return the degree of the given polynomial
     */
    private int computeDegree(int[] p) {
        int degree;
        for (degree = p.length - 1; degree > 0 && p[degree] == 0; degree--)
            ;
        return degree;
    }

    /**
     * Compute the sum of two polynomials given as arrays of their coefficients.
     *
     * @param a the first polynomial
     * @param b the second polynomial
     * @return a + b
     */
    private int[] add(int[] a, int[] b) {
        int[] result = new int[N];
        int[] addend;
        if (a.length < b.length) {
            System.arraycopy(b, 0, result, 0, b.length);
            addend = a;
        } else {
            System.arraycopy(a, 0, result, 0, a.length);
            addend = b;
        }

        for (int i = addend.length - 1; i >= 0; i--) {
            result[i] += addend[i];
        }

        return result;
    }

    /**
     * Compute the product of two polynomials, given as arrays of their
     * coefficients.
     *
     * @param a the first polynomial
     * @param b the second polynomial
     * @return a * b (newly created)
     */
    private int[] multiply(int[] a, int[] b) {
        int[] result = new int[N];

        for (int i = a.length - 1; i >= 0; i--) {
            int index = i;
            for (int j = 0; j < b.length; j++) {
                result[index++] += a[i] * b[j];
                if (index == N) {
                    index = 0;
                }
            }
        }

        return result;
    }

    /**
     * Compute the extended Euclidean algorithm of this polynomial and
     * <tt>X^N-1</tt>. Return <tt>d=gcd(this, X^N-1)</tt> and <tt>u</tt>
     * such that <tt>this*u=d mod X^N-1</tt>.
     *
     * @return <tt>ModQPolynomial[] {d,u}</tt>, where
     * <tt>d=gcd(this, X^N-1)</tt> and <tt>this*u=d mod X^N-1</tt>
     */
    private ModQConvolutionPolynomial[] extGCD() {

        // create the unit polynomial
        int[] u = new int[N];
        u[0] = 1;

        // create polynomial X^N-1
        int[] d = new int[N + 1];
        d[0] = -1;
        d[N] = 1;
        int dDegree = N;

        // create the zero polynomial
        int[] v1 = new int[N];

        // clone this polynomial
        int[] v3 = IntUtils.clone(coefficients);
        int v3Degree = degree;
        int[] duv = IntegerFunctions.extGCD(v3[v3Degree], q);

        boolean minus = true;

        while (duv[0] != q) {
            int[] r = IntUtils.clone(d);
            int k = dDegree - v3Degree;
            int[] v = new int[k + 1];
            for (int i = k, c = dDegree; i >= 0; i--, c--) {
                if (r[c] == 0) {
                    v[i] = 0;
                } else {
                    v[i] = (duv[1] * r[c]) % q;
                    r[c] = 0;
                    // compute r = r - v*v3
                    int cc = c;
                    for (int j = v3Degree - 1; j >= 0; j--) {
                        cc--;
                        r[cc] -= v[i] * v3[j];
                        r[cc] %= q;
                    }
                }
            }

            d = IntUtils.clone(v3);
            dDegree = computeDegree(d);

            if (r.length > N) {
                v3 = new int[N];
                System.arraycopy(r, 0, v3, 0, N);
            } else {
                v3 = IntUtils.clone(r);
            }
            reduceCoeffThis(v3);
            v3Degree = computeDegree(v3);
            duv = IntegerFunctions.extGCD(v3[v3Degree], q);

            int[] uClone = IntUtils.clone(u);

            u = add(v1, multiply(v, u));
            reduceCoeffThis(u);

            v1 = uClone;

            minus = !minus;
        }

        duv = IntegerFunctions.extGCD(d[dDegree], q);
        d[dDegree] = duv[0];
        for (int i = dDegree - 1; i >= 0; i--) {
            d[i] *= duv[1];
        }
        if (minus) {
            duv[1] = q - duv[1];
        }
        for (int i = N - 1; i >= 0; i--) {
            v1[i] *= duv[1];
        }

        // create and return result polynomials
        return new ModQConvolutionPolynomial[]{
                new ModQConvolutionPolynomial(N, q, d),
                new ModQConvolutionPolynomial(N, q, v1)};
    }

}
