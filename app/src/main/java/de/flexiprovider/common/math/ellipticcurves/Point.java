package de.flexiprovider.common.math.ellipticcurves;


import java.security.spec.InvalidParameterSpecException;

import de.flexiprovider.common.exceptions.DifferentCurvesException;
import de.flexiprovider.common.exceptions.DifferentFieldsException;
import de.flexiprovider.common.exceptions.InvalidFormatException;
import de.flexiprovider.common.exceptions.InvalidPointException;
import de.flexiprovider.common.exceptions.NoQuadraticResidueException;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.math.finitefields.GFElement;
import de.flexiprovider.common.math.finitefields.GFPElement;
import de.flexiprovider.common.util.FlexiBigIntUtils;
import de.flexiprovider.ec.parameters.CurveParams;


public class Point {
    public static final int ENCODING_TYPE_UNCOMPRESSED = 0;
    public static final int ENCODING_TYPE_COMPRESSED = 1;
    public static final int ENCODING_TYPE_HYBRID = 2;
    protected EllipticCurve mE;
    protected FlexiBigInt mP;
    private GFPElement mA;
    private GFPElement mB;
    private GFPElement mX;
    private GFPElement mY;
    private GFPElement mZ;
    private GFPElement mZ2;
    private GFPElement mZ3;
    private GFPElement mAZ4;


    public Point(EllipticCurve E) {
        mE = E;
        mP = E.getQ();
        mA = (GFPElement) E.getA();
        mB = (GFPElement) E.getB();
        assignZero();
    }


    public Point(GFPElement x, GFPElement y, EllipticCurve E)
            throws InvalidPointException, DifferentFieldsException {

        mE = E;
        mP = E.getQ();
        mA = (GFPElement) E.getA();
        mB = (GFPElement) E.getB();

        mX = (GFPElement) x.clone();
        mY = (GFPElement) y.clone();
        mZ = GFPElement.ONE(mP);
        mZ2 = null;
        mZ3 = null;
        mAZ4 = null;
    }


    public Point(GFPElement x, GFPElement y, GFPElement z, EllipticCurve E)
            throws InvalidPointException, DifferentFieldsException {

        mE = E;
        mP = E.getQ();
        mA = (GFPElement) E.getA();
        mB = (GFPElement) E.getB();

        mX = x;
        mY = y;
        mZ = z;
        mZ2 = null;
        mZ3 = null;
        mAZ4 = null;
    }


    public Point(byte[] encoded, EllipticCurve E)
            throws InvalidPointException, InvalidFormatException {

        mE = E;
        mP = E.getQ();
        mA = (GFPElement) E.getA();
        mB = (GFPElement) E.getB();

        // the zero point is encoded as a single byte 0
        if (encoded.length == 1 && encoded[0] == 0) {
            assignZero();
            return;
        }

        // the first OCTET pc indicates the form the point is represented in:
        // if pc = 2, the indicating bit is not set (point = pc | x)
        // if pc = 3, the indicating bit is set (point = pc | x)
        // if pc = 4, x and y are given: (point = pc | x | y), |x| = |y| =
        // (|point| - 1) / 2)
        // if pc = 6, x and y are given and the indicating bit is not set:
        // (point = pc | x | y), |x| = |y| = (|point| - 1) / 2)
        // if pc = 7, x and y are given and the indicating bit is set: (point =
        // pc | x | y), |x| = |y| = (|point| - 1) / 2)

        byte[] bX, bY;
        GFPElement x, y, z;

        final byte pc = encoded[0];

        switch (pc) {

            case 2:
            case 3:
                // compressed form
                bX = new byte[encoded.length - 1];
                System.arraycopy(encoded, 1, bX, 0, bX.length);
                x = new GFPElement(new FlexiBigInt(1, bX), mP);
                boolean yMod2 = (pc & 1) == 1;
                y = decompress(yMod2, x);
                break;

            case 4:
                // uncompressed form
                int l = (encoded.length - 1) >> 1;
                bX = new byte[l];
                bY = new byte[l];
                System.arraycopy(encoded, 1, bX, 0, l);
                System.arraycopy(encoded, 1 + l, bY, 0, l);
                x = new GFPElement(new FlexiBigInt(1, bX), mP);
                y = new GFPElement(new FlexiBigInt(1, bY), mP);
                break;

            case 6:
            case 7:
                // hybrid form
                l = (encoded.length - 1) >> 1;
                bX = new byte[l];
                bY = new byte[l];
                System.arraycopy(encoded, 1, bX, 0, l);
                System.arraycopy(encoded, 1 + l, bY, 0, l);
                x = new GFPElement(new FlexiBigInt(1, bX), mP);
                y = new GFPElement(new FlexiBigInt(1, bY), mP);
                yMod2 = (pc & 0x01) == 1;
                if (!(decompress(yMod2, x).equals(y))) {
                    throw new InvalidPointException();
                }
                break;

            default:
                throw new InvalidFormatException(pc);
        }

        z = GFPElement.ONE(mP);

        assign(x, y, z);
    }


