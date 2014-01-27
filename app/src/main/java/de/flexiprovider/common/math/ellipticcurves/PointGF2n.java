package de.flexiprovider.common.math.ellipticcurves;

import de.flexiprovider.common.exceptions.DifferentCurvesException;
import de.flexiprovider.common.exceptions.DifferentFieldsException;
import de.flexiprovider.common.exceptions.InvalidFormatException;
import de.flexiprovider.common.exceptions.InvalidPointException;
import de.flexiprovider.common.math.finitefields.GF2nElement;
import de.flexiprovider.common.math.finitefields.GF2nField;
import de.flexiprovider.common.math.finitefields.GF2nONBElement;
import de.flexiprovider.common.math.finitefields.GF2nONBField;
import de.flexiprovider.common.math.finitefields.GF2nPolynomialElement;
import de.flexiprovider.common.math.finitefields.GF2nPolynomialField;
import de.flexiprovider.common.math.finitefields.GFElement;


public class PointGF2n extends Point {




    private int mDeg;


    private GF2nField mGF2n;

    private boolean isGF2nONBField = false;


    private GF2nElement mA;


    private boolean mAIsZero;


    private GF2nElement mB;

    private GF2nElement mX;

    private GF2nElement mY;


    private GF2nElement mZ;


    public PointGF2n(EllipticCurveGF2n E) {
        mE = E;
        mP = E.getQ();
        mA = (GF2nElement) E.getA();
        mAIsZero = mA.isZero();
        mB = (GF2nElement) E.getB();
        mGF2n = mA.getField();
        mDeg = mGF2n.getDegree();
        setGF2nFieldType();
        assignZero();
    }


    public PointGF2n(GF2nElement x, GF2nElement y, EllipticCurveGF2n E)
            throws InvalidPointException, DifferentFieldsException {

        mE = E;
        mP = E.getQ();
        mA = (GF2nElement) E.getA();
        mAIsZero = mA.isZero();
        mB = (GF2nElement) E.getB();
        mGF2n = mA.getField();
        mDeg = mGF2n.getDegree();
        setGF2nFieldType();

        mX = (GF2nElement) x.clone();
        mY = (GF2nElement) y.clone();
        mZ = createGF2nOneElement(mGF2n);
    }



    public PointGF2n(byte[] encoded, EllipticCurveGF2n E)
            throws InvalidPointException, InvalidFormatException {

        mE = E;
        mP = E.getQ();
        mA = (GF2nElement) E.getA();
        mAIsZero = mA.isZero();
        mB = (GF2nElement) E.getB();
        mGF2n = mA.getField();
        mDeg = mGF2n.getDegree();
        setGF2nFieldType();

        // the zero point is encoded as a single byte 0
        if (encoded.length == 1 && encoded[0] == 0) {
            assignZero();
            return;
        }

        byte[] bX, bY;

        final byte pc = encoded[0];

        switch (pc) {

            case 2:
            case 3:
                // compressed form
                bX = new byte[encoded.length - 1];
                System.arraycopy(encoded, 1, bX, 0, bX.length);
                mX = createGF2nElement(bX);
                boolean yMod2 = (pc & 0x01) == 1;
                mY = decompress(yMod2, mX);
                break;

            case 4:
                // uncompressed form
                int l = (encoded.length - 1) >> 1;
                bX = new byte[l];
                bY = new byte[l];
                System.arraycopy(encoded, 1, bX, 0, l);
                System.arraycopy(encoded, 1 + l, bY, 0, l);
                mX = createGF2nElement(bX);
                mY = createGF2nElement(bY);
                break;

            case 6:
            case 7:
                // hybrid form
                l = (encoded.length - 1) >> 1;
                bX = new byte[l];
                bY = new byte[l];
                System.arraycopy(encoded, 1, bX, 0, l);
                System.arraycopy(encoded, 1 + l, bY, 0, l);
                mX = createGF2nElement(bX);
                mY = createGF2nElement(bY);
                yMod2 = (pc & 0x01) == 1;
                if (!(decompress(yMod2, mX).equals(mY))) {
                    throw new InvalidPointException();
                }
                break;

            default:
                throw new InvalidFormatException(pc);
        }

        mZ = createGF2nOneElement(mGF2n);
    }


