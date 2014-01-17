package de.flexiprovider.pqc.pflash;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.math.codingtheory.GF2mField;


public class PFlashKeyGenParameterSpec implements AlgorithmParameterSpec {

    /**
     * the default dimension of K<sup>n</sup>
     */
    public static final int DEFAULT_n = 96;

    /**
     * the default value of &#945
     */
    public static final int DEFAULT_alpha = 32;

    /**
     * the default value of r
     */
    public static final int DEFAULT_r = 32;

    /**
     * the default value of s
     */
    public static final int DEFAULT_s = 1;

    /**
     * the default field Polynomial : X<sup>4</sup> + X + 1 = 10011<sub>bin</sub> = 19<sub>10</sub>
     */
    public static final int DEFAULT_fieldPoly = 19;

    /**
     * extension degree of E/K
     */
    private int n;

    /**
     * the parameter &#945, r, s
     */
    private int alpha, r, s;

    /**
     * the field GF(2<sup>m</sup>)
     */
    private GF2mField field;

    /**
     * Constructor with default settings.
     */
    public PFlashKeyGenParameterSpec() {
        this(DEFAULT_fieldPoly, DEFAULT_n, DEFAULT_alpha, DEFAULT_r, DEFAULT_s);
    }

    public PFlashKeyGenParameterSpec(int p, int n, int alpha, int r, int s) {
        this(new GF2mField(IntegerFunctions.ceilLog(p) - 1, p), n, alpha, r, s);
    }

    public PFlashKeyGenParameterSpec(GF2mField field, int n, int alpha, int r, int s) {
        this.field = field;
        this.n = n;
        this.alpha = alpha;
        this.r = r;
        this.s = s;
    }

    public GF2mField getField() {
        return field;
    }

    public int getN() {
        return n;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getR() {
        return r;
    }

    public int getS() {
        return s;
    }
}

