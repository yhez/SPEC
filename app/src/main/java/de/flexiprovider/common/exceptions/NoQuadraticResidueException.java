/*
 * Copyright (c) 1998-2003 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 */

package de.flexiprovider.common.exceptions;

import de.flexiprovider.common.math.FlexiBigInt;

/**
 * This exception is thrown, if the square root modulo a prime of a nonquadratic
 * residue is to be calculated.
 *
 * @author Birgit Henhapl
 * @see de.flexiprovider.common.math.ellipticcurves.Point
 * @see de.flexiprovider.common.math.ellipticcurves.PointGFP
 */
public class NoQuadraticResidueException extends ECException {


    public NoQuadraticResidueException(FlexiBigInt a, FlexiBigInt p) {
        super("NoQuadraticResidueException:\na = " + a + " is not"
                + "a quadratic residue mod p = " + p + ".");
    }

}