    public PointGF2n(PointGF2n other) {
        EllipticCurveGF2n E = (EllipticCurveGF2n) other.getE();
        mE = E;
        mP = E.getQ();
        mA = (GF2nElement) E.getA();
        mAIsZero = mA.isZero();
        mB = (GF2nElement) E.getB();
        mGF2n = mA.getField();
        mDeg = mGF2n.getDegree();
        setGF2nFieldType();

        assign(other);
    }




    private void assignZero() {
        mX = createGF2nOneElement(mGF2n);
        mY = createGF2nOneElement(mGF2n);
        mZ = createGF2nZeroElement(mGF2n);
    }


    private void assign(GF2nElement x, GF2nElement y, GF2nElement z)
            throws InvalidPointException {
        mX = x;
        mY = y;
        mZ = z;
    }

    private void assign(PointGF2n other) {
        mX = (GF2nElement) other.mX.clone();
        mY = (GF2nElement) other.mY.clone();
        mZ = (GF2nElement) other.mZ.clone();
    }


    public Object clone() {
        return new PointGF2n(this);
    }


    public boolean equals(Object other) {

        // Guard against other==null or being of an unsuitable type:
        if (other == null || !(other instanceof PointGF2n)) {
            return false;
        }

        PointGF2n otherPoint = (PointGF2n) other;

        if (mZ.isOne() && otherPoint.mZ.isOne()) {
            return mX.equals(otherPoint.mX) && mY.equals(otherPoint.mY);
        }

        boolean result;

        if (mZ.isOne()) {
            GFElement z2 = otherPoint.mZ.square();

            result = otherPoint.mX.equals(mX.multiply(z2));

            z2.multiplyThisBy(otherPoint.mZ);

            // mY*z2 = P mY?
            result = result && otherPoint.mY.equals(mY.multiply(z2));

        } else if (otherPoint.mZ.isOne()) {
            // z1 = mZ^2
            GFElement z1 = mZ.square();

            result = mX.equals(otherPoint.mX.multiply(z1));

            // z1 = mZ^3
            z1.multiplyThisBy(mZ);

            //
            result = result && mY.equals(otherPoint.mY.multiply(z1));

        } else {
            // z1 = mZ^2
            GFElement z1 = mZ.square();

            GFElement z2 = otherPoint.mZ.square();

            result = mX.multiply(z2).equals(otherPoint.mX.multiply(z1));

            // z1 = mZ^3
            z1.multiplyThisBy(mZ);

            z2.multiplyThisBy(otherPoint.mZ);

            result = result
                    && mY.multiply(z2).equals(otherPoint.mY.multiply(z1));

        }

        return result;
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
        return "(" + getXAffin().toString(16) + ",\n "
                + getYAffin().toString(16) + ")";
    }




    public GFElement getX() {
        return mX;
    }


    public GFElement getY() {
        return mY;
    }


