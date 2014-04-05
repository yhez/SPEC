package de.flexiprovider.ec.parameters;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurve;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurveGFP;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.PointGFP;
import de.flexiprovider.common.math.finitefields.GFPElement;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.common.util.StringUtils;


public abstract class CurveParams implements java.security.spec.AlgorithmParameterSpec {

    FlexiBigInt q;
    EllipticCurve E;
    Point g;
    private FlexiBigInt r;
    private int k;
    protected CurveParams(String r, String k) {
        String s = StringUtils.filterSpaces(r);
        this.r = new FlexiBigInt(s, 16);
        s = StringUtils.filterSpaces(k);
        this.k = Integer.valueOf(s, 16);
    }
    protected CurveParams(Point g, FlexiBigInt r, int k) {
        this.g = g;
        E = g.getE();
        q = E.getQ();
        this.r = r;
        this.k = k;
    }
    public FlexiBigInt getQ() {
        return q;
    }
    public EllipticCurve getE() {
        return E;
    }
    public Point getG() {
        return (Point) g.clone();
    }
    public FlexiBigInt getR() {
        return r;
    }
    public int getK() {
        return k;
    }
    public int hashCode() {
        int oidHashCode = 0;
        int qHashCode = 0;
        int eHashCode = 0;
        int gHashCode = 0;
        int rHashCode = 0;
        int kHashCode = k;
        if (q != null) {
            qHashCode = q.hashCode();
        }
        if (E != null) {
            eHashCode = E.hashCode();
        }
        if (g != null) {
            gHashCode = g.hashCode();
        }
        if (r != null) {
            rHashCode = r.hashCode();
        }
        return oidHashCode + qHashCode + eHashCode + gHashCode + rHashCode + kHashCode;
    }


    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof CurveParams)) {
            return false;
        }
        CurveParams otherParams = (CurveParams) other;
        return q.equals(otherParams.q)
                && E.equals(otherParams.E) && g.equals(otherParams.g)
                && r.equals(otherParams.r) && (k == otherParams.k);
    }
    public static class CurveParamsGFP extends CurveParams {
        protected CurveParamsGFP(String a, String b, String p,
                                 String g, String r, String k) {
            super(r, k);
            String s = StringUtils.filterSpaces(p);
            this.q = new FlexiBigInt(s, 16);

            s = StringUtils.filterSpaces(a);
            byte[] encA = ByteUtils.fromHexString(s);
            GFPElement mA = new GFPElement(encA, this.q);
            s = StringUtils.filterSpaces(b);
            byte[] encB = ByteUtils.fromHexString(s);
            GFPElement mB = new GFPElement(encB, this.q);
            E = new EllipticCurveGFP(mA, mB, this.q);
            s = StringUtils.filterSpaces(g);
            byte[] encG = ByteUtils.fromHexString(s);
            this.g = new PointGFP(encG, (EllipticCurveGFP) E);
        }

        /**
         * Compare these parameters with another object.
         *
         * @param other the other object
         * @return the result of the comparison
         */
        public boolean equals(Object other) {
            return !((other == null) || !(other instanceof CurveParamsGFP)) && super.equals(other);
        }
    }
}
