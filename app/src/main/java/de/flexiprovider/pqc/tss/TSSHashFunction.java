package de.flexiprovider.pqc.tss;

import java.util.Vector;


public class TSSHashFunction {

    private int m;
    private Vector a;

    public TSSHashFunction(Vector a) {
        this.a = a;
        m = a.size();
    }

    public TSSPolynomial calculatHash(Vector vec) {
        Vector intermediateResult = new Vector();
        for (int i = m; i > 0; i--) {
            intermediateResult.addElement(((TSSPolynomial) a.elementAt(i - 1))
                    .multiply((TSSPolynomial) vec.elementAt(i - 1)));
        }

        return elementSum(intermediateResult);
    }

    private TSSPolynomial elementSum(Vector v) {
        int size = v.size();
        TSSPolynomial result = (TSSPolynomial) v.elementAt(size - 1);

        for (int j = size - 1; j > 0; j--) {
            result.addToThis((TSSPolynomial) v.elementAt(j - 1));
        }

        return result;
    }
}