    public GFElement getXAffin() {
        if (isZero()) {
            // mZ equals zero.
            return (GF2nElement) mZ.clone();
        } else if (mZ.isOne()) {
            return mX;
        } else {
            GFElement z;
            z = mZ.square();
            z = z.invert();
            z.multiplyThisBy(mX);
            return z;
        }
    }
    private GFElement getYAffin() {
        if (isZero()) {
            // mZ equals zero.
            return (GFElement) mZ.clone();
        } else if (mZ.isOne()) {
            return (GFElement) mY.clone();
        } else {
            GFElement z;
            // z = mZ^(-3) * mY ?
            z = mZ.square();
            z.multiplyThisBy(mZ);
            z = z.invert();
            z.multiplyThisBy(mY);
            return z;
        }
    }
    public Point getAffin() {
        if (isZero()) {
            return this;
        }

        GF2nElement notZ = (GF2nElement) mZ.invert();
        GF2nElement squareNotZ = notZ.square();
        GF2nElement x = (GF2nElement) mX.multiply(squareNotZ);

        // z = mZ^(-3) * mY
        notZ.multiplyThisBy(squareNotZ);
        GF2nElement y = (GF2nElement) mY.multiply(notZ);

        return new PointGF2n(x, y, (EllipticCurveGF2n) mE);
    }
    public boolean onCurve() {

        if (isZero()) {
            return true;
        }

        GFElement left, right, tmp;

        if (mZ.isOne()) {
            right = mX.square(); // right = x^2
            tmp = right.multiply(mX); // tmp = x^3

            // right = ax^2
            //
            right.multiplyThisBy(mA);

            // right = x^3 + ax^2
            //
            right.addToThis(tmp);

            // right = x^3 + ax^2 + b
            //
            right.addToThis(mB);
            left = mX.multiply(mY); // left = xy

            // left = y^2 + xy
            //
            left.addToThis(mY.square());
        } else {
            right = mX.square(); // right = x^2
            tmp = right.multiply(mX); // tmp = x^3

            // right = ax^2
            //
            right.multiplyThisBy(mA);
            left = mZ.square(); // left = z^2

            // right = ax^2z^2
            //
            right.multiplyThisBy(left);

            // right = x^3 + ax^2z^2
            //
            right.addToThis(tmp);
            tmp = ((GF2nElement) left).square(); // tmp = z^4

            // left = z^6
            //
            left.multiplyThisBy(tmp);

            // right = x^3 + ax^2z^2 + bz^6
            //
            right.addToThis(left.multiply(mB));
            left = mX.multiply(mY); // left = xy

            // left = xyz
            //
            left.multiplyThisBy(mZ);

            // left = y^2 + xyz
            //
            left.addToThis(mY.square());
        }

        return right.equals(left);
    }
    public boolean isZero() {
        return mX.isOne() && mY.isOne() && mZ.isZero();
    }
    public Point add(Point other) throws DifferentCurvesException {
        PointGF2n result = new PointGF2n(this);
        result.addToThis(other);
        return result;
    }
    public void addToThis(Point other) throws DifferentCurvesException {

        if (!(other instanceof PointGF2n)) {
            throw new DifferentCurvesException(
                    "PointGF2n.addToThis(Point P): other is not an instance"
                            + " of PointGF2n");
        }

        PointGF2n otherPoint = (PointGF2n) other;

        // this point is at infinity
        if (isZero()) {
            assign(otherPoint);
        } else if (!otherPoint.isZero()) {

            GF2nElement T1, T2, T3, T4, T5, T6;
            GFElement T7, T8, T9;

            T1 = (GF2nElement) mX.clone();
            T2 = (GF2nElement) mY.clone();
            T3 = (GF2nElement) mZ.clone();
            T4 = (GF2nElement) otherPoint.mX.clone();
            T5 = (GF2nElement) otherPoint.mY.clone();
            T6 = (GF2nElement) otherPoint.mZ.clone();

            if (!otherPoint.mZ.isOne()) {
                T7 = T6.square();
                T1.multiplyThisBy(T7); // = U0 (if Z1 != 1)
                T7.multiplyThisBy(T6);
                T2.multiplyThisBy(T7); // = S0 (if Z1 != 1)
            }
            T7 = T3.square();
            T8 = T4.multiply(T7); // = U1
            T1.addToThis(T8); // = W
            T7.multiplyThisBy(T3);
            T8 = T5.multiply(T7); // = S1
            T2.addToThis(T8); // = R

            if (T1.isZero() && T2.isZero()) {
                multiplyThisBy2();
            } else if (T1.isZero() && !T2.isZero()) {
                assignZero();
            } else {
                T4.multiplyThisBy(T2);
                T3.multiplyThisBy(T1); // = L (= Z2 if Z1 = 1)
                T5.multiplyThisBy(T3);
                T4.addToThis(T5); // = V
                T5 = T3.square();
                T7 = T4.multiply(T5);
                if (!otherPoint.mZ.isOne()) {
                    T3.multiplyThisBy(T6); // = Z2 (if Z1 != 1)
                }
                T4 = (GF2nElement) T2.add(T3); // = T
                T2.multiplyThisBy(T4);
                T5 = T1.square();
                T1.multiplyThisBy(T5);
                if (!mAIsZero) {
                    T8 = T3.square();
                    T9 = mA.multiply(T8);
                    T1.addToThis(T9);
                }
                T1.addToThis(T2); // = X2
                T4.multiplyThisBy(T1);
                T4.addToThis(T7); // = Y2

                assign(T1, T4, T3);
            }
        }

    }
    public Point multiplyBy2() {
        PointGF2n result = new PointGF2n(this);
        result.multiplyThisBy2();
        return result;
    }
    public void multiplyThisBy2() {

        GF2nElement T1, T2, T3, T4;

        // if this point is zero, do nothing!
        //
        if (!isZero()) {

            T1 = (GF2nElement) mX.clone();
            T2 = (GF2nElement) mY.clone();
            T3 = (GF2nElement) mZ.clone();
            T4 = (GF2nElement) mB.clone();

            for (int i = 1; i < mDeg - 1; i++) {
                T4.squareThis();
            }

            if (T1.isZero() || T3.isZero()) {
                assignZero();
            } else {

                T2.multiplyThisBy(T3);
                T3.squareThis();
                T4.multiplyThisBy(T3);
                T3.multiplyThisBy(T1); // = Z2

                T2.addToThis(T3);
                T4.addToThis(T1);
                T4.squareThis();
                T4.squareThis(); // = X2
                T1.squareThis();
                T2.addToThis(T1); // = U

                T2.multiplyThisBy(T4);
                T1.squareThis();
                T1.multiplyThisBy(T3);
                T2.addToThis(T1); // = Y2
            }

            assign(T4, T2, T3);

        }
    }
    public Point multiplyBy2Affine() {
        PointGF2n p = (PointGF2n) this.getAffin();

        GF2nElement pX = p.mX;
        GF2nElement pY = p.mY;

        if (pX.isZero() || mZ.isZero()) {
            return new PointGF2n((EllipticCurveGF2n) mE);
        }

        GF2nElement lambda = (GF2nElement) pX.invert();
        lambda = (GF2nElement) lambda.multiply(pY);
        lambda = (GF2nElement) lambda.add(pX);

        GF2nElement x = (GF2nElement) lambda.square().add(lambda);
        x = (GF2nElement) x.add(mE.getA());

        GF2nElement y = (GF2nElement) lambda.add(createGF2nOneElement(mGF2n))
                .multiply(x);
        y = (GF2nElement) y.add(pX.square());

        return new PointGF2n(x, y, (EllipticCurveGF2n) mE);
    }
    public Point subtract(Point other) throws DifferentCurvesException {
        PointGF2n result = new PointGF2n(this);
        result.subtractFromThis(other);
        return result;
    }
    public void subtractFromThis(Point other) throws DifferentCurvesException {

        if (!(other instanceof PointGF2n)) {
            throw new DifferentCurvesException(
                    "PointGF2n.subtractFromThis(Point P): other is not"
                            + " an instance of PointGF2n");
        }

        PointGF2n minusOther = (PointGF2n) other.negate();
        if (isZero()) {
            assign(minusOther.mX, minusOther.mY, minusOther.mZ);
        } else {
            addToThis(minusOther);
        }
    }
    public Point negate() {
        PointGF2n result = new PointGF2n(this);
        result.negateThis();
        return result;
    }
    private void negateThis() {
        if (!isZero()) {
            GFElement tmp = mX.multiply(mZ);
            mY = (GF2nElement) tmp.add(mY);
        }
    }


