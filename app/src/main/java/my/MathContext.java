/*
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * Portions Copyright IBM Corporation, 1997, 2001. All Rights Reserved.
 */

package my;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

/**
 * Immutable objects which encapsulate the context settings which
 * describe certain rules for numerical operators, such as those
 * implemented by the {@link BigDecimal} class.
 * <p/>
 * <p>The base-independent settings are:
 * <ol>
 * <li>{@code precision}:
 * the number of digits to be used for an operation; results are
 * rounded to this precision
 * <p/>
 * <li>{@code roundingMode}:
 * a {@link RoundingMode} object which specifies the algorithm to be
 * used for rounding.
 * </ol>
 *
 * @author Mike Cowlishaw
 * @author Joseph D. Darcy
 * @see BigDecimal
 * @see RoundingMode
 * @since 1.5
 */

public final class MathContext implements Serializable {

    /* ----- Constants ----- */

    // Smallest values for digits (Maximum is Integer.MAX_VALUE)
    private static final int MIN_DIGITS = 0;

    // Serialization version
    private static final long serialVersionUID = 5579720004786848255L;

    /* ----- Public Properties ----- */


    /* ----- Shared Properties ----- */
    /**
     * The number of digits to be used for an operation.  A value of 0
     * indicates that unlimited precision (as many digits as are
     * required) will be used.  Note that leading zeros (in the
     * coefficient of a number) are never significant.
     * <p/>
     * <p>{@code precision} will always be non-negative.
     *
     * @serial
     */
    final int precision;

    /**
     * The rounding algorithm to be used for an operation.
     *
     * @serial
     * @see RoundingMode
     */
    final RoundingMode roundingMode;

    /* ----- Constructors ----- */


    public MathContext(int setPrecision,
                       RoundingMode setRoundingMode) {
        if (setPrecision < MIN_DIGITS)
            throw new IllegalArgumentException("Digits < 0");
        if (setRoundingMode == null)
            throw new NullPointerException("null RoundingMode");

        precision = setPrecision;
        roundingMode = setRoundingMode;
    }


    public boolean equals(Object x) {
        MathContext mc;
        if (!(x instanceof MathContext))
            return false;
        mc = (MathContext) x;
        return mc.precision == this.precision
                && mc.roundingMode == this.roundingMode; // no need for .equals()
    }


    public int hashCode() {
        return this.precision + roundingMode.hashCode() * 59;
    }


    public String toString() {
        return "precision=" + precision + " " +
                "roundingMode=" + roundingMode.toString();
    }

    // Private methods


    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();     // read in all fields
        // validate possibly bad fields
        if (precision < MIN_DIGITS) {
            String message = "MathContext: invalid digits in stream";
            throw new StreamCorruptedException(message);
        }
        if (roundingMode == null) {
            String message = "MathContext: null roundingMode in stream";
            throw new StreamCorruptedException(message);
        }
    }

}
