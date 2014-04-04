package de.flexiprovider.ec.keys;

import java.security.InvalidParameterException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import de.flexiprovider.common.exceptions.InvalidFormatException;
import de.flexiprovider.common.exceptions.InvalidPointException;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.ec.parameters.CurveParams;


public final class ECPublicKeySpec implements java.security.spec.KeySpec {




    private Point mW;


    private byte[] mEncodedW;


    private CurveParams mParams;




    public ECPublicKeySpec(Point W, CurveParams params)
            throws InvalidParameterException {
        if (params == null) {
            throw new InvalidParameterException(
                    "EC domain parameters must not be null");
        }
        // TODO: Test if params match curve encoded in point
        mW = W;
        mParams = params;
    }


    public ECPublicKeySpec(byte[] encodedW, CurveParams params)
            throws InvalidParameterSpecException {
        mEncodedW = ByteUtils.clone(encodedW);
        if (params != null) {
            setParams(params);
        }
    }




    public Point getW() throws InvalidKeySpecException {
        if (mW == null) {
            throw new InvalidKeySpecException(
                    "No EC domain parameters defined for the public point");
        }
        return mW;
    }


    public byte[] getEncodedW() {
        if (mEncodedW != null) {
            return mEncodedW;
        }

        return mW.EC2OSP(Point.ENCODING_TYPE_UNCOMPRESSED);
    }


    public CurveParams getParams() {
        return mParams;
    }


    public void setParams(CurveParams params)
            throws InvalidParameterSpecException {

        if (params == null) { // case 1: deleting EC domain parameters
            // public point is already in the respective format
            if (mEncodedW != null) {
                return;
            }

            // public point has to be encoded first
            mEncodedW = mW.EC2OSP(Point.ENCODING_TYPE_UNCOMPRESSED);
            mW = null;
        } else { // case 2: defining EC domain parameters
            if (mParams == null) {
                try {
                    mW = Point.OS2ECP(mEncodedW, params);
                    mParams = params;
                    mEncodedW = null;
                } catch (InvalidPointException ipe) {
                    throw new InvalidParameterException(
                            "Unable to compute point object from encoded point "
                                    + "and given EC domain parameters "
                                    + "(caught InvalidPointException: "
                                    + ipe.getMessage() + ").");
                } catch (InvalidFormatException ife) {
                    throw new InvalidParameterException(
                            "Unable to compute point object from encoded point "
                                    + "and given EC domain parameters "
                                    + "(caught InvalidFormatException: "
                                    + ife.getMessage() + ").");
                }
            } else {
                // in this case nothing needs to be done
                if (mParams.equals(params)) {
                    return;
                }

                throw new InvalidParameterException(
                        "Illegally tried to change existing curve parameters.");
            }
        }

    }

}