    public Point(Point other) {
        mE = other.mE;
        mP = other.mP;
        mA = other.mA;
        mB = other.mB;

        assign(other);
    }

    public static Point OS2ECP(byte[] encoded, CurveParams params)
            throws InvalidPointException, InvalidFormatException,
            InvalidParameterSpecException {
        EllipticCurve mE = params.getE();

        Point mW;
        if (mE != null) {
            mW = new Point(encoded, mE);
        } else {
            throw new InvalidParameterSpecException(
                    "the parameters are defined neither over GF(p) nor over GF(2^n)");
        }

        return mW;
    }

    public final EllipticCurve getE() {
        return mE;
    }

    public final byte[] EC2OSP(int type) {
        switch (type) {
            case ENCODING_TYPE_COMPRESSED:
                return encodeCompressed();
            case ENCODING_TYPE_UNCOMPRESSED:
                return encodeUncompressed();
            case ENCODING_TYPE_HYBRID:
                return encodeHybrid();
            default:
                return null;
        }
    }

    private void assignZero() {
        mX = GFPElement.ONE(mP);
        mY = GFPElement.ONE(mP);
        mZ = GFPElement.ZERO(mP);
        mZ2 = null;
        mZ3 = null;
        mAZ4 = null;
    }


    private void assign(GFPElement x, GFPElement y, GFPElement z) {
        mX = x;
        mY = y;
        mZ = z;
        mZ2 = null;
        mZ3 = null;
        mAZ4 = null;
    }


    private void assign(Point other) {
        mX = (GFPElement) other.mX.clone();
        mY = (GFPElement) other.mY.clone();
        mZ = (GFPElement) other.mZ.clone();
        mZ2 = null;
        mZ3 = null;
        mAZ4 = null;
    }


    public Object clone() {
        return new Point(this);
    }

    public boolean equals(Object other) {

        // Guard against other==null or being of an unsuitable type:
        if (other == null || !(other instanceof Point)) {
            return false;
        }

        Point otherPoint = (Point) other;

        if (!mE.equals(otherPoint.mE)) {
            return false;
        }

        if (isZero() && otherPoint.isZero()) {
            return true;
        }

        GFElement oX = (GFElement) otherPoint.mX.clone();
        GFElement oY = (GFElement) otherPoint.mY.clone();
        GFElement oZ = (GFElement) otherPoint.mZ.clone();

        if (mZ.isOne() && oZ.isOne()) {
            if (!(oX.equals(mX) && oY.equals(mY))) {
                return false;
            }
        }

        if (!mZ.isOne()) {
            GFElement z = (GFElement) mZ.clone();
            GFElement z2 = z.multiply(z);
            GFElement z3 = z2.multiply(z);

            oX.multiplyThisBy(z2);
            oY.multiplyThisBy(z3);
        }

        GFElement x = (GFElement) mX.clone();
        GFElement y = (GFElement) mY.clone();

        if (!oZ.isOne()) {
            GFElement oZ2 = oZ.multiply(oZ);
            GFElement oZ3 = oZ2.multiply(oZ);

            x.multiplyThisBy(oZ2);
            y.multiplyThisBy(oZ3);
        }

        return oX.equals(x) && oY.equals(y);
    }


    public int hashCode() {
        // Two projective points are equal iff their corresponding
        // affine representations are equal. We cannot simply sum over the
        // (hash values of) projective coordinates because the projective
        // representation is not unique: a given point (x,y) might be
        // represented as (X,Y,Z) or (X',Y',Z').
        //
        // This hash code could possibly be precomputed whenever the value of
        // this point changes.
        return getXAffin().hashCode() + getYAffin().hashCode();
    }

    public String toString() {
        if (isZero()) {
            return "(0, 0)";
        }
        return "(" + getXAffin().toString() + ",\n " + getYAffin().toString()
                + ")";
    }

