package de.flexiprovider.common.math.finitefields;

import de.flexiprovider.common.exceptions.DifferentFieldsException;
import de.flexiprovider.common.exceptions.NoSolutionException;


public abstract class GF2nElement implements GFElement {




    protected GF2nField mField;


    protected int mDegree;




    public abstract Object clone();


    public abstract boolean testRightmostBit();


    public final GF2nField getField() {
        return mField;
    }
    public abstract void increaseThis();


    public final GFElement subtract(GFElement minuend)
            throws DifferentFieldsException {
        return add(minuend);
    }

    public abstract GF2nElement square();


    public abstract void squareThis();


    public abstract GF2nElement squareRoot();


    public abstract GF2nElement solveQuadraticEquation()
            throws NoSolutionException;

}
