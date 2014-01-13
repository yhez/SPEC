/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyright IBM Corporation, 2001. All Rights Reserved.
 */
package my;


public enum RoundingMode {


    UP(BigDecimal.ROUND_UP),


    DOWN(BigDecimal.ROUND_DOWN),


    CEILING(BigDecimal.ROUND_CEILING),


    FLOOR(BigDecimal.ROUND_FLOOR),


    HALF_UP(BigDecimal.ROUND_HALF_UP),

    /**
     * Rounding mode to round towards {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case round
     * down.  Behaves as for {@code RoundingMode.UP} if the discarded
     * fraction is &gt; 0.5; otherwise, behaves as for
     * {@code RoundingMode.DOWN}.
     * <p/>
     * <p>Example:
     * <table border>
     * <tr valign=top><th>Input Number</th>
     * <th>Input rounded to one digit<br> with {@code HALF_DOWN} rounding
     * <tr align=right><td>5.5</td>  <td>5</td>
     * <tr align=right><td>2.5</td>  <td>2</td>
     * <tr align=right><td>1.6</td>  <td>2</td>
     * <tr align=right><td>1.1</td>  <td>1</td>
     * <tr align=right><td>1.0</td>  <td>1</td>
     * <tr align=right><td>-1.0</td> <td>-1</td>
     * <tr align=right><td>-1.1</td> <td>-1</td>
     * <tr align=right><td>-1.6</td> <td>-2</td>
     * <tr align=right><td>-2.5</td> <td>-2</td>
     * <tr align=right><td>-5.5</td> <td>-5</td>
     * </table>
     */
    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),

    /**
     * Rounding mode to round towards the {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case, round
     * towards the even neighbor.  Behaves as for
     * {@code RoundingMode.HALF_UP} if the digit to the left of the
     * discarded fraction is odd; behaves as for
     * {@code RoundingMode.HALF_DOWN} if it's even.  Note that this
     * is the rounding mode that statistically minimizes cumulative
     * error when applied repeatedly over a sequence of calculations.
     * It is sometimes known as {@literal "Banker's rounding,"} and is
     * chiefly used in the USA.  This rounding mode is analogous to
     * the rounding policy used for {@code float} and {@code double}
     * arithmetic in Java.
     * <p/>
     * <p>Example:
     * <table border>
     * <tr valign=top><th>Input Number</th>
     * <th>Input rounded to one digit<br> with {@code HALF_EVEN} rounding
     * <tr align=right><td>5.5</td>  <td>6</td>
     * <tr align=right><td>2.5</td>  <td>2</td>
     * <tr align=right><td>1.6</td>  <td>2</td>
     * <tr align=right><td>1.1</td>  <td>1</td>
     * <tr align=right><td>1.0</td>  <td>1</td>
     * <tr align=right><td>-1.0</td> <td>-1</td>
     * <tr align=right><td>-1.1</td> <td>-1</td>
     * <tr align=right><td>-1.6</td> <td>-2</td>
     * <tr align=right><td>-2.5</td> <td>-2</td>
     * <tr align=right><td>-5.5</td> <td>-6</td>
     * </table>
     */
    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),

    /**
     * Rounding mode to assert that the requested operation has an exact
     * result, hence no rounding is necessary.  If this rounding mode is
     * specified on an operation that yields an inexact result, an
     * {@code ArithmeticException} is thrown.
     * <p>Example:
     * <table border>
     * <tr valign=top><th>Input Number</th>
     * <th>Input rounded to one digit<br> with {@code UNNECESSARY} rounding
     * <tr align=right><td>5.5</td>  <td>throw {@code ArithmeticException}</td>
     * <tr align=right><td>2.5</td>  <td>throw {@code ArithmeticException}</td>
     * <tr align=right><td>1.6</td>  <td>throw {@code ArithmeticException}</td>
     * <tr align=right><td>1.1</td>  <td>throw {@code ArithmeticException}</td>
     * <tr align=right><td>1.0</td>  <td>1</td>
     * <tr align=right><td>-1.0</td> <td>-1</td>
     * <tr align=right><td>-1.1</td> <td>throw {@code ArithmeticException}</td>
     * <tr align=right><td>-1.6</td> <td>throw {@code ArithmeticException}</td>
     * <tr align=right><td>-2.5</td> <td>throw {@code ArithmeticException}</td>
     * <tr align=right><td>-5.5</td> <td>throw {@code ArithmeticException}</td>
     * </table>
     */
    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);

    final int oldMode;

    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }

    /**
     * Returns the {@code RoundingMode} object corresponding to a
     * legacy integer rounding mode constant in {@link BigDecimal}.
     *
     * @param rm legacy integer rounding mode to convert
     * @return {@code RoundingMode} corresponding to the given integer.
     * @throws IllegalArgumentException integer is out of range
     */
    public static RoundingMode valueOf(int rm) {
        switch (rm) {

            case BigDecimal.ROUND_UP:
                return UP;

            case BigDecimal.ROUND_DOWN:
                return DOWN;

            case BigDecimal.ROUND_CEILING:
                return CEILING;

            case BigDecimal.ROUND_FLOOR:
                return FLOOR;

            case BigDecimal.ROUND_HALF_UP:
                return HALF_UP;

            case BigDecimal.ROUND_HALF_DOWN:
                return HALF_DOWN;

            case BigDecimal.ROUND_HALF_EVEN:
                return HALF_EVEN;

            case BigDecimal.ROUND_UNNECESSARY:
                return UNNECESSARY;

            default:
                throw new IllegalArgumentException("argument out of range");
        }
    }
}