    byte[] encodeUncompressed() {

        // the zero point is encoded as a single byte 0
        if (isZero()) {
            return new byte[1];
        }

        int l = mDeg;
        final int dummy = l & 7;
        if (dummy != 0) {
            l += 8 - dummy;
        }
        l >>>= 3;

        byte[] encoded = new byte[(l << 1) + 1];

        encoded[0] = 4;

        byte[] bX = getXAffin().toByteArray();
        byte[] bY = getYAffin().toByteArray();
        System.arraycopy(bX, 0, encoded, 1 + l - bX.length, bX.length);
        System.arraycopy(bY, 0, encoded, 1 + (l << 1) - bY.length, bY.length);

        return encoded;
    }

    byte[] encodeCompressed() {

        // the zero point is encoded as a single byte 0
        if (isZero()) {
            return new byte[1];
        }

        int l = mDeg;
        if ((mDeg & 7) != 0) {
            l += 8 - (mDeg & 7);
        }
        l >>>= 3;

        byte[] encoded = new byte[l + 1];

        encoded[0] = 2;

        GFElement xAff = getXAffin();
        byte[] bX = xAff.toByteArray();
        System.arraycopy(bX, 0, encoded, 1, bX.length);

        if (!(xAff.isZero())) {
            GFElement xInv = xAff.invert();
            GFElement comp = xInv.multiply(getYAffin());

            if (((GF2nElement) comp).testRightmostBit()) {
                encoded[0] |= 1;
            }
        }

        return encoded;
    }

