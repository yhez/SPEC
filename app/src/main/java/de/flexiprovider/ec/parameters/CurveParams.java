package de.flexiprovider.ec.parameters;

import codec.asn1.ASN1ObjectIdentifier;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurve;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurveGFP;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.PointGF2n;
import de.flexiprovider.common.math.ellipticcurves.PointGFP;
import de.flexiprovider.common.math.finitefields.GFPElement;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.common.util.StringUtils;


public abstract class CurveParams implements AlgorithmParameterSpec {

    /**
     * OID
     */
    private ASN1ObjectIdentifier oid;

    FlexiBigInt q;

    /**
     * elliptic curve E
     */
    EllipticCurve E;

    /**
     * basepoint G
     */
    Point g;

    /**
     * order r of basepoint G
     */
    private FlexiBigInt r;

    /**
     * cofactor k
     */
    private int k;

    /**
     * Construct new curve parameters from the given Strings.
     *
     * @param r order r of basepoint G
     * @param k cofactor k
     */
    protected CurveParams(String r, String k) {
        String s = StringUtils.filterSpaces(r);
        this.r = new FlexiBigInt(s, 16);
        s = StringUtils.filterSpaces(k);
        this.k = Integer.valueOf(s, 16);
    }

    /**
     * Construct new curve parameters from the given Strings.
     *
     * @param oid OID of the curve parameters
     * @param r   order r of basepoint G
     * @param k   cofactor k
     */
    protected CurveParams(String oid, String r, String k) {
        this.oid = new ASN1ObjectIdentifier(oid);
        String s = StringUtils.filterSpaces(r);
        this.r = new FlexiBigInt(s, 16);
        s = StringUtils.filterSpaces(k);
        this.k = Integer.valueOf(s, 16);
    }

    /**
     * Construct new curve parameters from the given parameters.
     *
     * @param g basepoint G
     * @param r order r of basepoint G
     * @param k cofactor k
     */
    protected CurveParams(Point g, FlexiBigInt r, int k) {
        this.g = g;
        E = g.getE();
        q = E.getQ();
        this.r = r;
        this.k = k;
    }

    /**
     * @return the OID of the curve parameters
     */
    public ASN1ObjectIdentifier getOID() {
        return oid;
    }

    /**
     * @return the size of the underlying field
     */
    public FlexiBigInt getQ() {
        return q;
    }

    /**
     * @return the elliptic curve <tt>E</tt>
     */
    public EllipticCurve getE() {
        return E;
    }

    /**
     * @return a copy of the basepoint <tt>G</tt>
     */
    public Point getG() {
        return (Point) g.clone();
    }

    /**
     * @return the order <tt>r</tt> of basepoint <tt>G</tt>
     */
    public FlexiBigInt getR() {
        return r;
    }

    /**
     * @return the cofactor <tt>k</tt>
     */
    public int getK() {
        return k;
    }