    public GFElement getX() {
        return mX;
    }


    public GFElement getY() {
        return mY;
    }


    public GFElement getXAffin() {

        // TODO the zero point has no affine coordinates
        if (isZero()) {
            return GFPElement.ZERO(mP);
        }

        // return mX*mZ^-2
        if (mZ2 == null) {
            mZ2 = (GFPElement) mZ.multiply(mZ);
        }

        return mX.multiply(mZ2.invert());
    }


    private GFElement getYAffin() {

        // TODO the zero point has no affine coordinates
        if (isZero()) {
            return GFPElement.ZERO(mP);
        }
        // return mY*mZ^-3
        if (mZ3 == null) {
            mZ3 = (GFPElement) mZ.multiply(mZ).multiply(mZ);
        }

        return mY.multiply(mZ3.invert());
    }

    public Point getAffin() {
        if (!(mZ.isOne()) && !(mZ.isZero())) {
            GFElement z = mZ.invert();
            GFElement z2 = z.multiply(z);
            z.multiplyThisBy(z2);
            GFElement x = mX.multiply(z2);
            GFElement y = mY.multiply(z);
            return new Point((GFPElement) x, (GFPElement) y,
                    mE);
        }
        return this;
    }


    public boolean onCurve() {
        // The point at infinity is always on the curve:
        if (isZero()) {
            return true;
        }

        // y^2
        final GFElement y2 = mY.multiply(mY);
        // x^3
        final GFElement x3 = mX.multiply(mX).multiply(mX);

	/*
     * If the jacobian coordinate Z is 1, we can use the simpler affine
	 * equation for E:
	 */
        if (mZ.isOne()) {
            // Compare y^2 to (x^3 + ax + b):
            final GFElement ax = mA.multiply(mX); // a*x
            return y2.equals(x3.add(ax).add(mB));
        }
    /*
     * Z != 1, we have to use the jacobian equation for E:
	 */
        // Update mZ* fields if necessary:
        if (mZ2 == null) {
            mZ2 = (GFPElement) mZ.multiply(mZ); // z^2
        }
        if (mZ3 == null) {
            mZ3 = (GFPElement) mZ2.multiply(mZ); // z^3
        }
        if (mAZ4 == null) {
            mAZ4 = (GFPElement) mZ3.multiply(mZ).multiply(mA); // a*z^4
        }

        // Compare y^2 to (x^3 + axz^4 + bz^6):
        final GFElement aXZ4 = mAZ4.multiply(mX); // a*x*z^4
        final GFElement bZ6 = mB.multiply(mZ3).multiply(mZ3); // b*z^6
        return y2.equals(x3.add(aXZ4).add(bZ6));
    }

    public boolean isZero() {
        return mX.isOne() && mY.isOne() && mZ.isZero();
    }

    public Point add(Point other) {
        Point result = new Point(this);
        result.addToThis(other);
        return result;
    }


