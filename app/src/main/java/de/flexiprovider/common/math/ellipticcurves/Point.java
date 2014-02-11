package de.flexiprovider.common.math.ellipticcurves;

import java.security.spec.InvalidParameterSpecException;

import de.flexiprovider.common.exceptions.DifferentCurvesException;
import de.flexiprovider.common.exceptions.InvalidFormatException;
import de.flexiprovider.common.exceptions.InvalidPointException;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.finitefields.GFElement;
import de.flexiprovider.ec.parameters.CurveParams;


public abstract class Point {

    protected EllipticCurve mE;


    protected FlexiBigInt mP;


    public static final int ENCODING_TYPE_UNCOMPRESSED = 0;


    public static final int ENCODING_TYPE_COMPRESSED = 1;


    public static final int ENCODING_TYPE_HYBRID = 2;


    public abstract Object clone();


    public abstract boolean equals(Object other);


    public abstract int hashCode();
    public final EllipticCurve getE() {
        return mE;
    }


    public abstract GFElement getX();


    public abstract GFElement getY();


    public abstract GFElement getXAffin();


    public abstract Point getAffin();


    public abstract boolean onCurve();


    public abstract boolean isZero();

    public abstract Point add(Point other) throws DifferentCurvesException;
    public abstract void addToThis(Point other) throws DifferentCurvesException;


    public abstract Point subtract(Point other) throws DifferentCurvesException;


    public abstract void subtractFromThis(Point other)
            throws DifferentCurvesException;
    public abstract Point multiplyBy2();
    public abstract void multiplyThisBy2();
    public abstract Point negate();
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


    public static Point OS2ECP(byte[] encoded, CurveParams params)
            throws InvalidPointException, InvalidFormatException,
            InvalidParameterSpecException {
        EllipticCurve mE = params.getE();

        Point mW;
        if (mE instanceof EllipticCurveGF2n) {
            mW = new PointGF2n(encoded, (EllipticCurveGF2n) mE);
        } else if (mE instanceof EllipticCurveGFP) {
            mW = new PointGFP(encoded, (EllipticCurveGFP) mE);
        } else {
            throw new InvalidParameterSpecException(
                    "the parameters are defined neither over GF(p) nor over GF(2^n)");
        }

        return mW;
    }


    abstract byte[] encodeUncompressed();


    abstract byte[] encodeCompressed();

    abstract byte[] encodeHybrid();

}
