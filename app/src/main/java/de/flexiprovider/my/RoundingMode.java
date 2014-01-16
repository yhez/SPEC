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
package de.flexiprovider.my;


public enum RoundingMode {


    UP(BigDecimal.ROUND_UP),


    DOWN(BigDecimal.ROUND_DOWN),


    CEILING(BigDecimal.ROUND_CEILING),


    FLOOR(BigDecimal.ROUND_FLOOR),


    HALF_UP(BigDecimal.ROUND_HALF_UP),


    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),


    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),


    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);

    final int oldMode;

    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }


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