    public void addToThis(Point other) {

        if (other == null) {
            throw new DifferentCurvesException();
        }

        if (isZero()) {
            assign(other);
            return;
        }

        if (other.isZero()) {
            return;
        }

        GFElement oX = other.mX;
        GFElement oY = other.mY;
        GFElement oZ = other.mZ;
        GFElement oZ2 = other.mZ2;
        GFElement oZ3 = other.mZ3;

        GFElement U1;
        GFElement U2;
        GFElement S1;
        GFElement S2;

        if (oZ.isOne()) {

            // U_1 = X_1*Z_22
            //
            U1 = mX;

            // S_1 = Y_1*Z_23
            //
            S1 = mY;
        } else {

            if (oZ2 == null || oZ3 == null) {
                oZ2 = oZ.multiply(oZ);
                oZ3 = oZ2.multiply(oZ);
            }

            // U_1 = X_1*Z_22
            //
            U1 = mX.multiply(oZ2);

            // S_1 = Y_1*Z_23
            //
            S1 = mY.multiply(oZ3);

        }

        if (mZ.isOne()) {

            // U_2 = X_2*Z_12
            //
            U2 = oX;

            // S_2 = Y_2*Z_13
            //
            S2 = oY;
        } else {

            if (mZ2 == null || mZ3 == null) {
                mZ2 = (GFPElement) mZ.multiply(mZ);
                mZ3 = (GFPElement) mZ2.multiply(mZ);
            }

            // U_2 = X_2*Z_12
            //
            U2 = oX.multiply(mZ2);

            // S_2 = Y_2*Z_13
            //
            S2 = oY.multiply(mZ3);

        }

        // H = U2 - U1
        //
        GFElement H = U2.subtract(U1);

        // 3 = S2 - S1
        //
        GFElement r = S2.subtract(S1);

        if (H.isZero()) {
            if (r.isZero()) {
                multiplyThisBy2();
                return;
            }
            assignZero();
            return;
        }

        // U2 = H^2
        //
        U2 = H.multiply(H);

        // S2 = H^3
        //
        S2 = U2.multiply(H);

        // U2 = U1H^2
        //
        U2.multiplyThisBy(U1);

        // x = r^2 - S2 - 2U2
        //
        GFElement x = r.multiply(r).subtract(S2).subtract(U2.add(U2));

        // y = r(U2 - x) -S1S2
        //
        GFElement z = S1.multiply(S2);

        GFElement y = r.multiply(U2.subtract(x)).subtract(z);

        // z = Z1Z2H
        //
        if (mZ.isOne()) {
            if (!oZ.isOne()) {
                z = oZ.multiply(H);

            } else {
                z = H;

            }
        } else if (!oZ.isOne()) {
            U1 = mZ.multiply(oZ);

            z = U1.multiply(H);

        } else {
            z = mZ.multiply(H);

        }

        assign((GFPElement) x, (GFPElement) y, (GFPElement) z);
    }


    public void subtractFromThis(Point other) {

        if (other == null) {
            throw new DifferentCurvesException();
        }

        Point minusOther = other.negate();

        if (isZero()) {
            assign(minusOther.mX, minusOther.mY, minusOther.mZ);
        } else {
            addToThis(minusOther);
        }
    }

    public Point negate() {
        Point result = new Point(this);
        result.negateThis();
        return result;
    }


    private void negateThis() {
        if (!isZero()) {
            // y = -mY mod mP
            FlexiBigInt y = mP.add(mY.toFlexiBigInt().negate());
            mY = new GFPElement(y, mP);
        }
    }


    public Point multiplyBy2() {
        Point result = new Point(this);
        result.multiplyThisBy2();
        return result;
    }


    public void multiplyThisBy2() {

        if (isZero()) {
            assignZero();
            return;
        }
        if (mY.isZero()) {
            assignZero();
            return;
        }

        // z = Y^2
        GFElement z = mY.multiply(mY);

        // S = 4XY^2
        GFElement S = mX.multiply(z);

        GFElement x = S.add(S);
        S = x.add(x);

        // M = 3X^2 + a(Z^2)^2
        //
        if (mAZ4 == null) {
            if (mZ.isOne()) {
                mAZ4 = (GFPElement) mA.clone();
            } else {
                if (mZ2 == null) {
                    mZ2 = (GFPElement) mZ.multiply(mZ);

                }
                x = mZ2.multiply(mZ2);
                mAZ4 = (GFPElement) mA.multiply(x);

            }
        }
        GFElement y = mX.multiply(mX);

        GFElement M = y.add(y).add(y).add(mAZ4); // 3X^2+aZ^4
        // T = x = -2S + M^2
        //
        x = M.multiply(M).subtract(S.add(S));

        // y = -8Y^4 + M(S - T)
        //
        y = z.multiply(z);

        GFElement U = y.add(y); // 2Y^4
        z = U.add(U);
        U = z.add(z); // 8Y^4
        y = M.multiply(S.subtract(x)).subtract(U);

        // z = 2YZ;
        //
        if (!mZ.isOne()) {
            z = mY.multiply(mZ);

        } else {
            z = mY;
        }
        z = z.add(z);

        assign((GFPElement) x, (GFPElement) y, (GFPElement) z);
    }


    public Point multiplyBy2Affine() {

        if (this.isZero()) {
            return new Point(this.mE);
        }

        if (this.mY.equals(FlexiBigInt.ZERO)) {
            return new Point(mE);
        }

        Point p = this.getAffin();

        FlexiBigInt pX = p.mX.toFlexiBigInt();
        FlexiBigInt pY = p.mY.toFlexiBigInt();
        FlexiBigInt lambda, x, y, tmp;

        tmp = pY.add(pY).modInverse(mP);
        lambda = pX.multiply(pX).mod(mP);
        lambda = lambda
                .multiply(new FlexiBigInt(Integer.toString(3))).mod(
                        mP);
        lambda = lambda.add(mA.toFlexiBigInt());
        lambda = lambda.multiply(tmp).mod(mP);

        x = lambda.multiply(lambda).mod(mP);
        x = x.subtract(pX.add(pX)).mod(mP);

        y = pX.subtract(x);
        y = lambda.multiply(y);
        y = y.subtract(pY).mod(mP);

        GFPElement gfpx = new GFPElement(x, mP);
        GFPElement gfpy = new GFPElement(y, mP);
        return new Point(gfpx, gfpy, p.mE);
    }

