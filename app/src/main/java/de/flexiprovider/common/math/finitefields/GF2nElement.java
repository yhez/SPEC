package de.flexiprovider.common.math.finitefields;

import de.flexiprovider.common.exceptions.DifferentFieldsException;


public abstract class GF2nElement implements GFElement {


    public abstract Object clone();


    public final GFElement subtract(GFElement minuend)
            throws DifferentFieldsException {
        return add(minuend);
    }


}
