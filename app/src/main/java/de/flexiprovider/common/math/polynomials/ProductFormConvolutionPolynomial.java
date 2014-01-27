package de.flexiprovider.common.math.polynomials;

public class ProductFormConvolutionPolynomial implements ConvolutionPolynomial {

    /**
     * The degree of the reduction polynomial
     */
    int N;

    /**
     * The three sparse binary polynomials
     */
    SparseBinaryConvolutionPolynomial f1, f2, f3;


    public boolean equals(Object other) {
        if (other == null
                || !(other instanceof ProductFormConvolutionPolynomial)) {
            return false;
        }

        ProductFormConvolutionPolynomial otherPol = (ProductFormConvolutionPolynomial) other;

        return N == otherPol.N && f1.equals(otherPol.f1) && f2.equals(otherPol.f2)
                && f3.equals(otherPol.f3);

    }

    /**
     * @return the hash code of this polynomial
     */
    public int hashCode() {
        return N + f1.hashCode() + f2.hashCode() + f3.hashCode();
    }

    /**
     * @return a human readable form of this polynomial
     */
    public String toString() {
        String result = "ModQPolynomialProductForm (degree " + N + "):\n";
        result += "Polynomial f1: " + f1 + "\n";
        result += "Polynomial f2: " + f2 + "\n";
        result += "Polynomial f3: " + f3;
        return result;
    }

}