    byte[] encodeHybrid() {

        // the zero point is encoded as a single byte 0
        if (isZero()) {
            return new byte[1];
        }

        int l = mDeg;
        if ((l & 7) != 0) {
            l += 8 - (l & 7);
        }
        l >>>= 3;

        byte[] encoded = new byte[(l << 1) + 1];

        encoded[0] = 6;

        GFElement xAff = getXAffin();
        GFElement yAff = getYAffin();
        byte[] bX = xAff.toByteArray();
        byte[] bY = yAff.toByteArray();
        System.arraycopy(bX, 0, encoded, 1 + l - bX.length, bX.length);
        System.arraycopy(bY, 0, encoded, 1 + (l << 1) - bY.length, bY.length);

        if (!(xAff.isZero())) {
            GFElement xInv = xAff.invert();
            GFElement comp = xInv.multiply(yAff);

            if (((GF2nElement) comp).testRightmostBit()) {
                encoded[0] |= 1;
            }
        }

        return encoded;
    }




    private GF2nElement decompress(boolean yMod2, GF2nElement x) {

        // if x = 0, y' = 1 -> y = b^0,5
        // if x != 0 -> y = (z + z' + y')x (see further down)

        if (x.isZero()) {
            return mB.squareRoot();
        }

        GFElement alpha, beta, tmp;
        GF2nElement z;

        // x^3 + a*x^2 + b

        // tmp = x^2
        tmp = x.square();

        // beta = (x^2)^(-1)
        beta = tmp.invert();

        // alpha = a*x^2
        alpha = tmp.multiply(mA);

        // tmp = x^3
        tmp.multiplyThisBy(x);

        // alpha = x^3 + a*x^2
        alpha.addToThis(tmp);

        // alpha = x^3 + a*x^2 + b
        alpha.addToThis(mB);

        // (x^2)^(-1)*(x^3 + a*x^2 + b)

        // beta = (x^3 + a*x^2 + b)*(x^-2)
        beta.multiplyThisBy(alpha);

        z = ((GF2nElement) beta).solveQuadraticEquation();

        if (z.testRightmostBit()) {
            z.increaseThis();
        }

        // (z + z' + yMod2) * x
        if (yMod2) {
            z.increaseThis();
        }

        z.multiplyThisBy(x);

        return z;
    }

    private void setGF2nFieldType() {
        isGF2nONBField = mGF2n instanceof GF2nONBField;
    }

    private GF2nElement createGF2nZeroElement(GF2nField gf2n) {
        if (isGF2nONBField) {
            return GF2nONBElement.ZERO((GF2nONBField) gf2n);
        }
        return GF2nPolynomialElement.ZERO((GF2nPolynomialField) gf2n);
    }

    private GF2nElement createGF2nOneElement(GF2nField gf2n) {
        if (isGF2nONBField) {
            return GF2nONBElement.ONE((GF2nONBField) gf2n);
        }
        return GF2nPolynomialElement.ONE((GF2nPolynomialField) gf2n);
    }

    private GF2nElement createGF2nElement(byte[] value) {
        if (isGF2nONBField) {
            return new GF2nONBElement((GF2nONBField) mGF2n, value);
        }
        return new GF2nPolynomialElement((GF2nPolynomialField) mGF2n, value);
    }

}