    byte[] encodeUncompressed() {

        // the zero point is encoded as a single byte 0
        if (isZero()) {
            return new byte[1];
        }

        int l = mP.bitLength();
        final int dummy = l & 7;
        if (dummy != 0) {
            l += 8 - dummy;
        }
        l >>>= 3;

        byte[] encoded = new byte[(l << 1) + 1];

        encoded[0] = 4;

        FlexiBigInt x = getXAffin().toFlexiBigInt();
        FlexiBigInt y = getYAffin().toFlexiBigInt();
        byte[] bX = FlexiBigIntUtils.toMinimalByteArray(x);
        byte[] bY = FlexiBigIntUtils.toMinimalByteArray(y);
        System.arraycopy(bX, 0, encoded, 1 + l - bX.length, bX.length);
        System.arraycopy(bY, 0, encoded, 1 + (l << 1) - bY.length, bY.length);

        return encoded;
    }


    byte[] encodeCompressed() {

        // the zero point is encoded as a single byte 0
        if (isZero()) {
            return new byte[1];
        }

        int l = mP.bitLength();
        int dummy = l & 7;
        if (dummy != 0) {
            l += 8 - dummy;
        }
        l >>>= 3;

        byte[] encoded = new byte[l + 1];

        encoded[0] = 2;

        FlexiBigInt x = getXAffin().toFlexiBigInt();
        byte[] bX = FlexiBigIntUtils.toMinimalByteArray(x);
        System.arraycopy(bX, 0, encoded, 1 + l - bX.length, bX.length);

        FlexiBigInt y = getYAffin().toFlexiBigInt();
        if (y.testBit(0)) {
            encoded[0] |= 1;
        }

        return encoded;
    }

    byte[] encodeHybrid() {

        // the zero point is encoded as a single byte 0
        if (isZero()) {
            return new byte[1];
        }

        int l = mP.bitLength();
        final int dummy = l & 7;
        if (dummy != 0) {
            l += 8 - dummy;
        }
        l >>>= 3;

        byte[] encoded = new byte[(l << 1) + 1];

        encoded[0] = 6;

        FlexiBigInt x = getXAffin().toFlexiBigInt();
        FlexiBigInt y = getYAffin().toFlexiBigInt();
        byte[] bX = FlexiBigIntUtils.toMinimalByteArray(x);
        byte[] bY = FlexiBigIntUtils.toMinimalByteArray(y);
        System.arraycopy(bX, 0, encoded, 1 + l - bX.length, bX.length);
        System.arraycopy(bY, 0, encoded, 1 + (l << 1) - bY.length, bY.length);

        if (y.testBit(0)) {
            encoded[0] |= 1;
        }

        return encoded;
    }


    private GFPElement decompress(boolean yMod2, GFElement x)
            throws InvalidPointException {

        // compute g = x^3 + ax + b mod p
        FlexiBigInt xVal = x.toFlexiBigInt();
        // x3 = x^3
        FlexiBigInt x3 = xVal.multiply(xVal).multiply(xVal);
        FlexiBigInt g = mA.toFlexiBigInt().multiply(xVal);
        g = g.add(x3);
        g = g.add(mB.toFlexiBigInt());
        g = g.mod(mP);

        FlexiBigInt z;
        try {
            // compute z = sqrt(g) mod p
            z = IntegerFunctions.ressol(g, mP);
        } catch (NoQuadraticResidueException NQRExc) {
            throw new InvalidPointException("NoQuadraticResidueException: "
                    + NQRExc.getMessage());
        }

        // if lowest bit of z and yMod2 are not equal, compute z = p - z
        boolean zMod2 = z.testBit(0);
        if ((zMod2 && !yMod2) || (!zMod2 && yMod2)) {
            z = mP.subtract(z);
        }

        return new GFPElement(z, mP);
    }

}