    /**
     * @return the hash code of these curve parameters
     */
    public int hashCode() {

        int oidHashCode = 0;
        int qHashCode = 0;
        int eHashCode = 0;
        int gHashCode = 0;
        int rHashCode = 0;
        int kHashCode = k;

        if (oid != null) {
            oidHashCode = oid.hashCode();
        }
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

    /**
     * Compare these parameters with another object.
     *
     * @param other the other object
     * @return the result of the comparison
     */
    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof CurveParams)) {
            return false;
        }
        CurveParams otherParams = (CurveParams) other;
        return oid.equals(otherParams.oid) && q.equals(otherParams.q)
                && E.equals(otherParams.E) && g.equals(otherParams.g)
                && r.equals(otherParams.r) && (k == otherParams.k);
    }

    /**
     * Inner class for representing prime curve parameters.
     */
    public static class CurveParamsGFP extends CurveParams {

        /**
         * Construct new curve parameters from the given parameters.
         *
         * @param g basepoint G
         * @param r order r of basepoint G
         * @param k cofactor k
         */
        public CurveParamsGFP(PointGFP g, FlexiBigInt r, int k) {
            super(g, r, k);
        }

        /**
         * Construct new curve parameters from the given Strings.
         *
         * @param oid OID of the curve parameters (can be <tt>null</tt>)
         * @param a   curve coefficient a
         * @param b   curve coefficient b
         * @param p   prime characteristic p
         * @param g   basepoint G
         * @param r   order r of basepoint G
         * @param k   cofactor k
         */
        protected CurveParamsGFP(String oid, String a, String b, String p,
                                 String g, String r, String k) {
            super(oid, r, k);

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
         * @return the hash code of these curve parameters
         */
        public int hashCode() {
            return super.hashCode();
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

    /**
     * Inner class for representing char 2 curve parameters.
     */
    public abstract static class CurveParamsGF2n extends CurveParams {

        /**
         * extension degree n
         */
        protected int n;

        /**
         * Construct new curve parameters from the given parameters.
         *
         * @param g basepoint G
         * @param r order r of basepoint G
         * @param n extension degree n
         * @param k cofactor k
         */
        protected CurveParamsGF2n(PointGF2n g, FlexiBigInt r, int n, int k) {
            super(g, r, k);
            this.n = n;
        }

        /**
         * @return the extension degree <tt>n</tt> of the underlying field
         */
        public int getN() {
            return n;
        }

        /**
         * @return the hash code of these curve parameters
         */
        public int hashCode() {
            return super.hashCode() + n;
        }

        /**
         * Compare these parameters with another object.
         *
         * @param other the other object
         * @return the result of the comparison
         */
        public boolean equals(Object other) {
            if ((other == null) || !(other instanceof CurveParamsGF2n)) {
                return false;
            }
            CurveParamsGF2n otherParams = (CurveParamsGF2n) other;
            return super.equals(other) && (n == otherParams.n);
        }
    }

    /**
     * Inner class for representing char 2 curve parameters.
     */
    public static class CurveParamsGF2nONB extends CurveParamsGF2n {

        /**
         * Construct new curve parameters from the given parameters.
         *
         * @param g basepoint G
         * @param r order r of basepoint G
         * @param n extension degree n
         * @param k cofactor k
         */
        public CurveParamsGF2nONB(PointGF2n g, FlexiBigInt r, int n, int k) {
            super(g, r, n, k);
        }

        /**
         * @return the hash code of these curve parameters
         */
        public int hashCode() {
            return super.hashCode();
        }

        /**
         * Compare these parameters with another object.
         *
         * @param other the other object
         * @return the result of the comparison
         */
        public boolean equals(Object other) {
            return !((other == null) || !(other instanceof CurveParamsGF2nONB)) && super.equals(other);
        }
    }

    /**
     * Inner class for representing char 2 trinomial curve parameters.
     */
    public static class CurveParamsGF2nTrinomial extends CurveParamsGF2n {
        /**
         * trinomial coefficient
         */
        private int tc;

        /**
         * Construct new curve parameters from the given parameters.
         *
         * @param g  basepoint G
         * @param r  order r of basepoint G
         * @param n  extension degree n
         * @param k  cofactor k
         * @param tc trinomial coefficient
         */
        public CurveParamsGF2nTrinomial(PointGF2n g, FlexiBigInt r, int n,
                                        int k, int tc) {
            super(g, r, n, k);
            this.tc = tc;
        }

        /**
         * @return the trinomial coefficient
         */
        public int getTC() {
            return tc;
        }

        /**
         * @return the hash code of these curve parameters
         */
        public int hashCode() {
            return super.hashCode() + tc;
        }

        /**
         * Compare these parameters with another object.
         *
         * @param other the other object
         * @return the result of the comparison
         */
        public boolean equals(Object other) {
            if ((other == null) || !(other instanceof CurveParamsGF2nTrinomial)) {
                return false;
            }
            CurveParamsGF2nTrinomial otherParams = (CurveParamsGF2nTrinomial) other;
            return super.equals(other) && (tc == otherParams.tc);
        }
    }

    /**
     * Inner class for representing char 2 pentanomial curve parameters.
     */
    public static class CurveParamsGF2nPentanomial extends CurveParamsGF2n {
        /**
         * first pentanomial coefficient
         */
        private int pc1;

        /**
         * second pentanomial coefficient
         */
        private int pc2;

        /**
         * third pentanomial coefficient
         */
        private int pc3;

        /**
         * Construct new curve parameters from the given parameters.
         *
         * @param g   basepoint G
         * @param r   order r of basepoint G
         * @param n   extension degree n
         * @param k   cofactor k
         * @param pc1 first pentanomial coefficient
         * @param pc2 second pentanomial coefficient
         * @param pc3 third pentanomial coefficient
         */
        public CurveParamsGF2nPentanomial(PointGF2n g, FlexiBigInt r, int n,
                                          int k, int pc1, int pc2, int pc3) {
            super(g, r, n, k);
            this.pc1 = pc1;
            this.pc2 = pc2;
            this.pc3 = pc3;
        }

        /**
         * @return the first pentanomial coefficient
         */
        public int getPC1() {
            return pc1;
        }

        /**
         * @return the second pentanomial coefficient
         */
        public int getPC2() {
            return pc2;
        }

        /**
         * @return the third pentanomial coefficient
         */
        public int getPC3() {
            return pc3;
        }

        /**
         * @return the hash code of these curve parameters
         */
        public int hashCode() {
            return super.hashCode() + pc1 + pc2 + pc3;
        }

        /**
         * Compare these parameters with another object.
         *
         * @param other the other object
         * @return the result of the comparison
         */
        public boolean equals(Object other) {
            if ((other == null)
                    || !(other instanceof CurveParamsGF2nPentanomial)) {
                return false;
            }
            CurveParamsGF2nPentanomial otherParams = (CurveParamsGF2nPentanomial) other;
            return super.equals(other) && (pc1 == otherParams.pc1)
                    && (pc2 == otherParams.pc2) && (pc3 == otherParams.pc3);
        }
    }

}
